<script setup lang="ts">
import Button from 'primevue/button'
import InputText from 'primevue/inputtext'
import Message from 'primevue/message'
import { computed } from 'vue'
import { useStorage } from '@vueuse/core'
import UtilityShell from '@/components/tools/utility/UtilityShell.vue'
import UtilityResultActions from '@/components/tools/utility/UtilityResultActions.vue'

const input = useStorage('pastefy-utility-timestamp-input', String(Math.floor(Date.now() / 1000)))

const state = computed(() => {
  const raw = input.value.trim()
  if (!raw) return { error: 'Enter a Unix timestamp or date string.' }

  let date: Date | undefined
  if (/^\d+$/.test(raw)) {
    const numeric = Number(raw)
    date = new Date(raw.length > 10 ? numeric : numeric * 1000)
  } else {
    const parsed = new Date(raw)
    if (!Number.isNaN(parsed.getTime())) date = parsed
  }

  if (!date || Number.isNaN(date.getTime())) {
    return { error: 'Could not parse this timestamp or date input.' }
  }

  return {
    unixSeconds: Math.floor(date.getTime() / 1000),
    unixMillis: date.getTime(),
    iso: date.toISOString(),
    local: date.toLocaleString(),
    utc: date.toUTCString(),
    error: undefined,
  }
})

const result = computed(() => ('error' in state.value && state.value.error ? '' : JSON.stringify(state.value, null, 2)))
</script>

<template>
  <UtilityShell>
    <template #controls>
      <label class="text-sm font-medium">Timestamp or date</label>
      <InputText v-model="input" fluid />
      <div>
        <Button
          @click="input = String(Math.floor(Date.now() / 1000))"
          label="use current unix time"
          icon="ti ti-clock"
          severity="contrast"
          outlined
        />
      </div>
    </template>

    <template #result>
      <Message v-if="'error' in state && state.error" severity="error">{{ state.error }}</Message>
      <div v-else class="grid gap-3">
        <div
          v-for="entry of [
            ['Unix seconds', 'unixSeconds' in state ? state.unixSeconds : ''],
            ['Unix milliseconds', 'unixMillis' in state ? state.unixMillis : ''],
            ['ISO', 'iso' in state ? state.iso : ''],
            ['Local', 'local' in state ? state.local : ''],
            ['UTC', 'utc' in state ? state.utc : ''],
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
      <UtilityResultActions :result="result" file-name="timestamp-output.json" />
    </template>
  </UtilityShell>
</template>
