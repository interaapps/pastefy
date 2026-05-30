<script setup lang="ts">
import { useTranslation } from 'i18next-vue'
import Button from 'primevue/button'
import Select from 'primevue/select'
import Textarea from 'primevue/textarea'
import { computed } from 'vue'
import { useClipboard, useStorage } from '@vueuse/core'
import UtilityShell from '@/components/tools/utility/UtilityShell.vue'
import UtilityResultActions from '@/components/tools/utility/UtilityResultActions.vue'

const { t } = useTranslation()
const clipboard = useClipboard()
const mode = useStorage<'escape' | 'unescape'>('pastefy-utility-regex-mode', 'escape')
const input = useStorage('pastefy-utility-regex-input', 'https://pastefy.app/tools?tag=markdown')
const modes = [
  { get label() { return t('utility.regexEscapeTool.options.escape') }, value: 'escape' },
  { get label() { return t('utility.regexEscapeTool.options.unescape') }, value: 'unescape' },
]

const output = computed(() =>
  mode.value === 'escape'
    ? input.value.replace(/[.*+?^${}()|[\]\\]/g, '\\$&')
    : input.value.replace(/\\([.*+?^${}()|[\]\\])/g, '$1'),
)
</script>

<template>
  <UtilityShell>
    <template #controls>
      <label class="text-sm font-medium">{{ $t('utility.regexEscapeTool.mode') }}</label>
      <Select v-model="mode" :options="modes" option-label="label" option-value="value" />
      <label class="text-sm font-medium">{{ $t('utility.regexEscapeTool.input') }}</label>
      <Textarea v-model="input" auto-resize rows="10" fluid />
    </template>

    <template #result>
      <div class="rounded-xl border border-neutral-200 bg-white p-4 dark:border-neutral-700 dark:bg-neutral-900">
        <div class="mb-2 flex items-center justify-between gap-3">
          <span class="font-medium">{{ $t('common.output') }}</span>
          <Button @click="clipboard.copy(output)" icon="ti ti-copy" severity="contrast" text />
        </div>
        <code class="block break-all whitespace-pre-wrap text-sm">{{ output }}</code>
      </div>
    </template>

    <template #footer>
      <UtilityResultActions :result="output" file-name="regex-output.txt" />
    </template>
  </UtilityShell>
</template>
