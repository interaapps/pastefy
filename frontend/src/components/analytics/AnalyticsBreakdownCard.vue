<script setup lang="ts">
import { computed } from 'vue'
import type { AnalyticsBreakdownPoint } from '@/types/analytics.ts'
import ProgressSpinner from 'primevue/progressspinner'

const countryFlagModules = import.meta.glob('../../../node_modules/ia-flag-icons/states/*.svg', {
  eager: true,
  import: 'default',
  query: '?url',
}) as Record<string, string>

const countryFlags = Object.fromEntries(
  Object.entries(countryFlagModules)
    .map(([path, url]) => [path.match(/\/([A-Z]{2})\.svg$/)?.[1], url])
    .filter(([country]) => country),
) as Record<string, string>

const props = defineProps<{
  title: string
  icon: string
  accent: 'amber' | 'rose' | 'blue' | 'green'
  dimensions: { label: string; value: string }[]
  active: string
  rows: AnalyticsBreakdownPoint[]
  loading?: boolean
}>()

defineEmits<{
  'update:active': [value: string]
}>()

const maxVisits = computed(() => Math.max(...props.rows.map((row) => row.visits), 1))
const rowColor = computed(
  () =>
    ({
      amber: 'rgba(251, 191, 36, 0.2)',
      rose: 'rgba(251, 113, 133, 0.18)',
      blue: 'rgba(96, 165, 250, 0.2)',
      green: 'rgba(74, 222, 128, 0.18)',
    })[props.accent],
)

const displayValue = (value: string) => {
  if (!value) return '(direct / unknown)'
  if (
    props.active === 'paste_key' ||
    props.active === 'paste_tag' ||
    props.active === 'referer_host'
  )
    return value
  if (props.active === 'country' || props.active === 'region') return value.toUpperCase()
  return value
    .replace(/_/g, ' ')
    .toLowerCase()
    .replace(/(^|\s)\S/g, (character: string) => character.toUpperCase())
}

const countryFlag = (value: string) =>
  props.active === 'country' ? countryFlags[value.toUpperCase()] : undefined
</script>

<template>
  <section
    class="min-h-[19rem] overflow-hidden rounded-2xl border border-neutral-200 bg-white dark:border-neutral-800 dark:bg-neutral-900"
  >
    <header
      class="flex min-h-[3.8rem] items-end justify-between border-b border-neutral-200 px-5 dark:border-neutral-800"
    >
      <nav class="flex h-full gap-6 overflow-auto">
        <button
          v-for="dimension of dimensions"
          :key="dimension.value"
          class="relative pb-4 text-sm whitespace-nowrap text-neutral-500 transition-colors hover:text-neutral-950 dark:hover:text-white"
          :class="
            active === dimension.value
              ? 'font-semibold text-neutral-950 after:absolute after:right-0 after:bottom-0 after:left-0 after:h-0.5 after:bg-neutral-950 dark:text-white dark:after:bg-white'
              : ''
          "
          @click="$emit('update:active', dimension.value)"
        >
          {{ dimension.label }}
        </button>
      </nav>
      <span class="mb-4 ml-3 flex items-center gap-1 text-xs font-semibold text-neutral-500">
        <i :class="icon" />
        VISITS
      </span>
    </header>

    <div class="relative flex min-h-[15rem] flex-col gap-2 p-4">
      <div
        v-if="loading"
        class="absolute inset-0 z-10 flex items-center justify-center bg-white/70 dark:bg-neutral-900/70"
      >
        <ProgressSpinner class="h-7 w-7" stroke-width="5" />
      </div>
      <p v-if="!rows.length && !loading" class="m-auto text-sm text-neutral-500">
        No data for this selection.
      </p>
      <div
        v-for="row of rows"
        :key="row.value"
        class="relative flex min-h-10 items-center justify-between overflow-hidden rounded-lg px-3 text-sm"
      >
        <span
          class="absolute inset-y-0 left-0"
          :style="{
            width: `${Math.max(5, (row.visits / maxVisits) * 100)}%`,
            backgroundColor: rowColor,
          }"
        />
        <span class="relative z-1 flex min-w-0 items-center gap-2 truncate">
          <img
            v-if="countryFlag(row.value)"
            :src="countryFlag(row.value)"
            :alt="`${row.value.toUpperCase()} flag`"
            class="h-5 w-5 shrink-0 rounded-xs object-cover"
          />
          <i v-else :class="icon" class="shrink-0" />
          <span class="truncate">{{ displayValue(row.value) }}</span>
        </span>
        <strong class="relative z-1 ml-3 font-semibold">{{ row.visits }}</strong>
      </div>
    </div>
  </section>
</template>
