<script setup lang="ts">
import Button from 'primevue/button'
import Select from 'primevue/select'
import Textarea from 'primevue/textarea'
import { computed } from 'vue'
import { useClipboard, useStorage } from '@vueuse/core'
import UtilityShell from '@/components/tools/utility/UtilityShell.vue'
import UtilityResultActions from '@/components/tools/utility/UtilityResultActions.vue'

const clipboard = useClipboard()
const mode = useStorage<'encode' | 'decode'>('pastefy-utility-html-entity-mode', 'encode')
const input = useStorage('pastefy-utility-html-entity-input', '<div class="card">Pastefy & tools</div>')
const modes = [
  { label: 'Encode', value: 'encode' },
  { label: 'Decode', value: 'decode' },
]

const state = computed(() => {
  if (typeof document === 'undefined') return { value: input.value }

  if (mode.value === 'encode') {
    const el = document.createElement('div')
    el.textContent = input.value
    return { value: el.innerHTML }
  }

  const el = document.createElement('textarea')
  el.innerHTML = input.value
  return { value: el.value }
})
</script>

<template>
  <UtilityShell>
    <template #controls>
      <label class="text-sm font-medium">Mode</label>
      <Select v-model="mode" :options="modes" option-label="label" option-value="value" />
      <label class="text-sm font-medium">Input</label>
      <Textarea v-model="input" auto-resize rows="10" fluid />
    </template>

    <template #result>
      <div class="rounded-xl border border-neutral-200 bg-white p-4 dark:border-neutral-700 dark:bg-neutral-900">
        <div class="mb-2 flex items-center justify-between gap-3">
          <span class="font-medium">{{ mode === 'encode' ? 'Encoded' : 'Decoded' }}</span>
          <Button @click="clipboard.copy(state.value)" icon="ti ti-copy" severity="contrast" text />
        </div>
        <code class="block break-all whitespace-pre-wrap text-sm">{{ state.value }}</code>
      </div>
    </template>

    <template #footer>
      <UtilityResultActions :result="state.value" :file-name="mode === 'encode' ? 'html-entities.txt' : 'decoded-html.txt'" />
    </template>
  </UtilityShell>
</template>
