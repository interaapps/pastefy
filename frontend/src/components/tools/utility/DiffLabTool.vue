<script setup lang="ts">
import Button from 'primevue/button'
import InputText from 'primevue/inputtext'
import Select from 'primevue/select'
import Textarea from 'primevue/textarea'
import { computed } from 'vue'
import { useStorage } from '@vueuse/core'
import { createTwoFilesPatch, diffChars, diffLines, diffWords } from 'diff'

import DiffViewer from '@/components/previews/DiffViewer.vue'
import UtilityResultActions from '@/components/tools/utility/UtilityResultActions.vue'
import UtilityShell from '@/components/tools/utility/UtilityShell.vue'

type DiffMode = 'lines' | 'words' | 'chars'

const left = useStorage('pastefy-utility-diff-left', 'hello\nworld\nold line')
const right = useStorage('pastefy-utility-diff-right', 'hello\nPastefy\nnew line')
const leftName = useStorage('pastefy-utility-diff-left-name', 'before.txt')
const rightName = useStorage('pastefy-utility-diff-right-name', 'after.txt')
const mode = useStorage<DiffMode>('pastefy-utility-diff-mode', 'lines')

const modeOptions = [
  { label: 'Line diff', value: 'lines' as DiffMode },
  { label: 'Word diff', value: 'words' as DiffMode },
  { label: 'Character diff', value: 'chars' as DiffMode },
]

const patch = computed(() =>
  createTwoFilesPatch(leftName.value, rightName.value, left.value, right.value),
)

const stats = computed(() => {
  const changes =
    mode.value === 'words'
      ? diffWords(left.value, right.value)
      : mode.value === 'chars'
        ? diffChars(left.value, right.value)
        : diffLines(left.value, right.value)

  return changes.reduce(
    (
      acc: { added: number; removed: number; unchanged: number },
      entry: { value: string; added?: boolean; removed?: boolean; count?: number },
    ) => {
      const length = entry.count || entry.value.length
      if (entry.added) acc.added += length
      else if (entry.removed) acc.removed += length
      else acc.unchanged += length
      return acc
    },
    { added: 0, removed: 0, unchanged: 0 },
  )
})
</script>

<template>
  <UtilityShell
    control-title="Compare Text"
    control-description="Diff any two texts locally and inspect the generated unified patch."
    result-title="Diff"
    result-description="Rendered diff preview plus reusable patch output."
  >
    <template #controls>
      <div class="grid gap-3 md:grid-cols-3">
        <InputText v-model="leftName" fluid placeholder="before.txt" />
        <InputText v-model="rightName" fluid placeholder="after.txt" />
        <Select v-model="mode" :options="modeOptions" option-label="label" option-value="value" fluid />
      </div>
      <div class="grid gap-3 md:grid-cols-2">
        <div>
          <label class="mb-1 block text-sm text-neutral-500 dark:text-neutral-400">Before</label>
          <Textarea v-model="left" auto-resize rows="18" fluid />
        </div>
        <div>
          <label class="mb-1 block text-sm text-neutral-500 dark:text-neutral-400">After</label>
          <Textarea v-model="right" auto-resize rows="18" fluid />
        </div>
      </div>
    </template>
    <template #result>
      <div class="grid gap-3 md:grid-cols-3">
        <div
          v-for="entry of [['Added', stats.added], ['Removed', stats.removed], ['Unchanged', stats.unchanged]]"
          :key="entry[0]"
          class="rounded-xl border border-neutral-200 bg-white p-4 dark:border-neutral-700 dark:bg-neutral-900"
        >
          <div class="text-sm text-neutral-500 dark:text-neutral-400">{{ entry[0] }}</div>
          <div class="mt-2 text-xl font-semibold">{{ entry[1] }}</div>
        </div>
      </div>
      <div class="overflow-hidden rounded-xl border border-neutral-200 bg-white dark:border-neutral-700 dark:bg-neutral-900">
        <DiffViewer :diff="patch" in-editor />
      </div>
    </template>
    <template #footer>
      <UtilityResultActions :result="patch" file-name="changes.diff" />
    </template>
  </UtilityShell>
</template>
