<script setup lang="ts">
import Checkbox from 'primevue/checkbox'
import Textarea from 'primevue/textarea'
import { computed } from 'vue'
import { useStorage } from '@vueuse/core'
import UtilityShell from '@/components/tools/utility/UtilityShell.vue'
import UtilityResultActions from '@/components/tools/utility/UtilityResultActions.vue'

const input = useStorage(
  'pastefy-utility-sorter-input',
  'redis\npostgres\npastefy\nredis\nnginx\napi',
)
const dedupe = useStorage('pastefy-utility-sorter-dedupe', true)
const reverse = useStorage('pastefy-utility-sorter-reverse', false)
const caseSensitive = useStorage('pastefy-utility-sorter-case', false)

const output = computed(() => {
  let lines = input.value.split(/\r?\n/).filter((line) => line !== '')
  if (dedupe.value) lines = Array.from(new Set(lines))
  lines = lines.sort((a, b) =>
    caseSensitive.value ? a.localeCompare(b) : a.toLowerCase().localeCompare(b.toLowerCase()),
  )
  if (reverse.value) lines.reverse()
  return lines.join('\n')
})
</script>

<template>
  <UtilityShell>
    <template #controls>
      <label class="text-sm font-medium">Lines</label>
      <Textarea v-model="input" auto-resize rows="12" fluid />
      <label class="flex items-center gap-2 text-sm">
        <Checkbox v-model="dedupe" binary />
        Remove duplicates
      </label>
      <label class="flex items-center gap-2 text-sm">
        <Checkbox v-model="reverse" binary />
        Reverse order
      </label>
      <label class="flex items-center gap-2 text-sm">
        <Checkbox v-model="caseSensitive" binary />
        Case-sensitive sort
      </label>
    </template>

    <template #result>
      <div class="rounded-xl border border-neutral-200 bg-white p-4 dark:border-neutral-700 dark:bg-neutral-900">
        <code class="block whitespace-pre-wrap text-sm">{{ output }}</code>
      </div>
    </template>

    <template #footer>
      <UtilityResultActions :result="output" file-name="sorted-lines.txt" />
    </template>
  </UtilityShell>
</template>
