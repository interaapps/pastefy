<script setup lang="ts">
import Button from 'primevue/button'
import InputNumber from 'primevue/inputnumber'
import Select from 'primevue/select'
import { computed, ref, watch } from 'vue'
import { useStorage } from '@vueuse/core'
import UtilityShell from '@/components/tools/utility/UtilityShell.vue'
import UtilityResultActions from '@/components/tools/utility/UtilityResultActions.vue'

const mode = useStorage<'paragraphs' | 'sentences' | 'words'>('pastefy-utility-lorem-mode', 'paragraphs')
const count = useStorage('pastefy-utility-lorem-count', 3)
const output = ref('')

const modeOptions = [
  { label: 'Paragraphs', value: 'paragraphs' },
  { label: 'Sentences', value: 'sentences' },
  { label: 'Words', value: 'words' },
]

const corpus = 'lorem ipsum dolor sit amet consectetur adipiscing elit sed do eiusmod tempor incididunt ut labore et dolore magna aliqua ut enim ad minim veniam quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat'.split(
  ' ',
)

const wordAt = (index: number) => corpus[index % corpus.length]

const generate = () => {
  const target = Math.max(1, Math.min(count.value || 1, 20))
  if (mode.value === 'words') {
    output.value = Array.from({ length: target * 25 }, (_, index) => wordAt(index)).join(' ')
    return
  }

  if (mode.value === 'sentences') {
    output.value = Array.from({ length: target * 4 }, (_, index) => {
      const start = index * 10
      const sentence = Array.from({ length: 10 }, (_, wordIndex) => wordAt(start + wordIndex)).join(' ')
      return `${sentence.charAt(0).toUpperCase()}${sentence.slice(1)}.`
    }).join(' ')
    return
  }

  output.value = Array.from({ length: target }, (_, index) => {
    const start = index * 40
    return Array.from({ length: 4 }, (_, sentenceIndex) => {
      const sentence = Array.from({ length: 10 }, (_, wordIndex) =>
        wordAt(start + sentenceIndex * 10 + wordIndex),
      ).join(' ')
      return `${sentence.charAt(0).toUpperCase()}${sentence.slice(1)}.`
    }).join(' ')
  }).join('\n\n')
}

watch([mode, count], generate, { immediate: true })

const result = computed(() => output.value)
</script>

<template>
  <UtilityShell>
    <template #controls>
      <label class="text-sm font-medium">Mode</label>
      <Select v-model="mode" :options="modeOptions" option-label="label" option-value="value" />
      <label class="text-sm font-medium">Count</label>
      <InputNumber v-model="count" :min="1" :max="20" fluid />
      <div>
        <Button @click="generate()" label="generate again" icon="ti ti-refresh" severity="contrast" outlined />
      </div>
    </template>

    <template #result>
      <div class="rounded-xl border border-neutral-200 bg-white p-4 dark:border-neutral-700 dark:bg-neutral-900">
        <code class="block whitespace-pre-wrap text-sm">{{ output }}</code>
      </div>
    </template>

    <template #footer>
      <UtilityResultActions :result="result" file-name="lorem-ipsum.txt" />
    </template>
  </UtilityShell>
</template>
