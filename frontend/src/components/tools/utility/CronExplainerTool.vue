<script setup lang="ts">
import InputText from 'primevue/inputtext'
import Message from 'primevue/message'
import Select from 'primevue/select'
import { computed } from 'vue'
import { useStorage } from '@vueuse/core'
import UtilityShell from '@/components/tools/utility/UtilityShell.vue'
import UtilityResultActions from '@/components/tools/utility/UtilityResultActions.vue'

const cronInput = useStorage('pastefy-utility-cron-input', '*/15 9-17 * * 1-5')
const selectedSample = useStorage('pastefy-utility-cron-sample', '')

const samples = [
  { label: 'Custom', value: '', cron: '' },
  { label: 'Every hour', value: 'hourly', cron: '0 * * * *' },
  { label: 'Daily at midnight', value: 'daily', cron: '0 0 * * *' },
  { label: 'Weekly on Monday', value: 'weekly', cron: '0 9 * * 1' },
  { label: 'Monthly on the 1st', value: 'monthly', cron: '0 8 1 * *' },
  { label: 'Every 15 minutes', value: 'quarter-hour', cron: '*/15 * * * *' },
  { label: 'Weekday mornings', value: 'weekday-morning', cron: '0 9 * * 1-5' },
  { label: 'Yearly on Jan 1st', value: 'yearly', cron: '0 0 1 1 *' },
]

const applySample = (value: string) => {
  selectedSample.value = value
  const sample = samples.find((entry) => entry.value === value)
  if (sample?.cron) cronInput.value = sample.cron
}

const cronFields = computed(() => cronInput.value.trim().split(/\s+/).filter(Boolean))
const cronLabels = ['minute', 'hour', 'day of month', 'month', 'day of week']

const describeCronField = (value: string, label: string) => {
  if (value === '*') return `every ${label}`
  if (value.startsWith('*/')) return `every ${value.slice(2)} ${label}${value.slice(2) === '1' ? '' : 's'}`
  if (value.includes(',')) return `${label}: ${value.split(',').join(', ')}`
  if (value.includes('-')) return `${label}: ${value}`
  return `${label}: ${value}`
}

const cronState = computed(() => {
  if (cronFields.value.length !== 5) {
    return { error: 'Cron explainer currently expects a standard 5-part cron expression.' }
  }
  return {
    parts: cronFields.value.map((value, index) => ({
      label: cronLabels[index],
      value,
      description: describeCronField(value, cronLabels[index]),
    })),
    error: undefined,
  }
})

const result = computed(() => {
  if (!('parts' in cronState.value) || !cronState.value.parts) return ''
  return cronState.value.parts.map((part) => `${part.label}: ${part.value} (${part.description})`).join('\n')
})
</script>

<template>
  <UtilityShell>
    <template #controls>
      <label class="text-sm font-medium">Samples</label>
      <Select
        :model-value="selectedSample"
        :options="samples"
        option-label="label"
        option-value="value"
        @update:model-value="applySample"
      />
      <label class="text-sm font-medium">Cron expression</label>
      <InputText v-model="cronInput" fluid />
      <Message severity="secondary" size="small" variant="simple">
        Uses the common 5-field cron format: minute hour day-of-month month day-of-week.
      </Message>
    </template>

    <template #result>
      <Message v-if="'error' in cronState && cronState.error" severity="error">
        {{ cronState.error }}
      </Message>
      <div v-else class="grid gap-3">
        <div
          v-for="part of ('parts' in cronState ? cronState.parts : [])"
          :key="part.label"
          class="rounded-xl border border-neutral-200 bg-white p-4 dark:border-neutral-700 dark:bg-neutral-900"
        >
          <div class="font-medium capitalize">{{ part.label }}</div>
          <div class="mt-1 text-sm text-neutral-500 dark:text-neutral-400">{{ part.value }}</div>
          <p class="mt-2 text-sm">{{ part.description }}</p>
        </div>
      </div>
    </template>

    <template #footer>
      <UtilityResultActions :result="result" file-name="cron-explainer.txt" />
    </template>
  </UtilityShell>
</template>
