<script setup lang="ts">
import Button from 'primevue/button'
import Textarea from 'primevue/textarea'
import { computed } from 'vue'
import { useClipboard, useStorage } from '@vueuse/core'
import UtilityShell from '@/components/tools/utility/UtilityShell.vue'
import UtilityResultActions from '@/components/tools/utility/UtilityResultActions.vue'

const clipboard = useClipboard()
const input = useStorage('pastefy-utility-case-input', 'Pastefy makes developer tooling feel polished.')

const words = computed(() =>
  input.value
    .normalize('NFKD')
    .replace(/[\u0300-\u036f]/g, '')
    .replace(/[^a-zA-Z0-9]+/g, ' ')
    .trim()
    .split(/\s+/)
    .filter(Boolean),
)

const state = computed(() => {
  const lower = input.value.toLowerCase()
  const upper = input.value.toUpperCase()
  const sentence = input.value
    ? `${input.value.charAt(0).toUpperCase()}${input.value.slice(1).toLowerCase()}`
    : ''
  const title = words.value
    .map((word) => `${word.charAt(0).toUpperCase()}${word.slice(1).toLowerCase()}`)
    .join(' ')
  const camel = words.value
    .map((word, index) =>
      index === 0
        ? word.toLowerCase()
        : `${word.charAt(0).toUpperCase()}${word.slice(1).toLowerCase()}`,
    )
    .join('')
  const pascal = words.value
    .map((word) => `${word.charAt(0).toUpperCase()}${word.slice(1).toLowerCase()}`)
    .join('')
  const snake = words.value.map((word) => word.toLowerCase()).join('_')
  const kebab = words.value.map((word) => word.toLowerCase()).join('-')

  return { lower, upper, sentence, title, camel, pascal, snake, kebab }
})

const result = computed(() => JSON.stringify(state.value, null, 2))
</script>

<template>
  <UtilityShell>
    <template #controls>
      <label class="text-sm font-medium">Source text</label>
      <Textarea v-model="input" auto-resize rows="10" fluid />
    </template>

    <template #result>
      <div class="grid gap-3">
        <div
          v-for="entry of [
            ['lowercase', state.lower],
            ['UPPERCASE', state.upper],
            ['Sentence case', state.sentence],
            ['Title Case', state.title],
            ['camelCase', state.camel],
            ['PascalCase', state.pascal],
            ['snake_case', state.snake],
            ['kebab-case', state.kebab],
          ]"
          :key="entry[0]"
          class="rounded-xl border border-neutral-200 bg-white p-4 dark:border-neutral-700 dark:bg-neutral-900"
        >
          <div class="mb-2 flex items-center justify-between gap-3">
            <span class="font-medium">{{ entry[0] }}</span>
            <Button @click="clipboard.copy(String(entry[1]))" icon="ti ti-copy" severity="contrast" text />
          </div>
          <code class="block break-all text-sm">{{ entry[1] }}</code>
        </div>
      </div>
    </template>

    <template #footer>
      <UtilityResultActions :result="result" file-name="case-conversion.json" />
    </template>
  </UtilityShell>
</template>
