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
const mode = useStorage<'encode' | 'decode'>('pastefy-utility-base64-mode', 'encode')
const input = useStorage('pastefy-utility-base64-input', 'Pastefy tool output')

const modeOptions = [
  { label: 'Encode', value: 'encode' },
  { label: 'Decode', value: 'decode' },
]

const encodeBase64 = (value: string) => {
  const bytes = new TextEncoder().encode(value)
  let binary = ''
  bytes.forEach((byte) => {
    binary += String.fromCharCode(byte)
  })
  return btoa(binary)
}

const decodeBase64 = (value: string) => {
  const normalized = value.replace(/-/g, '+').replace(/_/g, '/')
  const padded = normalized.padEnd(Math.ceil(normalized.length / 4) * 4, '=')
  const binary = atob(padded)
  const bytes = Uint8Array.from(binary, (char) => char.charCodeAt(0))
  return new TextDecoder().decode(bytes)
}

const state = computed(() => {
  try {
    if (mode.value === 'encode') {
      const encoded = encodeBase64(input.value)
      return {
        encoded,
        base64url: encoded.replace(/\+/g, '-').replace(/\//g, '_').replace(/=+$/g, ''),
        decoded: '',
        error: undefined,
      }
    }

    return {
      encoded: '',
      base64url: '',
      decoded: decodeBase64(input.value.trim()),
      error: undefined,
    }
  } catch {
    return {
      encoded: '',
      base64url: '',
      decoded: '',
      error: 'Input is not valid Base64 or Base64URL.',
    }
  }
})

const result = computed(() =>
  mode.value === 'encode'
    ? JSON.stringify({ base64: state.value.encoded, base64url: state.value.base64url }, null, 2)
    : state.value.decoded,
)
</script>

<template>
  <UtilityShell>
    <template #controls>
      <label class="text-sm font-medium">Mode</label>
      <Select v-model="mode" :options="modeOptions" option-label="label" option-value="value" />
      <label class="text-sm font-medium">Input</label>
      <Textarea v-model="input" auto-resize rows="10" fluid />
    </template>

    <template #result>
      <Message v-if="state.error" severity="error">{{ state.error }}</Message>
      <div v-else class="grid gap-3">
        <div
          v-if="mode === 'encode'"
          class="rounded-xl border border-neutral-200 bg-white p-4 dark:border-neutral-700 dark:bg-neutral-900"
        >
          <div class="mb-2 flex items-center justify-between gap-3">
            <span class="font-medium">Base64</span>
            <Button @click="clipboard.copy(state.encoded)" icon="ti ti-copy" severity="contrast" text />
          </div>
          <code class="block break-all text-sm">{{ state.encoded }}</code>
        </div>
        <div
          v-if="mode === 'encode'"
          class="rounded-xl border border-neutral-200 bg-white p-4 dark:border-neutral-700 dark:bg-neutral-900"
        >
          <div class="mb-2 flex items-center justify-between gap-3">
            <span class="font-medium">Base64URL</span>
            <Button @click="clipboard.copy(state.base64url)" icon="ti ti-copy" severity="contrast" text />
          </div>
          <code class="block break-all text-sm">{{ state.base64url }}</code>
        </div>
        <div
          v-if="mode === 'decode'"
          class="rounded-xl border border-neutral-200 bg-white p-4 dark:border-neutral-700 dark:bg-neutral-900"
        >
          <div class="mb-2 flex items-center justify-between gap-3">
            <span class="font-medium">Decoded</span>
            <Button @click="clipboard.copy(state.decoded)" icon="ti ti-copy" severity="contrast" text />
          </div>
          <code class="block break-all whitespace-pre-wrap text-sm">{{ state.decoded }}</code>
        </div>
      </div>
    </template>

    <template #footer>
      <UtilityResultActions :result="result" :file-name="mode === 'encode' ? 'base64-output.json' : 'decoded.txt'" />
    </template>
  </UtilityShell>
</template>
