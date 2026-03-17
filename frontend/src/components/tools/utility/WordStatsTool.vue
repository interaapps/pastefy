<script setup lang="ts">
import Textarea from 'primevue/textarea'
import { computed } from 'vue'
import { useStorage } from '@vueuse/core'
import UtilityShell from '@/components/tools/utility/UtilityShell.vue'
import UtilityResultActions from '@/components/tools/utility/UtilityResultActions.vue'

const input = useStorage(
  'pastefy-utility-word-stats-input',
  `Pastefy gives you a fast place to shape content before you share it.

This tool counts much more than just words. It also shows reading time, sentence and paragraph counts, average word length, and the most common words in your text.`,
)

const words = computed(() => input.value.match(/\b[\p{L}\p{N}'-]+\b/gu) || [])
const lines = computed(() => (input.value === '' ? 0 : input.value.split(/\r?\n/).length))
const paragraphs = computed(() =>
  input.value.trim() === '' ? 0 : input.value.trim().split(/\n\s*\n/).filter(Boolean).length,
)
const sentences = computed(() => input.value.split(/[.!?]+/).map((entry) => entry.trim()).filter(Boolean).length)
const characters = computed(() => input.value.length)
const charactersNoSpaces = computed(() => input.value.replace(/\s/g, '').length)
const uniqueWords = computed(() => new Set(words.value.map((word) => word.toLowerCase())).size)
const averageWordLength = computed(() =>
  words.value.length
    ? (words.value.reduce((sum, word) => sum + word.length, 0) / words.value.length).toFixed(1)
    : '0.0',
)
const readingTimeMinutes = computed(() =>
  words.value.length ? Math.max(1, Math.ceil(words.value.length / 200)) : 0,
)
const longestWord = computed(() =>
  words.value.reduce((longest, word) => (word.length > longest.length ? word : longest), ''),
)

const topWords = computed(() => {
  const frequencies = new Map<string, number>()
  words.value
    .map((word) => word.toLowerCase())
    .filter((word) => word.length > 2)
    .forEach((word) => {
      frequencies.set(word, (frequencies.get(word) || 0) + 1)
    })

  return Array.from(frequencies.entries())
    .sort((a, b) => b[1] - a[1] || a[0].localeCompare(b[0]))
    .slice(0, 8)
})

const result = computed(() =>
  JSON.stringify(
    {
      words: words.value.length,
      uniqueWords: uniqueWords.value,
      characters: characters.value,
      charactersNoSpaces: charactersNoSpaces.value,
      lines: lines.value,
      paragraphs: paragraphs.value,
      sentences: sentences.value,
      averageWordLength: Number(averageWordLength.value),
      readingTimeMinutes: readingTimeMinutes.value,
      longestWord: longestWord.value,
      topWords: topWords.value.map(([word, count]) => ({ word, count })),
    },
    null,
    2,
  ),
)
</script>

<template>
  <UtilityShell
    control-title="Text"
    control-description="Paste or write text to inspect counts and useful writing metrics."
    result-title="Stats"
    result-description="Detailed counts and heuristics for the current text."
  >
    <template #controls>
      <label class="text-sm font-medium">Text input</label>
      <Textarea v-model="input" auto-resize rows="14" fluid />
    </template>

    <template #result>
      <div class="grid gap-3 md:grid-cols-2">
        <div
          v-for="entry of [
            ['Words', words.length],
            ['Unique words', uniqueWords],
            ['Characters', characters],
            ['Characters (no spaces)', charactersNoSpaces],
            ['Lines', lines],
            ['Paragraphs', paragraphs],
            ['Sentences', sentences],
            ['Avg. word length', averageWordLength],
            ['Reading time (min)', readingTimeMinutes],
            ['Longest word', longestWord || '—'],
          ]"
          :key="entry[0]"
          class="rounded-xl border border-neutral-200 bg-white p-4 dark:border-neutral-700 dark:bg-neutral-900"
        >
          <div class="text-sm text-neutral-500 dark:text-neutral-400">{{ entry[0] }}</div>
          <div class="mt-2 text-xl font-semibold break-all">{{ entry[1] }}</div>
        </div>
      </div>

      <div class="mt-4 rounded-xl border border-neutral-200 bg-white p-4 dark:border-neutral-700 dark:bg-neutral-900">
        <div class="mb-3 text-sm font-medium">Most common words</div>
        <div v-if="topWords.length" class="flex flex-wrap gap-2">
          <span
            v-for="[word, count] of topWords"
            :key="word"
            class="rounded-full border border-neutral-200 px-3 py-1 text-sm dark:border-neutral-700"
          >
            {{ word }} · {{ count }}
          </span>
        </div>
        <div v-else class="text-sm text-neutral-500 dark:text-neutral-400">
          Add more text to see common words.
        </div>
      </div>
    </template>

    <template #footer>
      <UtilityResultActions :result="result" file-name="text-stats.json" />
    </template>
  </UtilityShell>
</template>
