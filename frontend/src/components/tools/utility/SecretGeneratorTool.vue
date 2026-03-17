<script setup lang="ts">
import Button from 'primevue/button'
import InputNumber from 'primevue/inputnumber'
import Select from 'primevue/select'
import { computed, ref, watch } from 'vue'
import { useClipboard, useStorage } from '@vueuse/core'
import UtilityShell from '@/components/tools/utility/UtilityShell.vue'
import UtilityResultActions from '@/components/tools/utility/UtilityResultActions.vue'

const clipboard = useClipboard()
const mode = useStorage<'uuid' | 'hex' | 'base64url'>('pastefy-utility-secret-mode', 'uuid')
const count = useStorage('pastefy-utility-secret-count', 3)
const length = useStorage('pastefy-utility-secret-length', 32)
const generatedValues = ref<string[]>([])

const modeOptions = [
  { label: 'UUID', value: 'uuid' },
  { label: 'Hex token', value: 'hex' },
  { label: 'Base64URL token', value: 'base64url' },
]

const randomChars = (charset: string, targetLength: number) => {
  const values = new Uint32Array(targetLength)
  window.crypto.getRandomValues(values)
  return Array.from(values, (value) => charset[value % charset.length]).join('')
}

const encodeBase64Url = (bytes: Uint8Array) =>
  btoa(String.fromCharCode(...bytes))
    .replace(/\+/g, '-')
    .replace(/\//g, '_')
    .replace(/=+$/g, '')

const generateValues = () => {
  const targetCount = Math.max(1, Math.min(count.value || 1, 25))
  const targetLength = Math.max(8, Math.min(length.value || 32, 256))

  generatedValues.value = Array.from({ length: targetCount }, () => {
    if (mode.value === 'uuid') return window.crypto.randomUUID()
    if (mode.value === 'hex') return randomChars('0123456789abcdef', targetLength)
    const bytes = new Uint8Array(Math.ceil((targetLength * 3) / 4))
    window.crypto.getRandomValues(bytes)
    return encodeBase64Url(bytes).slice(0, targetLength)
  })
}

watch([mode, count, length], generateValues, { immediate: true })

const result = computed(() => generatedValues.value.join('\n'))
</script>

<template>
  <UtilityShell>
    <template #controls>
      <label class="text-sm font-medium">Mode</label>
      <Select v-model="mode" :options="modeOptions" option-label="label" option-value="value" />
      <label class="text-sm font-medium">Count</label>
      <InputNumber v-model="count" :min="1" :max="25" fluid />
      <label class="text-sm font-medium">Length</label>
      <InputNumber v-model="length" :min="8" :max="256" fluid />
      <div>
        <Button @click="generateValues()" label="generate again" icon="ti ti-refresh" severity="contrast" outlined />
      </div>
    </template>

    <template #result>
      <div class="grid gap-3">
        <div
          v-for="(entry, index) of generatedValues"
          :key="entry"
          class="rounded-xl border border-neutral-200 bg-white p-4 dark:border-neutral-700 dark:bg-neutral-900"
        >
          <div class="mb-2 flex items-center justify-between gap-3">
            <span class="font-medium">Secret {{ index + 1 }}</span>
            <Button @click="clipboard.copy(entry)" icon="ti ti-copy" severity="contrast" text />
          </div>
          <code class="block break-all text-sm">{{ entry }}</code>
        </div>
      </div>
    </template>

    <template #footer>
      <UtilityResultActions :result="result" file-name="generated-secrets.txt" />
    </template>
  </UtilityShell>
</template>
