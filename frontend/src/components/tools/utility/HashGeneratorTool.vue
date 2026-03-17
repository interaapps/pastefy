<script setup lang="ts">
import Button from 'primevue/button'
import Textarea from 'primevue/textarea'
import { computed, ref, watch } from 'vue'
import { useClipboard, useStorage } from '@vueuse/core'
import UtilityShell from '@/components/tools/utility/UtilityShell.vue'
import UtilityResultActions from '@/components/tools/utility/UtilityResultActions.vue'

const clipboard = useClipboard()
const hashInput = useStorage('pastefy-utility-hash-input', 'Pastefy makes sharing snippets feel fast and polished.')
const hashResults = ref<{ label: string; value: string }[]>([])

const toHex = (buffer: ArrayBuffer) =>
  Array.from(new Uint8Array(buffer))
    .map((byte) => byte.toString(16).padStart(2, '0'))
    .join('')

const calculateHashes = async () => {
  const input = new TextEncoder().encode(hashInput.value)
  if (!window.crypto?.subtle) {
    hashResults.value = []
    return
  }

  hashResults.value = await Promise.all(
    ['SHA-1', 'SHA-256', 'SHA-384', 'SHA-512'].map(async (algorithm) => ({
      label: algorithm,
      value: toHex(await window.crypto.subtle.digest(algorithm, input)),
    })),
  )
}

watch(hashInput, calculateHashes, { immediate: true })

const result = computed(() => hashResults.value.map((entry) => `${entry.label}: ${entry.value}`).join('\n'))
</script>

<template>
  <UtilityShell>
    <template #controls>
      <label class="text-sm font-medium">Input text</label>
      <Textarea v-model="hashInput" auto-resize rows="10" fluid />
      <div class="text-sm text-neutral-500 dark:text-neutral-400">{{ hashInput.length }} characters</div>
    </template>

    <template #result>
      <div class="grid gap-3">
        <div
          v-for="entry of hashResults"
          :key="entry.label"
          class="rounded-xl border border-neutral-200 bg-white p-4 dark:border-neutral-700 dark:bg-neutral-900"
        >
          <div class="mb-2 flex items-center justify-between gap-3">
            <span class="font-medium">{{ entry.label }}</span>
            <Button @click="clipboard.copy(entry.value)" icon="ti ti-copy" severity="contrast" text />
          </div>
          <code class="block break-all text-sm">{{ entry.value }}</code>
        </div>
      </div>
    </template>

    <template #footer>
      <UtilityResultActions :result="result" file-name="hashes.txt" />
    </template>
  </UtilityShell>
</template>
