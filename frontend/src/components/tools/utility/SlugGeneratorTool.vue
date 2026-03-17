<script setup lang="ts">
import Button from 'primevue/button'
import Textarea from 'primevue/textarea'
import { computed } from 'vue'
import { useClipboard, useStorage } from '@vueuse/core'
import UtilityShell from '@/components/tools/utility/UtilityShell.vue'
import UtilityResultActions from '@/components/tools/utility/UtilityResultActions.vue'

const clipboard = useClipboard()
const input = useStorage('pastefy-utility-slug-input', 'Pastefy makes snippet sharing feel fast & elegant')

const state = computed(() => {
  const words = input.value
    .trim()
    .normalize('NFKD')
    .replace(/[\u0300-\u036f]/g, '')
    .replace(/[^a-zA-Z0-9]+/g, ' ')
    .trim()
    .split(/\s+/)
    .filter(Boolean)
    .map((word) => word.toLowerCase())

  return {
    kebab: words.join('-'),
    snake: words.join('_'),
    constant: words.join('_').toUpperCase(),
    camel: words
      .map((word, index) =>
        index === 0 ? word : `${word.charAt(0).toUpperCase()}${word.slice(1)}`,
      )
      .join(''),
    pascal: words.map((word) => `${word.charAt(0).toUpperCase()}${word.slice(1)}`).join(''),
  }
})

const result = computed(() => JSON.stringify(state.value, null, 2))
</script>

<template>
  <UtilityShell>
    <template #controls>
      <label class="text-sm font-medium">Source text</label>
      <Textarea v-model="input" auto-resize rows="8" fluid />
    </template>

    <template #result>
      <div class="grid gap-3">
        <div
          v-for="entry of [
            ['kebab-case', state.kebab],
            ['snake_case', state.snake],
            ['CONSTANT_CASE', state.constant],
            ['camelCase', state.camel],
            ['PascalCase', state.pascal],
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
      <UtilityResultActions :result="result" file-name="slug-output.json" />
    </template>
  </UtilityShell>
</template>
