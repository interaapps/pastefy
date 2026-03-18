<script setup lang="ts">
import InputText from 'primevue/inputtext'
import Select from 'primevue/select'
import Textarea from 'primevue/textarea'
import { computed } from 'vue'
import { useStorage } from '@vueuse/core'

import MermaidToolResult from '@/components/tools/utility/MermaidToolResult.vue'
import UtilityShell from '@/components/tools/utility/UtilityShell.vue'
import { parseYamlDocument, parseYamlDocuments } from '@/utils/yaml.ts'
import { isRecord } from '@/utils/structured-data.ts'

type InfraFormat = 'auto' | 'compose' | 'kubernetes' | 'terraform' | 'github-actions'
type MermaidNode = {
  id: string
  label: string
}
type KubernetesResource = {
  id: string
  kind: string
  name: string
  document: Record<string, unknown>
}

const source = useStorage(
  'pastefy-utility-infra-source',
  `services:
  web:
    image: pastefy/web:latest
    depends_on: [db, redis]
  db:
    image: postgres:16
  redis:
    image: redis:7`,
)
const format = useStorage<InfraFormat>('pastefy-utility-infra-format', 'auto')
const direction = useStorage('pastefy-utility-infra-direction', 'LR')
const graphTitle = useStorage('pastefy-utility-infra-title', 'Infrastructure Overview')

const formatOptions = [
  { label: 'Auto detect', value: 'auto' as InfraFormat },
  { label: 'Docker Compose', value: 'compose' as InfraFormat },
  { label: 'Kubernetes', value: 'kubernetes' as InfraFormat },
  { label: 'Terraform / HCL', value: 'terraform' as InfraFormat },
  { label: 'GitHub Actions', value: 'github-actions' as InfraFormat },
]

const directionOptions = [
  { label: 'Left to right', value: 'LR' },
  { label: 'Top to bottom', value: 'TD' },
]

const sanitizeNodeId = (...parts: string[]) => {
  const value = parts
    .join('_')
    .replace(/[^a-zA-Z0-9_]/g, '_')
    .replace(/_+/g, '_')
    .replace(/^_+|_+$/g, '')
  return value || 'node'
}

const toRecordArray = (value: unknown): Record<string, unknown>[] =>
  Array.isArray(value) ? value.filter(isRecord) : []

const getNestedRecord = (value: unknown, path: string[]): Record<string, unknown> | undefined => {
  let current: unknown = value
  for (const segment of path) {
    if (!isRecord(current)) return undefined
    current = current[segment]
  }
  return isRecord(current) ? current : undefined
}

const getNestedArray = (value: unknown, path: string[]): unknown[] => {
  const current = path.reduce<unknown>((entry, segment) => {
    if (!isRecord(entry)) return undefined
    return entry[segment]
  }, value)

  return Array.isArray(current) ? current : []
}

const getMetadataLabels = (resource: KubernetesResource) => {
  if (resource.kind === 'Pod') {
    return getNestedRecord(resource.document, ['metadata', 'labels']) ?? {}
  }

  if (resource.kind === 'CronJob') {
    return getNestedRecord(resource.document, ['spec', 'jobTemplate', 'spec', 'template', 'metadata', 'labels']) ?? {}
  }

  return getNestedRecord(resource.document, ['spec', 'template', 'metadata', 'labels']) ?? {}
}

const getContainerSpecs = (resource: KubernetesResource) => {
  if (resource.kind === 'Pod') {
    return toRecordArray(getNestedArray(resource.document, ['spec', 'containers']))
  }

  if (resource.kind === 'CronJob') {
    return toRecordArray(getNestedArray(resource.document, ['spec', 'jobTemplate', 'spec', 'template', 'spec', 'containers']))
  }

  return toRecordArray(getNestedArray(resource.document, ['spec', 'template', 'spec', 'containers']))
}

const detectedFormat = computed<Exclude<InfraFormat, 'auto'>>(() => {
  if (format.value !== 'auto') return format.value
  const trimmed = source.value.trim()
  if (/^\s*resource\s+"/m.test(trimmed) || /^\s*module\s+"/m.test(trimmed)) return 'terraform'
  if (/^\s*name:\s+/m.test(trimmed) && /^\s*jobs:\s+/m.test(trimmed)) return 'github-actions'
  if (/^\s*apiVersion:\s+/m.test(trimmed) && /^\s*kind:\s+/m.test(trimmed)) return 'kubernetes'
  return 'compose'
})

const diagram = computed(() => {
  try {
    const lines = ['flowchart ' + direction.value, `  %% ${graphTitle.value}`]
    const definedNodes = new Map<string, MermaidNode>()
    const definedEdges = new Set<string>()

    const ensureNode = (id: string, label: string) => {
      if (definedNodes.has(id)) return
      definedNodes.set(id, { id, label })
      lines.push(`  ${id}[${JSON.stringify(label)}]`)
    }

    const ensureEdge = (from: string, to: string) => {
      const edgeKey = `${from}-->${to}`
      if (definedEdges.has(edgeKey)) return
      definedEdges.add(edgeKey)
      lines.push(`  ${from} --> ${to}`)
    }

    if (detectedFormat.value === 'terraform') {
      const resources = Array.from(source.value.matchAll(/resource\s+"([^"]+)"\s+"([^"]+)"/g)).map(
        (match) => `${match[1]}.${match[2]}`,
      )
      const modules = Array.from(source.value.matchAll(/module\s+"([^"]+)"/g)).map((match) => `module.${match[1]}`)
      const providers = Array.from(source.value.matchAll(/provider\s+"([^"]+)"/g)).map((match) => `provider.${match[1]}`)

      ;[...providers, ...modules, ...resources].forEach((entry) => {
        const id = sanitizeNodeId(entry)
        ensureNode(id, entry)
      })

      modules.forEach((moduleName) => {
        const moduleId = sanitizeNodeId(moduleName)
        resources.forEach((resource) => {
          ensureEdge(moduleId, sanitizeNodeId(resource))
        })
      })
      return lines.join('\n')
    }

    if (detectedFormat.value === 'compose') {
      const doc = parseYamlDocument(source.value)
      if (!isRecord(doc) || !isRecord(doc.services)) return lines.join('\n')

      Object.entries(doc.services).forEach(([name, config]) => {
        const serviceId = sanitizeNodeId(name)
        ensureNode(serviceId, name)

        const depends = isRecord(config)
          ? Array.isArray(config.depends_on)
            ? config.depends_on
            : isRecord(config.depends_on)
              ? Object.keys(config.depends_on)
              : []
          : []

        depends.forEach((dependency) => {
          const dependencyName = String(dependency)
          const dependencyId = sanitizeNodeId(dependencyName)
          ensureNode(dependencyId, dependencyName)
          ensureEdge(serviceId, dependencyId)
        })
      })
      return lines.join('\n')
    }

    if (detectedFormat.value === 'github-actions') {
      const doc = parseYamlDocument(source.value)
      if (!isRecord(doc) || !isRecord(doc.jobs)) return lines.join('\n')

      Object.entries(doc.jobs).forEach(([name, config]) => {
        const id = sanitizeNodeId(name)
        ensureNode(id, name)
        const needs = isRecord(config) ? config.needs : undefined
        const deps = Array.isArray(needs) ? needs : needs ? [needs] : []
        deps.forEach((dependency) => {
          const dependencyName = String(dependency)
          const dependencyId = sanitizeNodeId(dependencyName)
          ensureNode(dependencyId, dependencyName)
          ensureEdge(dependencyId, id)
        })
      })
      return lines.join('\n')
    }

    if (detectedFormat.value === 'kubernetes') {
      const resources = parseYamlDocuments(source.value)
        .filter(isRecord)
        .map((entry, index) => {
          const kind = String(entry.kind || `Resource${index + 1}`)
          const metadata = isRecord(entry.metadata) ? entry.metadata : {}
          const name = String(metadata.name || `${kind}-${index + 1}`)
          return {
            id: sanitizeNodeId(kind, name),
            kind,
            name,
            document: entry,
          } satisfies KubernetesResource
        })

      const resourceIndex = new Map<string, KubernetesResource>()
      const resourceKindsWithPods = new Set(['Deployment', 'StatefulSet', 'DaemonSet', 'ReplicaSet', 'Pod', 'Job', 'CronJob'])

      const ensureResource = (kind: string, name: string) => {
        const key = `${kind}:${name}`.toLowerCase()
        const existing = resourceIndex.get(key)
        if (existing) return existing

        const resource = {
          id: sanitizeNodeId(kind, name),
          kind,
          name,
          document: {},
        } satisfies KubernetesResource
        resourceIndex.set(key, resource)
        ensureNode(resource.id, `${kind}: ${name}`)
        return resource
      }

      resources.forEach((resource) => {
        resourceIndex.set(`${resource.kind}:${resource.name}`.toLowerCase(), resource)
        ensureNode(resource.id, `${resource.kind}: ${resource.name}`)
      })

      resources.forEach((resource) => {
        if (resource.kind === 'Ingress') {
          const rules = toRecordArray(getNestedArray(resource.document, ['spec', 'rules']))
          rules.forEach((rule) => {
            const paths = toRecordArray(getNestedArray(rule, ['http', 'paths']))
            paths.forEach((path) => {
              const serviceName = getNestedRecord(path, ['backend', 'service'])
              const name = typeof serviceName?.name === 'string' ? serviceName.name : ''
              if (!name) return
              ensureEdge(resource.id, ensureResource('Service', name).id)
            })
          })

          const defaultBackend = getNestedRecord(resource.document, ['spec', 'defaultBackend', 'service'])
          if (typeof defaultBackend?.name === 'string' && defaultBackend.name) {
            ensureEdge(resource.id, ensureResource('Service', defaultBackend.name).id)
          }
          return
        }

        if (resource.kind === 'Service') {
          const selector = getNestedRecord(resource.document, ['spec', 'selector']) ?? {}
          if (!Object.keys(selector).length) return

          resources
            .filter((candidate) => resourceKindsWithPods.has(candidate.kind))
            .forEach((candidate) => {
              const labels = getMetadataLabels(candidate)
              const matches = Object.entries(selector).every(([key, value]) => labels[key] === value)
              if (matches) ensureEdge(resource.id, candidate.id)
            })
          return
        }

        if (!resourceKindsWithPods.has(resource.kind)) return

        const containers = getContainerSpecs(resource)
        containers.forEach((container) => {
          toRecordArray(container.envFrom).forEach((entry) => {
            const configMapRef = isRecord(entry.configMapRef) ? entry.configMapRef : undefined
            if (typeof configMapRef?.name === 'string' && configMapRef.name) {
              ensureEdge(resource.id, ensureResource('ConfigMap', configMapRef.name).id)
            }

            const secretRef = isRecord(entry.secretRef) ? entry.secretRef : undefined
            if (typeof secretRef?.name === 'string' && secretRef.name) {
              ensureEdge(resource.id, ensureResource('Secret', secretRef.name).id)
            }
          })

          toRecordArray(container.env).forEach((entry) => {
            const valueFrom = isRecord(entry.valueFrom) ? entry.valueFrom : undefined
            const configMapKeyRef = isRecord(valueFrom?.configMapKeyRef) ? valueFrom.configMapKeyRef : undefined
            if (typeof configMapKeyRef?.name === 'string' && configMapKeyRef.name) {
              ensureEdge(resource.id, ensureResource('ConfigMap', configMapKeyRef.name).id)
            }

            const secretKeyRef = isRecord(valueFrom?.secretKeyRef) ? valueFrom.secretKeyRef : undefined
            if (typeof secretKeyRef?.name === 'string' && secretKeyRef.name) {
              ensureEdge(resource.id, ensureResource('Secret', secretKeyRef.name).id)
            }
          })
        })
      })
      return lines.join('\n')
    }

    return lines.join('\n')
  } catch (error) {
    return `%% ${(error as Error).message || 'Could not convert this infrastructure definition.'}`
  }
})
</script>

<template>
  <UtilityShell
    control-title="Infra Input"
    control-description="Turn Compose, Kubernetes, Terraform, or GitHub Actions files into Mermaid architecture flowcharts."
    result-title="Infrastructure Diagram"
    result-description="Generated Mermaid flowchart for the detected infrastructure format."
  >
    <template #controls>
      <div class="grid gap-3 md:grid-cols-2">
        <div>
          <label class="mb-1 block text-sm text-neutral-500 dark:text-neutral-400">Format</label>
          <Select v-model="format" :options="formatOptions" option-label="label" option-value="value" fluid />
        </div>
        <div>
          <label class="mb-1 block text-sm text-neutral-500 dark:text-neutral-400">Direction</label>
          <Select v-model="direction" :options="directionOptions" option-label="label" option-value="value" fluid />
        </div>
        <div class="md:col-span-2">
          <label class="mb-1 block text-sm text-neutral-500 dark:text-neutral-400">Diagram title</label>
          <InputText v-model="graphTitle" fluid />
        </div>
      </div>
      <div>
        <label class="mb-1 block text-sm text-neutral-500 dark:text-neutral-400">Infra file</label>
        <Textarea v-model="source" auto-resize rows="20" fluid />
      </div>
    </template>
    <template #result>
      <MermaidToolResult :code="diagram" file-name="infra-diagram.mmd" />
    </template>
  </UtilityShell>
</template>
