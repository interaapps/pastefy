<script setup lang="ts">
import Button from 'primevue/button'
import Checkbox from 'primevue/checkbox'
import InputNumber from 'primevue/inputnumber'
import Message from 'primevue/message'
import { computed, ref, watch } from 'vue'
import { useClipboard, useStorage } from '@vueuse/core'
import UtilityShell from '@/components/tools/utility/UtilityShell.vue'
import UtilityResultActions from '@/components/tools/utility/UtilityResultActions.vue'

const clipboard = useClipboard()
const count = useStorage('pastefy-utility-password-count', 4)
const length = useStorage('pastefy-utility-password-length', 20)
const includeUppercase = useStorage('pastefy-utility-password-uppercase', true)
const includeLowercase = useStorage('pastefy-utility-password-lowercase', true)
const includeNumbers = useStorage('pastefy-utility-password-numbers', true)
const includeSymbols = useStorage('pastefy-utility-password-symbols', true)
const excludeAmbiguous = useStorage('pastefy-utility-password-ambiguous', true)
const generatedPasswords = ref<string[]>([])

const buildCharset = () => {
  let charset = ''
  if (includeUppercase.value) charset += 'ABCDEFGHJKLMNPQRSTUVWXYZ'
  if (includeLowercase.value) charset += 'abcdefghijkmnopqrstuvwxyz'
  if (includeNumbers.value) charset += '23456789'
  if (includeSymbols.value) charset += '!@#$%^&*()-_=+[]{};:,.?'

  if (!excludeAmbiguous.value) {
    if (includeUppercase.value) charset += 'IO'
    if (includeLowercase.value) charset += 'l'
    if (includeNumbers.value) charset += '01'
  }

  return charset
}

const randomChars = (charset: string, targetLength: number) => {
  const values = new Uint32Array(targetLength)
  window.crypto.getRandomValues(values)
  return Array.from(values, (value) => charset[value % charset.length]).join('')
}

const generatePasswords = () => {
  const charset = buildCharset()
  if (!charset) {
    generatedPasswords.value = []
    return
  }

  const targetCount = Math.max(1, Math.min(count.value || 1, 25))
  const targetLength = Math.max(8, Math.min(length.value || 20, 128))
  generatedPasswords.value = Array.from({ length: targetCount }, () =>
    randomChars(charset, targetLength),
  )
}

watch(
  [count, length, includeUppercase, includeLowercase, includeNumbers, includeSymbols, excludeAmbiguous],
  generatePasswords,
  { immediate: true },
)

const result = computed(() => generatedPasswords.value.join('\n'))
</script>

<template>
  <UtilityShell>
    <template #controls>
      <label class="text-sm font-medium">Count</label>
      <InputNumber v-model="count" :min="1" :max="25" fluid />
      <label class="text-sm font-medium">Length</label>
      <InputNumber v-model="length" :min="8" :max="128" fluid />

      <label class="flex items-center gap-2 text-sm">
        <Checkbox v-model="includeUppercase" binary />
        Uppercase letters
      </label>
      <label class="flex items-center gap-2 text-sm">
        <Checkbox v-model="includeLowercase" binary />
        Lowercase letters
      </label>
      <label class="flex items-center gap-2 text-sm">
        <Checkbox v-model="includeNumbers" binary />
        Numbers
      </label>
      <label class="flex items-center gap-2 text-sm">
        <Checkbox v-model="includeSymbols" binary />
        Symbols
      </label>
      <label class="flex items-center gap-2 text-sm">
        <Checkbox v-model="excludeAmbiguous" binary />
        Exclude ambiguous characters
      </label>

      <div>
        <Button @click="generatePasswords()" label="generate again" icon="ti ti-refresh" severity="contrast" outlined />
      </div>
    </template>

    <template #result>
      <Message
        v-if="!generatedPasswords.length"
        severity="warn"
      >
        Select at least one character group to generate passwords.
      </Message>
      <div v-else class="grid gap-3">
        <div
          v-for="(entry, index) of generatedPasswords"
          :key="entry"
          class="rounded-xl border border-neutral-200 bg-white p-4 dark:border-neutral-700 dark:bg-neutral-900"
        >
          <div class="mb-2 flex items-center justify-between gap-3">
            <span class="font-medium">Password {{ index + 1 }}</span>
            <Button @click="clipboard.copy(entry)" icon="ti ti-copy" severity="contrast" text />
          </div>
          <code class="block break-all text-sm">{{ entry }}</code>
        </div>
      </div>
    </template>

    <template #footer>
      <UtilityResultActions :result="result" file-name="generated-passwords.txt" />
    </template>
  </UtilityShell>
</template>
