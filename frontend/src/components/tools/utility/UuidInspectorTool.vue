<script setup lang="ts">
import InputText from 'primevue/inputtext'
import Message from 'primevue/message'
import { computed } from 'vue'
import { useStorage } from '@vueuse/core'
import UtilityShell from '@/components/tools/utility/UtilityShell.vue'
import UtilityResultActions from '@/components/tools/utility/UtilityResultActions.vue'

const input = useStorage('pastefy-utility-uuid-input', crypto.randomUUID())

const state = computed(() => {
  const value = input.value.trim().toLowerCase()
  const match = value.match(
    /^([0-9a-f]{8})-([0-9a-f]{4})-([1-8][0-9a-f]{3})-([89ab][0-9a-f]{3})-([0-9a-f]{12})$/,
  )

  if (!match) {
    return { error: 'Input is not a valid canonical UUID.' }
  }

  const version = Number.parseInt(match[3][0], 16)
  const variantNibble = Number.parseInt(match[4][0], 16)
  const variant =
    (variantNibble & 0b1000) === 0
      ? 'NCS'
      : (variantNibble & 0b1100) === 0b1000
        ? 'RFC 4122 / RFC 9562'
        : (variantNibble & 0b1110) === 0b1100
          ? 'Microsoft'
          : 'Future'

  return {
    value,
    version,
    variant,
    nil: value === '00000000-0000-0000-0000-000000000000',
    error: undefined,
  }
})

const result = computed(() => ('error' in state.value && state.value.error ? '' : JSON.stringify(state.value, null, 2)))
</script>

<template>
  <UtilityShell>
    <template #controls>
      <label class="text-sm font-medium">UUID</label>
      <InputText v-model="input" fluid />
    </template>

    <template #result>
      <Message v-if="'error' in state && state.error" severity="error">{{ state.error }}</Message>
      <div v-else class="grid gap-3">
        <div
          v-for="entry of [
            ['UUID', 'value' in state ? state.value : ''],
            ['Version', 'version' in state ? state.version : ''],
            ['Variant', 'variant' in state ? state.variant : ''],
            ['Nil UUID', 'nil' in state ? String(state.nil) : ''],
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
      <UtilityResultActions :result="result" file-name="uuid-inspection.json" />
    </template>
  </UtilityShell>
</template>
