<script setup lang="ts">
import Button from 'primevue/button'
import Textarea from 'primevue/textarea'
import { computed } from 'vue'
import { useClipboard, useStorage } from '@vueuse/core'
import Highlighted from '@/components/Highlighted.vue'
import UtilityShell from '@/components/tools/utility/UtilityShell.vue'
import UtilityResultActions from '@/components/tools/utility/UtilityResultActions.vue'

const clipboard = useClipboard()
const input = useStorage('pastefy-utility-query-input', 'page=1&sort=created_at&tag=markdown&tag=json')

const entries = computed(() => {
  const params = new URLSearchParams(input.value.startsWith('?') ? input.value.slice(1) : input.value)
  return Array.from(params.entries())
})

const grouped = computed(() => {
  const output: Record<string, string[]> = {}
  entries.value.forEach(([key, value]) => {
    output[key] ||= []
    output[key].push(value)
  })
  return output
})

const rebuilt = computed(() => new URLSearchParams(entries.value).toString())
const result = computed(() =>
  JSON.stringify(
    {
      entries: entries.value.map(([key, value]) => ({ key, value })),
      grouped: grouped.value,
      rebuilt: rebuilt.value,
    },
    null,
    2,
  ),
)
</script>

<template>
  <UtilityShell>
    <template #controls>
      <label class="text-sm font-medium">Query string</label>
      <Textarea v-model="input" auto-resize rows="8" fluid />
    </template>

    <template #result>
      <div class="mb-4 rounded-xl border border-neutral-200 bg-white p-4 dark:border-neutral-700 dark:bg-neutral-900">
        <div class="mb-2 flex items-center justify-between gap-3">
          <span class="font-medium">Rebuilt</span>
          <Button @click="clipboard.copy(rebuilt)" icon="ti ti-copy" severity="contrast" text />
        </div>
        <code class="block break-all text-sm">{{ rebuilt }}</code>
      </div>

      <div class="overflow-hidden rounded-xl border border-neutral-200 bg-white dark:border-neutral-700 dark:bg-neutral-900">
        <div class="border-b border-neutral-200 px-4 py-2 text-sm font-medium dark:border-neutral-700">Parsed</div>
        <Highlighted :contents="JSON.stringify(grouped, null, 2)" file-name="query.json" />
      </div>
    </template>

    <template #footer>
      <UtilityResultActions :result="result" file-name="query-params.json" />
    </template>
  </UtilityShell>
</template>
