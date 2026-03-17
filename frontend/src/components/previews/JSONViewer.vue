<script setup lang="ts">
import { computed, defineAsyncComponent, ref, watch } from 'vue'
import { useClipboard } from '@vueuse/core'
import Button from 'primevue/button'
import InputText from 'primevue/inputtext'
import Select from 'primevue/select'
import Tag from 'primevue/tag'
import JSONTreeNode from '@/components/previews/JSONTreeNode.vue'
const Highlighted = defineAsyncComponent(() => import('@/components/Highlighted.vue'))

const props = defineProps<{
  json: string
}>()

const clipboard = useClipboard()
const search = ref('')
const viewMode = ref<'tree' | 'pretty' | 'raw'>('tree')
const expandedPaths = ref(new Set<string>(['root']))

const parsed = computed(() => {
  try {
    return JSON.parse(props.json)
  } catch {
    return undefined
  }
})

const prettyJson = computed(() => {
  if (parsed.value === undefined) return props.json
  return JSON.stringify(parsed.value, null, 2)
})

const minifiedJson = computed(() => {
  if (parsed.value === undefined) return props.json
  return JSON.stringify(parsed.value)
})

const rootSummary = computed(() => {
  if (Array.isArray(parsed.value)) return `${parsed.value.length} items`
  if (parsed.value && typeof parsed.value === 'object')
    return `${Object.keys(parsed.value).length} keys`
  if (parsed.value === undefined) return 'Invalid JSON'
  return typeof parsed.value
})

const stats = computed(() => {
  const source = parsed.value

  let keys = 0
  let arrays = 0
  let objects = 0
  let primitives = 0

  const walk = (value: unknown) => {
    if (Array.isArray(value)) {
      arrays++
      value.forEach(walk)
      return
    }
    if (value !== null && typeof value === 'object') {
      objects++
      const entries = Object.entries(value as Record<string, unknown>)
      keys += entries.length
      entries.forEach(([, child]) => walk(child))
      return
    }
    primitives++
  }

  if (source !== undefined) walk(source)

  return { keys, arrays, objects, primitives }
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
  const next = new Set<string>()

  const walk = (value: unknown, path: string) => {
    if (value !== null && typeof value === 'object') {
      next.add(path)
      if (Array.isArray(value)) {
        value.forEach((child, index) => walk(child, `${path}.${index}`))
        return
      }
      Object.entries(value as Record<string, unknown>).forEach(([key, child]) =>
        walk(child, `${path}.${key}`),
      )
    }
  }

  walk(parsed.value, 'root')
  expandedPaths.value = next
}

const collapseAll = () => {
  expandedPaths.value = new Set(['root'])
}

const exportJson = (mode: 'pretty' | 'minified') => {
  const contents = mode === 'pretty' ? prettyJson.value : minifiedJson.value
  const blob = new Blob([contents], { type: 'application/json' })
  const objectUrl = URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = objectUrl
  link.download = `pastefy-export.${mode === 'pretty' ? 'pretty' : 'min'}.json`
  link.click()
  setTimeout(() => URL.revokeObjectURL(objectUrl), 1000)
}

watch(search, (value) => {
  if (value.trim()) {
    expandAll()
  }
})
</script>

<template>
  <div class="overflow-hidden text-sm">
    <div
      class="flex flex-col gap-4 border-b border-neutral-200 bg-neutral-50 px-4 py-4 dark:border-neutral-700 dark:bg-neutral-900/70"
    >
      <div class="flex flex-wrap items-center justify-between gap-3">
        <div>
          <div class="text-xs font-semibold tracking-[0.2em] uppercase opacity-60">JSON View</div>
          <div class="mt-1 text-xs opacity-60">{{ rootSummary }}</div>
        </div>
        <div class="flex flex-wrap gap-2">
          <Tag :value="`${stats.keys} keys`" severity="contrast" />
          <Tag :value="`${stats.objects} objects`" severity="contrast" />
          <Tag :value="`${stats.arrays} arrays`" severity="contrast" />
          <Tag :value="`${stats.primitives} values`" severity="contrast" />
        </div>
      </div>

      <div class="grid gap-3 xl:grid-cols-[minmax(0,1fr)_180px_auto]">
        <InputText v-model="search" placeholder="Search keys, paths, and values" fluid />
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
            @click="clipboard.copy(prettyJson)"
          />
          <Button
            icon="ti ti-download"
            label="Export"
            size="small"
            severity="contrast"
            outlined
            @click="exportJson(viewMode === 'raw' ? 'minified' : 'pretty')"
          />
        </div>
      </div>
    </div>

    <div
      v-if="parsed === undefined"
      class="p-4 font-[JetBrains_Mono_Variable] text-[0.92rem] leading-7 whitespace-pre-wrap text-red-700 dark:text-red-300"
    >
      {{ props.json }}
    </div>

    <div
      v-else-if="viewMode === 'tree'"
      class="max-h-[42rem] overflow-auto px-2 py-3"
    >
      <JSONTreeNode
        :value="parsed"
        path="root"
        :search="search"
        :is-expanded="isExpanded"
        :set-expanded="setExpanded"
      />
    </div>

    <Highlighted
      v-else-if="viewMode === 'pretty'"
      class="max-h-[42rem] overflow-auto"
      file-name="data.json"
      :contents="prettyJson"
      show-copy-button
    />

    <pre
      v-else
      class="max-h-[42rem] overflow-auto p-4 font-[JetBrains_Mono_Variable] text-[0.92rem] leading-7 whitespace-pre-wrap text-neutral-700 dark:text-neutral-200"
    ><code>{{ props.json }}</code></pre>
  </div>
</template>
