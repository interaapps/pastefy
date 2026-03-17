<script setup lang="ts">
import { computed, defineAsyncComponent, ref, watch } from 'vue'
import { useClipboard } from '@vueuse/core'
import Button from 'primevue/button'
import InputText from 'primevue/inputtext'
import Select from 'primevue/select'
import Tag from 'primevue/tag'
import YAMLTreeNode, { type YAMLNodeEntry } from '@/components/previews/YAMLTreeNode.vue'
const Highlighted = defineAsyncComponent(() => import('@/components/Highlighted.vue'))

const props = defineProps<{
  yaml: string
}>()

const clipboard = useClipboard()
const search = ref('')
const viewMode = ref<'tree' | 'pretty' | 'raw'>('tree')
const expandedPaths = ref(new Set<string>(['root']))

const parsedTree = computed(() => {
  const root: YAMLNodeEntry = {
    label: 'root',
    path: 'root',
    lineNumber: 0,
    kind: 'mapping',
    children: [],
  }

  const stack: Array<{ indent: number; node: YAMLNodeEntry }> = [{ indent: -1, node: root }]
  const sequenceCounters = new Map<string, number>()

  props.yaml.split('\n').forEach((line, index) => {
    const trimmed = line.trim()
    if (!trimmed) return

    const indent = line.match(/^\s*/)?.[0].length || 0
    while (stack.length > 1 && indent <= stack[stack.length - 1].indent) {
      stack.pop()
    }

    const parent = stack[stack.length - 1].node
    const basePath = parent.path

    let node: YAMLNodeEntry

    if (trimmed.startsWith('#')) {
      node = {
        label: trimmed,
        path: `${basePath}.comment-${index}`,
        lineNumber: index + 1,
        kind: 'comment',
        children: [],
      }
      parent.children.push(node)
      return
    }

    if (trimmed.startsWith('- ')) {
      const counter = sequenceCounters.get(basePath) || 0
      sequenceCounters.set(basePath, counter + 1)
      const remainder = trimmed.slice(2).trim()
      const separatorIndex = remainder.indexOf(':')

      if (separatorIndex > -1) {
        const label = remainder.slice(0, separatorIndex).trim()
        const value = remainder.slice(separatorIndex + 1).trim() || undefined
        node = {
          label,
          value,
          path: `${basePath}[${counter}]`,
          lineNumber: index + 1,
          kind: 'sequence',
          children: [],
        }
      } else {
        node = {
          label: `[item ${counter + 1}]`,
          value: remainder || undefined,
          path: `${basePath}[${counter}]`,
          lineNumber: index + 1,
          kind: 'sequence',
          children: [],
        }
      }

      parent.children.push(node)
      stack.push({ indent, node })
      return
    }

    const separatorIndex = trimmed.indexOf(':')
    if (separatorIndex > -1) {
      const label = trimmed.slice(0, separatorIndex).trim()
      const value = trimmed.slice(separatorIndex + 1).trim() || undefined
      node = {
        label,
        value,
        path: `${basePath}.${label}-${index}`,
        lineNumber: index + 1,
        kind: 'mapping',
        children: [],
      }
      parent.children.push(node)
      stack.push({ indent, node })
      return
    }

    node = {
      label: trimmed,
      path: `${basePath}.scalar-${index}`,
      lineNumber: index + 1,
      kind: 'scalar',
      children: [],
    }
    parent.children.push(node)
  })

  return root
})

const stats = computed(() => {
  const allNodes: YAMLNodeEntry[] = []

  const walk = (node: YAMLNodeEntry) => {
    allNodes.push(node)
    node.children.forEach(walk)
  }
  parsedTree.value.children.forEach(walk)

  return {
    keys: allNodes.filter((node) => node.kind === 'mapping').length,
    listItems: allNodes.filter((node) => node.kind === 'sequence').length,
    comments: allNodes.filter((node) => node.kind === 'comment').length,
  }
})

const viewOptions = [
  { label: 'Tree', value: 'tree' },
  { label: 'Pretty', value: 'pretty' },
  { label: 'Raw', value: 'raw' },
]

const setExpanded = (path: string, expanded: boolean) => {
  const next = new Set(expandedPaths.value)
  if (expanded) next.add(path)
  else next.delete(path)
  expandedPaths.value = next
}

const isExpanded = (path: string) => expandedPaths.value.has(path)

const expandAll = () => {
  const next = new Set<string>(['root'])
  const walk = (node: YAMLNodeEntry) => {
    next.add(node.path)
    node.children.forEach(walk)
  }
  parsedTree.value.children.forEach(walk)
  expandedPaths.value = next
}

const collapseAll = () => {
  expandedPaths.value = new Set(['root'])
}

const exportYaml = () => {
  const blob = new Blob([props.yaml], { type: 'text/yaml;charset=utf-8' })
  const objectUrl = URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = objectUrl
  link.download = 'pastefy-export.yaml'
  link.click()
  setTimeout(() => URL.revokeObjectURL(objectUrl), 1000)
}

watch(search, (value) => {
  if (value.trim()) expandAll()
})
</script>

<template>
  <div class="overflow-hidden text-sm">
    <div
      class="flex flex-col gap-4 border-b border-neutral-200 bg-neutral-50 px-4 py-4 dark:border-neutral-700 dark:bg-neutral-900/70"
    >
      <div class="flex flex-wrap items-center justify-between gap-3">
        <div>
          <div class="text-xs font-semibold tracking-[0.2em] uppercase opacity-60">YAML View</div>
          <div class="mt-1 text-xs opacity-60">Collapsible tree, search, copy, and export for YAML files</div>
        </div>
        <div class="flex flex-wrap gap-2">
          <Tag :value="`${stats.keys} keys`" severity="contrast" />
          <Tag :value="`${stats.listItems} items`" severity="contrast" />
          <Tag :value="`${stats.comments} comments`" severity="contrast" />
        </div>
      </div>

      <div class="grid gap-3 xl:grid-cols-[minmax(0,1fr)_180px_auto]">
        <InputText v-model="search" placeholder="Search keys, values, and paths" fluid />
        <Select
          v-model="viewMode"
          :options="viewOptions"
          option-label="label"
          option-value="value"
          fluid
        />
        <div class="flex flex-wrap gap-2">
          <Button
            v-if="viewMode === 'tree'"
            icon="ti ti-fold-down"
            label="Expand all"
            size="small"
            severity="contrast"
            outlined
            @click="expandAll"
          />
          <Button
            v-if="viewMode === 'tree'"
            icon="ti ti-fold-up"
            label="Collapse"
            size="small"
            severity="contrast"
            outlined
            @click="collapseAll"
          />
          <Button
            icon="ti ti-copy"
            label="Copy"
            size="small"
            severity="contrast"
            outlined
            @click="clipboard.copy(yaml)"
          />
          <Button
            icon="ti ti-download"
            label="Export"
            size="small"
            severity="contrast"
            outlined
            @click="exportYaml"
          />
        </div>
      </div>
    </div>

    <div v-if="viewMode === 'tree'" class="max-h-[42rem] overflow-auto px-2 py-3">
      <div v-if="parsedTree.children.length === 0" class="p-4 text-sm opacity-60">No YAML nodes found.</div>
      <YAMLTreeNode
        v-for="node in parsedTree.children"
        :key="node.path"
        :node="node"
        :search="search"
        :is-expanded="isExpanded"
        :set-expanded="setExpanded"
      />
    </div>

    <Highlighted
      v-else-if="viewMode === 'pretty'"
      class="max-h-[42rem] overflow-auto"
      file-name="config.yaml"
      :contents="yaml"
      show-copy-button
    />

    <Highlighted
      v-else
      class="max-h-[42rem] overflow-auto"
      file-name="config.yaml"
      :contents="yaml"
      show-copy-button
    />
  </div>
</template>
