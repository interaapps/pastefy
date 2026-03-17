<script setup lang="ts">
import { computed, defineAsyncComponent, ref, watch } from 'vue'
import { useClipboard } from '@vueuse/core'
import Button from 'primevue/button'
import InputText from 'primevue/inputtext'
import Select from 'primevue/select'
import Tag from 'primevue/tag'
import XMLTreeNode, { type XMLNodeEntry } from '@/components/previews/XMLTreeNode.vue'
const Highlighted = defineAsyncComponent(() => import('@/components/Highlighted.vue'))

const props = defineProps<{
  xml: string
}>()

const clipboard = useClipboard()
const search = ref('')
const viewMode = ref<'tree' | 'pretty' | 'raw'>('tree')
const expandedPaths = ref(new Set<string>(['root']))

const parserResult = computed(() => {
  try {
    const parser = new DOMParser()
    const documentNode = parser.parseFromString(props.xml, 'application/xml')
    const errorNode = documentNode.querySelector('parsererror')
    if (errorNode) {
      return {
        root: undefined,
        error: errorNode.textContent || 'Invalid XML',
        pretty: props.xml,
      }
    }

    const convert = (element: Element, path: string): XMLNodeEntry => ({
      name: element.tagName,
      path,
      attributes: Array.from(element.attributes).map((attribute) => ({
        name: attribute.name,
        value: attribute.value,
      })),
      text:
        Array.from(element.childNodes)
          .filter((node) => node.nodeType === Node.TEXT_NODE)
          .map((node) => node.textContent?.trim() || '')
          .filter(Boolean)
          .join(' ') || undefined,
      children: Array.from(element.children).map((child, index) =>
        convert(child, `${path}.${child.tagName}[${index}]`),
      ),
    })

    const serializer = new XMLSerializer()
    return {
      root: documentNode.documentElement
        ? convert(documentNode.documentElement, `root.${documentNode.documentElement.tagName}`)
        : undefined,
      error: undefined,
      pretty: serializer.serializeToString(documentNode),
    }
  } catch (error) {
    return {
      root: undefined,
      error: error instanceof Error ? error.message : 'Invalid XML',
      pretty: props.xml,
    }
  }
})

const rootNode = computed(() => parserResult.value.root)
const prettyXml = computed(() => parserResult.value.pretty)

const viewOptions = [
  { label: 'Tree', value: 'tree' },
  { label: 'Pretty', value: 'pretty' },
  { label: 'Raw', value: 'raw' },
]

const summary = computed(() => {
  if (!rootNode.value) return parserResult.value.error || 'Invalid XML'
  return rootNode.value.name
})

const stats = computed(() => {
  let elements = 0
  let attributes = 0
  let textNodes = 0

  const walk = (node?: XMLNodeEntry) => {
    if (!node) return
    elements++
    attributes += node.attributes.length
    if (node.text) textNodes++
    node.children.forEach(walk)
  }

  walk(rootNode.value)

  return { elements, attributes, textNodes }
})

const setExpanded = (path: string, expanded: boolean) => {
  const next = new Set(expandedPaths.value)
  if (expanded) next.add(path)
  else next.delete(path)
  expandedPaths.value = next
}

const isExpanded = (path: string) => expandedPaths.value.has(path)

const expandAll = () => {
  const next = new Set<string>(['root'])

  const walk = (node?: XMLNodeEntry) => {
    if (!node) return
    next.add(node.path)
    node.children.forEach(walk)
  }

  walk(rootNode.value)
  expandedPaths.value = next
}

const collapseAll = () => {
  expandedPaths.value = new Set(['root'])
}

const exportXml = (mode: 'pretty' | 'raw') => {
  const contents = mode === 'pretty' ? prettyXml.value : props.xml
  const blob = new Blob([contents], { type: 'application/xml' })
  const objectUrl = URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = objectUrl
  link.download = `pastefy-export.${mode}.xml`
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
          <div class="text-xs font-semibold tracking-[0.2em] uppercase opacity-60">XML View</div>
          <div class="mt-1 text-xs opacity-60">{{ summary }}</div>
        </div>
        <div class="flex flex-wrap gap-2">
          <Tag :value="`${stats.elements} elements`" severity="contrast" />
          <Tag :value="`${stats.attributes} attrs`" severity="contrast" />
          <Tag :value="`${stats.textNodes} texts`" severity="contrast" />
        </div>
      </div>

      <div class="grid gap-3 xl:grid-cols-[minmax(0,1fr)_180px_auto]">
        <InputText v-model="search" placeholder="Search tags, attributes, text, and paths" fluid />
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
            label="Copy pretty"
            size="small"
            severity="contrast"
            outlined
            @click="clipboard.copy(prettyXml)"
          />
          <Button
            icon="ti ti-download"
            label="Export"
            size="small"
            severity="contrast"
            outlined
            @click="exportXml(viewMode === 'raw' ? 'raw' : 'pretty')"
          />
        </div>
      </div>
    </div>

    <div
      v-if="parserResult.error"
      class="p-4 font-[JetBrains_Mono_Variable] text-[0.92rem] leading-7 whitespace-pre-wrap text-red-700 dark:text-red-300"
    >
      {{ parserResult.error }}
    </div>

    <div v-else-if="viewMode === 'tree'" class="max-h-[42rem] overflow-auto px-2 py-3">
      <XMLTreeNode
        v-if="rootNode"
        :node="rootNode"
        :search="search"
        :is-expanded="isExpanded"
        :set-expanded="setExpanded"
      />
    </div>

    <Highlighted
      v-else-if="viewMode === 'pretty'"
      class="max-h-[42rem] overflow-auto"
      file-name="document.xml"
      :contents="prettyXml"
      show-copy-button
    />

    <Highlighted
      v-else
      class="max-h-[42rem] overflow-auto"
      file-name="document.xml"
      :contents="props.xml"
      show-copy-button
    />
  </div>
</template>
