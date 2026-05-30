<script setup lang="ts">
import { useTranslation } from 'i18next-vue'
import Button from 'primevue/button'
import Message from 'primevue/message'
import Select from 'primevue/select'
import Textarea from 'primevue/textarea'
import { computed } from 'vue'
import { useClipboard, useStorage } from '@vueuse/core'
import UtilityShell from '@/components/tools/utility/UtilityShell.vue'
import UtilityResultActions from '@/components/tools/utility/UtilityResultActions.vue'

const { t } = useTranslation()
const clipboard = useClipboard()
const mode = useStorage<'escape' | 'unescape'>('pastefy-utility-json-string-mode', 'escape')
const input = useStorage('pastefy-utility-json-string-input', '{"title":"Pastefy","content":"Hello\\nWorld"}')
const modes = [
  { get label() { return t('utility.jsonStringTool.options.escape') }, value: 'escape' },
  { get label() { return t('utility.jsonStringTool.options.unescape') }, value: 'unescape' },
]

const state = computed(() => {
  try {
    return {
      value: mode.value === 'escape' ? JSON.stringify(input.value) : JSON.parse(input.value),
      error: undefined,
    }
  } catch {
    return {
      value: '',
      error: 'Input could not be parsed for JSON string unescaping.',
    }
  }
})
</script>

<template>
  <UtilityShell>
    <template #controls>
      <label class="text-sm font-medium">{{ $t('utility.jsonStringTool.mode') }}</label>
      <Select v-model="mode" :options="modes" option-label="label" option-value="value" />
      <label class="text-sm font-medium">{{ $t('utility.jsonStringTool.input') }}</label>
      <Textarea v-model="input" auto-resize rows="10" fluid />
    </template>

    <template #result>
      <Message v-if="state.error" severity="error">{{ state.error }}</Message>
      <div
        v-else
        class="rounded-xl border border-neutral-200 bg-white p-4 dark:border-neutral-700 dark:bg-neutral-900"
      >
        <div class="mb-2 flex items-center justify-between gap-3">
          <span class="font-medium">{{ $t('common.output') }}</span>
          <Button @click="clipboard.copy(String(state.value))" icon="ti ti-copy" severity="contrast" text />
        </div>
        <code class="block break-all whitespace-pre-wrap text-sm">{{ state.value }}</code>
      </div>
    </template>

    <template #footer>
      <UtilityResultActions :result="state.value" file-name="json-string.txt" />
    </template>
  </UtilityShell>
</template>
