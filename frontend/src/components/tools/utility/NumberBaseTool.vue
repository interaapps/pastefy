<script setup lang="ts">
import { useTranslation } from 'i18next-vue'
import InputText from 'primevue/inputtext'
import Message from 'primevue/message'
import Select from 'primevue/select'
import { computed } from 'vue'
import { useStorage } from '@vueuse/core'
import UtilityShell from '@/components/tools/utility/UtilityShell.vue'
import UtilityResultActions from '@/components/tools/utility/UtilityResultActions.vue'

const { t } = useTranslation()
const input = useStorage('pastefy-utility-number-base-input', '255')
const base = useStorage<'2' | '8' | '10' | '16'>('pastefy-utility-number-base', '10')
const bases = [
  { get label() { return t('utility.numberBaseTool.options.binary') }, value: '2' },
  { get label() { return t('utility.numberBaseTool.options.octal') }, value: '8' },
  { get label() { return t('utility.numberBaseTool.options.decimal') }, value: '10' },
  { get label() { return t('utility.numberBaseTool.options.hex') }, value: '16' },
]

const state = computed(() => {
  const parsed = Number.parseInt(input.value.trim(), Number(base.value))
  if (Number.isNaN(parsed)) return { error: 'Input could not be parsed in the selected base.' }

  return {
    decimal: parsed.toString(10),
    binary: parsed.toString(2),
    octal: parsed.toString(8),
    hex: parsed.toString(16).toUpperCase(),
    error: undefined,
  }
})

const result = computed(() => ('error' in state.value && state.value.error ? '' : JSON.stringify(state.value, null, 2)))
</script>

<template>
  <UtilityShell>
    <template #controls>
      <label class="text-sm font-medium">{{ $t('utility.numberBaseTool.inputBase') }}</label>
      <Select v-model="base" :options="bases" option-label="label" option-value="value" />
      <label class="text-sm font-medium">{{ $t('utility.numberBaseTool.value') }}</label>
      <InputText v-model="input" fluid />
    </template>

    <template #result>
      <Message v-if="'error' in state && state.error" severity="error">{{ state.error }}</Message>
      <div v-else class="grid gap-3">
        <div
          v-for="entry of [
            ['Decimal', 'decimal' in state ? state.decimal : ''],
            ['Binary', 'binary' in state ? state.binary : ''],
            ['Octal', 'octal' in state ? state.octal : ''],
            ['Hex', 'hex' in state ? state.hex : ''],
          ]"
          :key="entry[0]"
          class="rounded-xl border border-neutral-200 bg-white p-4 dark:border-neutral-700 dark:bg-neutral-900"
        >
          <div class="font-medium">{{ entry[0] }}</div>
          <code class="mt-2 block break-all text-sm">{{ entry[1] }}</code>
        </div>
      </div>
    </template>

    <template #footer>
      <UtilityResultActions :result="result" file-name="number-bases.json" />
    </template>
  </UtilityShell>
</template>
