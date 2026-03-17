<script setup lang="ts">
import { computed, defineAsyncComponent, ref } from 'vue'
import { useClipboard } from '@vueuse/core'
import Button from 'primevue/button'
import InputText from 'primevue/inputtext'
import Tag from 'primevue/tag'
const Highlighted = defineAsyncComponent(() => import('@/components/Highlighted.vue'))

const props = defineProps<{
  contents: string
  fileName?: string
  type: 'yaml' | 'toml' | 'properties' | 'ini'
}>()

const clipboard = useClipboard()
const search = ref('')

const lines = computed(() =>
  props.contents.split('\n').map((line, index) => {
    const trimmed = line.trim()
    const commentPrefixes =
      props.type === 'ini' || props.type === 'properties' ? ['#', ';'] : ['#']

    const isComment = commentPrefixes.some((prefix) => trimmed.startsWith(prefix))
    const isSection =
      props.type === 'ini'
        ? /^\[[^\]]+\]$/.test(trimmed)
        : props.type === 'toml'
          ? /^\[\[?.+\]?\]$/.test(trimmed)
          : false

    const separatorIndex = trimmed.indexOf('=')
    const yamlSeparatorIndex = trimmed.indexOf(':')

    let key = ''
    let value = ''

    if (!isComment && !isSection && separatorIndex > -1) {
      key = trimmed.slice(0, separatorIndex).trim()
      value = trimmed.slice(separatorIndex + 1).trim()
    } else if (!isComment && !isSection && props.type === 'yaml' && yamlSeparatorIndex > -1) {
      key = trimmed.slice(0, yamlSeparatorIndex).trim()
      value = trimmed.slice(yamlSeparatorIndex + 1).trim()
    }

    return {
      index,
      line,
      trimmed,
      key,
      value,
      isComment,
      isSection,
    }
  }),
)

const filteredLines = computed(() => {
  const needle = search.value.trim().toLowerCase()
  if (!needle) return lines.value
  return lines.value.filter((entry) =>
    [entry.trimmed, entry.key, entry.value].join(' ').toLowerCase().includes(needle),
  )
})

const stats = computed(() => ({
  total: lines.value.length,
  sections: lines.value.filter((entry) => entry.isSection).length,
  comments: lines.value.filter((entry) => entry.isComment).length,
  keys: lines.value.filter((entry) => entry.key).length,
}))

const exportConfig = () => {
  const blob = new Blob([props.contents], { type: 'text/plain;charset=utf-8' })
  const objectUrl = URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = objectUrl
  link.download = props.fileName || `pastefy-export.${props.type}`
  link.click()
  setTimeout(() => URL.revokeObjectURL(objectUrl), 1000)
}
</script>

<template>
  <div class="overflow-hidden text-sm">
    <div
      class="flex flex-col gap-4 border-b border-neutral-200 bg-neutral-50 px-4 py-4 dark:border-neutral-700 dark:bg-neutral-900/70"
    >
      <div class="flex flex-wrap items-center justify-between gap-3">
        <div>
          <div class="text-xs font-semibold tracking-[0.2em] uppercase opacity-60">
            {{ type }} View
          </div>
          <div class="mt-1 text-xs opacity-60">Config explorer with search and export</div>
        </div>
        <div class="flex flex-wrap gap-2">
          <Tag :value="`${stats.keys} keys`" severity="contrast" />
          <Tag :value="`${stats.sections} sections`" severity="contrast" />
          <Tag :value="`${stats.comments} comments`" severity="contrast" />
        </div>
      </div>

      <div class="grid gap-3 xl:grid-cols-[minmax(0,1fr)_auto]">
        <InputText v-model="search" placeholder="Search sections, keys, and values" fluid />
        <div class="flex flex-wrap gap-2">
          <Button
            icon="ti ti-copy"
            label="Copy"
            size="small"
            severity="contrast"
            outlined
            @click="clipboard.copy(contents)"
          />
          <Button
            icon="ti ti-download"
            label="Export"
            size="small"
            severity="contrast"
            outlined
            @click="exportConfig"
          />
        </div>
      </div>
    </div>

    <div class="max-h-[22rem] overflow-auto border-b border-neutral-200 dark:border-neutral-700">
      <div v-if="filteredLines.length === 0" class="p-4 text-sm opacity-60">No matching lines.</div>
      <div
        v-for="entry in filteredLines"
        :key="entry.index"
        class="grid grid-cols-[auto_minmax(0,1fr)] gap-3 border-b border-neutral-100 px-4 py-2 font-[JetBrains_Mono_Variable] text-[0.9rem] last:border-b-0 dark:border-neutral-800"
      >
        <span class="text-right text-xs opacity-40">{{ entry.index + 1 }}</span>
        <div class="min-w-0">
          <span v-if="entry.isSection" class="font-semibold text-sky-700 dark:text-sky-300">
            {{ entry.line }}
          </span>
          <span v-else-if="entry.isComment" class="opacity-50">{{ entry.line }}</span>
          <template v-else-if="entry.key">
            <span class="text-amber-700 dark:text-amber-300">{{ entry.key }}</span>
            <span class="opacity-50"> = </span>
            <span class="break-all text-emerald-700 dark:text-emerald-300">{{ entry.value }}</span>
          </template>
          <span v-else class="break-all">{{ entry.line }}</span>
        </div>
      </div>
    </div>

    <Highlighted
      class="max-h-[20rem] overflow-auto"
      :file-name="fileName || `config.${type}`"
      :contents="contents"
      show-copy-button
    />
  </div>
</template>
