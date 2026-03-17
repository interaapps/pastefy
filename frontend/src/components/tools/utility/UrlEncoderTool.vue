<script setup lang="ts">
import Button from 'primevue/button'
import Message from 'primevue/message'
import Select from 'primevue/select'
import Textarea from 'primevue/textarea'
import { computed } from 'vue'
import { useClipboard, useStorage } from '@vueuse/core'
import UtilityShell from '@/components/tools/utility/UtilityShell.vue'
import UtilityResultActions from '@/components/tools/utility/UtilityResultActions.vue'

const clipboard = useClipboard()
const mode = useStorage<'encode' | 'decode'>('pastefy-utility-url-mode', 'encode')
const input = useStorage('pastefy-utility-url-input', 'https://pastefy.app/tools?tab=utilities&query=hello world')

const modeOptions = [
  { label: 'Encode', value: 'encode' },
  { label: 'Decode', value: 'decode' },
]

const state = computed(() => {
  try {
    return {
      value: mode.value === 'encode' ? encodeURIComponent(input.value) : decodeURIComponent(input.value),
      error: undefined,
    }
  } catch {
    return {
      value: '',
      error: 'Input could not be decoded as a valid encoded URL component.',
    }
  }
})
</script>

<template>
  <UtilityShell>
    <template #controls>
      <label class="text-sm font-medium">Mode</label>
      <Select v-model="mode" :options="modeOptions" option-label="label" option-value="value" />
      <label class="text-sm font-medium">Input</label>
      <Textarea v-model="input" auto-resize rows="8" fluid />
    </template>

    <template #result>
      <Message v-if="state.error" severity="error">{{ state.error }}</Message>
      <div
        v-else
        class="rounded-xl border border-neutral-200 bg-white p-4 dark:border-neutral-700 dark:bg-neutral-900"
      >
        <div class="mb-2 flex items-center justify-between gap-3">
          <span class="font-medium">{{ mode === 'encode' ? 'Encoded' : 'Decoded' }}</span>
          <Button @click="clipboard.copy(state.value)" icon="ti ti-copy" severity="contrast" text />
        </div>
        <code class="block break-all whitespace-pre-wrap text-sm">{{ state.value }}</code>
      </div>
    </template>

    <template #footer>
      <UtilityResultActions :result="state.value" :file-name="mode === 'encode' ? 'url-encoded.txt' : 'url-decoded.txt'" />
    </template>
  </UtilityShell>
</template>
