<script setup lang="ts">
import { computed, onMounted, reactive, ref, useTemplateRef, watch } from 'vue'
import { client } from '@/main.ts'
import type { AnalyticsBreakdownPoint, AnalyticsResponse } from '@/types/analytics.ts'
import AnalyticsBreakdownCard from '@/components/analytics/AnalyticsBreakdownCard.vue'
import Button from 'primevue/button'
import DatePicker from 'primevue/datepicker'
import InputText from 'primevue/inputtext'
import Popover from 'primevue/popover'
import Select from 'primevue/select'
import type { PopoverMethods } from 'primevue'
import ErrorContainer from '@/components/ErrorContainer.vue'
import LoadingContainer from '@/components/LoadingContainer.vue'
import VChart from 'vue-echarts'
import { use } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import { LineChart } from 'echarts/charts'
import { GridComponent, TooltipComponent } from 'echarts/components'

use([CanvasRenderer, LineChart, GridComponent, TooltipComponent])

const props = defineProps<{
  endpoint: string
  title: string
  description?: string
}>()

type Filter = { field: string; value: string }
type Metric = 'visits' | 'unique_visitors'
type PanelKey = 'pastes' | 'referrers' | 'locations' | 'devices'

type Dimension = {
  label: string
  value: string
}

type Panel = {
  key: PanelKey
  title: string
  icon: string
  accent: 'amber' | 'rose' | 'blue' | 'green'
  active: string
  dimensions: Dimension[]
}

const summary = ref<AnalyticsResponse>()
const breakdowns = reactive<Record<PanelKey, AnalyticsBreakdownPoint[]>>({
  pastes: [],
  referrers: [],
  locations: [],
  devices: [],
})
const loading = ref(false)
const panelLoading = reactive<Record<PanelKey, boolean>>({
  pastes: false,
  referrers: false,
  locations: false,
  devices: false,
})
const error = ref<unknown>()
const dates = ref<[Date, Date | null]>([new Date(Date.now() - 24 * 60 * 60 * 1000), new Date()])
const interval = ref('hour')
const selectedMetric = ref<Metric>('visits')
const filters = reactive<Filter[]>([])
const rangeLabel = ref('Last 24 hours')
const filterPopover = useTemplateRef<PopoverMethods>('filterPopover')
const rangePopover = useTemplateRef<PopoverMethods>('rangePopover')

const panels = reactive<Panel[]>([
  {
    key: 'pastes',
    title: 'Pastes',
    icon: 'ti ti-file-text',
    accent: 'amber',
    active: 'paste_key',
    dimensions: [
      { label: 'Pastes', value: 'paste_key' },
      { label: 'Tags', value: 'paste_tag' },
      { label: 'Visibility', value: 'paste_visibility' },
    ],
  },
  {
    key: 'referrers',
    title: 'Referrers',
    icon: 'ti ti-link',
    accent: 'rose',
    active: 'referer_host',
    dimensions: [
      { label: 'Domains', value: 'referer_host' },
      { label: 'Acquisition', value: 'acquisition' },
      { label: 'Visit type', value: 'visit_type' },
    ],
  },
  {
    key: 'locations',
    title: 'Locations',
    icon: 'ti ti-map-pin',
    accent: 'blue',
    active: 'country',
    dimensions: [
      { label: 'Countries', value: 'country' },
      { label: 'Cities', value: 'city' },
      { label: 'Regions', value: 'region' },
    ],
  },
  {
    key: 'devices',
    title: 'Devices',
    icon: 'ti ti-device-desktop',
    accent: 'green',
    active: 'device_type',
    dimensions: [
      { label: 'Devices', value: 'device_type' },
      { label: 'Browsers', value: 'browser' },
      { label: 'OS', value: 'os' },
    ],
  },
])

const fields = [
  { label: 'Paste key', value: 'paste_key' },
  { label: 'Paste visibility', value: 'paste_visibility' },
  { label: 'Paste owner', value: 'paste_user_id' },
  { label: 'Paste tag', value: 'paste_tag' },
  { label: 'Visit type', value: 'visit_type' },
  { label: 'Country', value: 'country' },
  { label: 'Region', value: 'region' },
  { label: 'City', value: 'city' },
  { label: 'Visitor user', value: 'visitor_user_id' },
  { label: 'Browser', value: 'browser' },
  { label: 'Device type', value: 'device_type' },
  { label: 'OS', value: 'os' },
  { label: 'Referer host', value: 'referer_host' },
  { label: 'Acquisition', value: 'acquisition' },
  { label: 'Bot', value: 'is_bot' },
]

const presets = [
  { label: 'Last 24 hours', amount: 24, unit: 'hour', interval: 'hour', shortcut: 'D' },
  { label: 'Last 7 days', amount: 7, unit: 'day', interval: 'day', shortcut: 'W' },
  { label: 'Last 30 days', amount: 30, unit: 'day', interval: 'day', shortcut: 'T' },
  { label: 'Last 3 months', amount: 3, unit: 'month', interval: 'week', shortcut: 'M' },
]

const activeFilterCount = computed(() => filters.filter(({ value }) => value.trim()).length)

const requestParams = (groupBy: string, includeSummary = true, includeBreakdown = true) => {
  const [from, to] = dates.value
  const params: Record<string, string> = {
    from: from.toISOString(),
    to: (to || from).toISOString(),
    interval: interval.value,
    group_by: groupBy,
    include_summary: String(includeSummary),
    include_breakdown: String(includeBreakdown),
  }
  filters.forEach(({ field, value }) => {
    if (field && value.trim()) params[field] = value.trim()
  })
  return params
}

const fetchPanel = async (panel: Panel) => {
  panelLoading[panel.key] = true
  try {
    breakdowns[panel.key] = (
      await client.get(props.endpoint, {
        params: requestParams(panel.active, false),
      })
    ).data.breakdown as AnalyticsBreakdownPoint[]
  } finally {
    panelLoading[panel.key] = false
  }
}

const fetchAnalytics = async () => {
  loading.value = true
  error.value = undefined
  try {
    const [nextSummary] = await Promise.all([
      client.get(props.endpoint, { params: requestParams('paste_key', true, false) }),
      ...panels.map(fetchPanel),
    ])
    summary.value = nextSummary.data as AnalyticsResponse
  } catch (fetchError) {
    error.value = fetchError
  } finally {
    loading.value = false
  }
}

const switchPanelDimension = async (panel: Panel, dimension: string) => {
  if (panel.active === dimension) return
  panel.active = dimension
  try {
    await fetchPanel(panel)
  } catch (fetchError) {
    error.value = fetchError
  }
}

const addFilter = () => filters.push({ field: 'country', value: '' })
const removeFilter = (index: number) => filters.splice(index, 1)

const toggleFilterPopover = (event: Event) => {
  rangePopover.value?.hide()
  filterPopover.value?.toggle(event)
}

const toggleRangePopover = (event: Event) => {
  filterPopover.value?.hide()
  rangePopover.value?.toggle(event)
}

const applyPreset = (preset: (typeof presets)[number]) => {
  const nextTo = new Date()
  const nextFrom = new Date(nextTo)
  if (preset.unit === 'hour') nextFrom.setHours(nextFrom.getHours() - preset.amount)
  if (preset.unit === 'day') nextFrom.setDate(nextFrom.getDate() - preset.amount)
  if (preset.unit === 'month') nextFrom.setMonth(nextFrom.getMonth() - preset.amount)
  dates.value = [nextFrom, nextTo]
  interval.value = preset.interval
  rangeLabel.value = preset.label
  rangePopover.value?.hide()
  fetchAnalytics()
}

const applyCustomRange = () => {
  if (!dates.value[1]) return
  rangeLabel.value = 'Custom range'
  rangePopover.value?.hide()
  fetchAnalytics()
}

const formatBucket = (value: string) => {
  const date = new Date(value)
  if (interval.value === 'hour') {
    return date.toLocaleTimeString(undefined, { hour: 'numeric', minute: '2-digit' })
  }
  return date.toLocaleDateString(undefined, { month: 'short', day: 'numeric' })
}

const chartOptions = computed(() => {
  const values =
    summary.value?.series.map((point) =>
      selectedMetric.value === 'visits' ? point.visits : point.unique_visitors,
    ) || []

  return {
    animationDuration: 350,
    tooltip: {
      trigger: 'axis',
      formatter: (items: { axisValue: string; value: number }[]) => {
        const item = items[0]
        return `${formatBucket(item.axisValue)}<br><strong>${item.value}</strong> ${selectedMetric.value === 'visits' ? 'visits' : 'unique visitors'}`
      },
    },
    grid: { left: 44, right: 20, top: 24, bottom: 42 },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: summary.value?.series.map((point) => point.bucket) || [],
      axisLabel: { hideOverlap: true, formatter: formatBucket, color: '#8a8a8a' },
      axisLine: { lineStyle: { color: '#d4d4d4' } },
      axisTick: { show: false },
    },
    yAxis: {
      type: 'value',
      minInterval: 1,
      splitLine: { lineStyle: { color: '#d4d4d4', type: 'dashed' } },
      axisLabel: { color: '#8a8a8a' },
    },
    series: [
      {
        type: 'line',
        smooth: false,
        symbolSize: 7,
        showSymbol: values.length < 48,
        data: values,
        lineStyle: { color: '#4f8df7', width: 2 },
        itemStyle: { color: '#3b82f6' },
        areaStyle: { color: 'rgba(79, 141, 247, 0.14)' },
      },
    ],
  }
})

watch(() => props.endpoint, fetchAnalytics)
onMounted(fetchAnalytics)
</script>

<template>
  <section class="flex flex-col gap-5">
    <header>
      <h1 class="text-2xl font-bold">{{ title }}</h1>
      <p v-if="description" class="mt-1 text-sm text-neutral-600 dark:text-neutral-300">
        {{ description }}
      </p>
    </header>

    <div class="relative flex flex-wrap items-center gap-2">
      <Button
        icon="ti ti-filter"
        :label="activeFilterCount ? `Filter (${activeFilterCount})` : 'Filter'"
        severity="contrast"
        outlined
        @click="toggleFilterPopover"
      />
      <Button
        icon="ti ti-calendar"
        :label="rangeLabel"
        severity="contrast"
        outlined
        @click="toggleRangePopover"
      />
      <Button
        icon="ti ti-refresh"
        aria-label="Refresh analytics"
        severity="contrast"
        text
        :loading="loading"
        @click="fetchAnalytics"
      />

      <Popover ref="filterPopover" class="w-[42rem] max-w-[calc(100vw-3rem)]">
        <div class="mb-3 flex items-center justify-between">
          <h2 class="font-bold">Filters</h2>
          <Button icon="ti ti-plus" label="Add filter" size="small" text @click="addFilter" />
        </div>
        <p v-if="!filters.length" class="py-4 text-sm text-neutral-500">
          Add filters to narrow analytics by paste metadata, audience or acquisition.
        </p>
        <div v-for="(filter, index) in filters" :key="index" class="mb-2 flex gap-2">
          <Select
            v-model="filter.field"
            :options="fields"
            option-label="label"
            option-value="value"
            class="w-[14rem]"
          />
          <InputText v-model="filter.value" class="min-w-0 flex-1" placeholder="Exact value" />
          <Button
            icon="ti ti-trash"
            severity="contrast"
            text
            aria-label="Remove filter"
            @click="removeFilter(index)"
          />
        </div>
        <div class="mt-3 flex justify-end">
          <Button
            label="Apply filters"
            size="small"
            @click="(filterPopover?.hide(), fetchAnalytics())"
          />
        </div>
      </Popover>

      <Popover
        ref="rangePopover"
        class="w-[32rem] max-w-[calc(100vw-3rem)]"
        :pt="{ content: 'p-0' }"
      >
        <div class="grid md:grid-cols-[1fr_12rem]">
          <div class="flex flex-col gap-2">
            <DatePicker
              v-model="dates"
              selection-mode="range"
              :manual-input="false"
              inline
              :pt="{
                panel: 'border-0 p-0 pt-2',
              }"
            />
            <div class="p-2">
              <Button
                label="Apply custom range"
                size="small"
                :disabled="!dates[1]"
                @click="applyCustomRange"
              />
            </div>
          </div>
          <div
            class="flex flex-col gap-1 border-t border-neutral-200 p-2 md:border-t-0 md:border-l dark:border-neutral-700"
          >
            <Button
              v-for="preset of presets"
              :key="preset.label"
              :label="preset.label"
              severity="contrast"
              text
              class="justify-between"
              @click="applyPreset(preset)"
            />
          </div>
        </div>
      </Popover>
    </div>

    <ErrorContainer v-if="error" :error="error as any" />
    <LoadingContainer
      v-else-if="loading && !summary"
      class="flex items-center justify-center p-3"
    />
    <template v-else-if="summary">
      <div
        class="overflow-hidden rounded-2xl border border-neutral-200 bg-white dark:border-neutral-800 dark:bg-neutral-900"
      >
        <div
          class="grid grid-cols-1 border-b border-neutral-200 sm:grid-cols-3 dark:border-neutral-800"
        >
          <button
            class="relative flex min-h-[8.5rem] flex-col items-start justify-center gap-2 border-b border-neutral-200 px-6 text-left sm:border-r sm:border-b-0 dark:border-neutral-800"
            :class="
              selectedMetric === 'visits'
                ? 'after:absolute after:right-0 after:bottom-0 after:left-0 after:h-0.5 after:bg-neutral-950 dark:after:bg-white'
                : ''
            "
            @click="selectedMetric = 'visits'"
          >
            <span class="flex items-center gap-2 text-neutral-600 dark:text-neutral-300">
              <span class="h-2.5 w-2.5 rounded-sm bg-blue-300" />
              Visits
            </span>
            <strong class="text-4xl font-semibold">{{ summary.total_visits }}</strong>
          </button>
          <button
            class="relative flex min-h-[8.5rem] flex-col items-start justify-center gap-2 border-b border-neutral-200 px-6 text-left sm:border-r sm:border-b-0 dark:border-neutral-800"
            :class="
              selectedMetric === 'unique_visitors'
                ? 'after:absolute after:right-0 after:bottom-0 after:left-0 after:h-0.5 after:bg-neutral-950 dark:after:bg-white'
                : ''
            "
            @click="selectedMetric = 'unique_visitors'"
          >
            <span class="flex items-center gap-2 text-neutral-600 dark:text-neutral-300">
              <span class="h-2.5 w-2.5 rounded-sm bg-violet-300" />
              Unique visitors
            </span>
            <strong class="text-4xl font-semibold">{{ summary.unique_visitors }}</strong>
          </button>
          <div class="flex min-h-[8.5rem] flex-col items-start justify-center gap-2 px-6 text-left">
            <span class="flex items-center gap-2 text-neutral-600 dark:text-neutral-300">
              <span class="h-2.5 w-2.5 rounded-sm bg-rose-300" />
              Bot visits
            </span>
            <strong class="text-4xl font-semibold">{{ summary.bot_visits }}</strong>
          </div>
        </div>
        <div class="h-[380px] min-h-0 overflow-hidden p-4">
          <VChart class="h-full w-full" :option="chartOptions" autoresize />
        </div>
      </div>

      <div class="grid grid-cols-1 gap-4 xl:grid-cols-2">
        <AnalyticsBreakdownCard
          v-for="panel of panels"
          :key="panel.key"
          :title="panel.title"
          :icon="panel.icon"
          :accent="panel.accent"
          :dimensions="panel.dimensions"
          :active="panel.active"
          :rows="breakdowns[panel.key]"
          :loading="panelLoading[panel.key]"
          @update:active="switchPanelDimension(panel, $event)"
        />
      </div>
    </template>
  </section>
</template>
