<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { client } from '@/main.ts'
import type { AnalyticsResponse } from '@/types/analytics.ts'
import Button from 'primevue/button'
import Column from 'primevue/column'
import DataTable from 'primevue/datatable'
import DatePicker from 'primevue/datepicker'
import InputText from 'primevue/inputtext'
import Select from 'primevue/select'
import ErrorContainer from '@/components/ErrorContainer.vue'
import LoadingContainer from '@/components/LoadingContainer.vue'
import StatsCard from '@/components/admin/StatsCard.vue'
import VChart from 'vue-echarts'
import { use } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import { BarChart, LineChart } from 'echarts/charts'
import { GridComponent, LegendComponent, TooltipComponent } from 'echarts/components'

use([CanvasRenderer, LineChart, BarChart, GridComponent, LegendComponent, TooltipComponent])

const props = defineProps<{
  endpoint: string
  title: string
  description?: string
}>()

type Filter = { field: string; value: string }

const data = ref<AnalyticsResponse>()
const loading = ref(false)
const error = ref<unknown>()
const from = ref(new Date(Date.now() - 30 * 24 * 60 * 60 * 1000))
const to = ref(new Date())
const interval = ref('day')
const groupBy = ref('country')
const filters = reactive<Filter[]>([])

const intervalOptions = [
  { label: 'Hourly', value: 'hour' },
  { label: 'Daily', value: 'day' },
  { label: 'Weekly', value: 'week' },
  { label: 'Monthly', value: 'month' },
]

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

const addFilter = () => filters.push({ field: 'country', value: '' })
const removeFilter = (index: number) => filters.splice(index, 1)

const fetchAnalytics = async () => {
  loading.value = true
  error.value = undefined
  try {
    const params: Record<string, string> = {
      from: from.value.toISOString(),
      to: to.value.toISOString(),
      interval: interval.value,
      group_by: groupBy.value,
    }
    filters.forEach(({ field, value }) => {
      if (field && value.trim()) params[field] = value.trim()
    })
    data.value = (await client.get(props.endpoint, { params })).data as AnalyticsResponse
  } catch (fetchError) {
    error.value = fetchError
  } finally {
    loading.value = false
  }
}

const chartOptions = computed(() => ({
  tooltip: { trigger: 'axis' },
  legend: { data: ['Visits', 'Unique visitors'] },
  grid: { left: 44, right: 24, top: 42, bottom: 42 },
  xAxis: {
    type: 'category',
    data: data.value?.series.map((point) => point.bucket) || [],
    axisLabel: { hideOverlap: true },
  },
  yAxis: { type: 'value', minInterval: 1 },
  series: [
    {
      name: 'Visits',
      type: 'line',
      smooth: true,
      showSymbol: false,
      data: data.value?.series.map((point) => point.visits) || [],
    },
    {
      name: 'Unique visitors',
      type: 'line',
      smooth: true,
      showSymbol: false,
      data: data.value?.series.map((point) => point.unique_visitors) || [],
    },
  ],
}))

const breakdownOptions = computed(() => ({
  tooltip: { trigger: 'axis' },
  grid: { left: 44, right: 24, top: 24, bottom: 72 },
  xAxis: {
    type: 'category',
    data: data.value?.breakdown.map((point) => point.value || '(empty)') || [],
    axisLabel: { interval: 0, rotate: 35 },
  },
  yAxis: { type: 'value', minInterval: 1 },
  series: [
    {
      name: 'Visits',
      type: 'bar',
      data: data.value?.breakdown.map((point) => point.visits) || [],
    },
  ],
}))

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

    <form
      class="rounded-2xl border border-neutral-200 bg-white/80 p-4 dark:border-neutral-800 dark:bg-neutral-900/70"
      @submit.prevent="fetchAnalytics"
    >
      <div class="grid grid-cols-1 gap-3 md:grid-cols-2 lg:grid-cols-4">
        <label class="flex flex-col gap-1 text-sm">
          <span>From</span>
          <DatePicker v-model="from" show-time hour-format="24" />
        </label>
        <label class="flex flex-col gap-1 text-sm">
          <span>To</span>
          <DatePicker v-model="to" show-time hour-format="24" />
        </label>
        <label class="flex flex-col gap-1 text-sm">
          <span>Interval</span>
          <Select
            v-model="interval"
            :options="intervalOptions"
            option-label="label"
            option-value="value"
          />
        </label>
        <label class="flex flex-col gap-1 text-sm">
          <span>Breakdown</span>
          <Select v-model="groupBy" :options="fields" option-label="label" option-value="value" />
        </label>
      </div>

      <div v-if="filters.length" class="mt-4 flex flex-col gap-2">
        <div
          v-for="(filter, index) in filters"
          :key="index"
          class="flex flex-col gap-2 md:flex-row"
        >
          <Select
            v-model="filter.field"
            :options="fields"
            option-label="label"
            option-value="value"
            class="md:w-[15rem]"
          />
          <InputText v-model="filter.value" class="flex-1" placeholder="Exact value" />
          <Button
            type="button"
            icon="ti ti-trash"
            severity="contrast"
            text
            aria-label="Remove filter"
            @click="removeFilter(index)"
          />
        </div>
      </div>

      <div class="mt-4 flex flex-wrap gap-2">
        <Button
          type="button"
          icon="ti ti-plus"
          label="Add filter"
          outlined
          severity="contrast"
          @click="addFilter"
        />
        <Button type="submit" icon="ti ti-refresh" label="Apply" :loading="loading" />
      </div>
    </form>

    <ErrorContainer v-if="error" :error="error as any" />
    <LoadingContainer v-else-if="loading && !data" class="flex items-center justify-center p-3" />
    <template v-else-if="data">
      <div class="grid grid-cols-1 gap-3 md:grid-cols-3">
        <StatsCard :top="data.total_visits" description="Visits" />
        <StatsCard :top="data.unique_visitors" description="Unique visitors" />
        <StatsCard :top="data.bot_visits" description="Bot visits" />
      </div>

      <div class="grid grid-cols-1 gap-4 xl:grid-cols-2">
        <div
          class="rounded-2xl border border-neutral-200 bg-white/80 p-3 dark:border-neutral-800 dark:bg-neutral-900/70"
        >
          <h2 class="mb-2 font-bold">Visits over time</h2>
          <div class="h-[360px] min-h-0 overflow-hidden">
            <VChart class="h-full w-full" :option="chartOptions" autoresize />
          </div>
        </div>
        <div
          class="rounded-2xl border border-neutral-200 bg-white/80 p-3 dark:border-neutral-800 dark:bg-neutral-900/70"
        >
          <h2 class="mb-2 font-bold">Breakdown</h2>
          <div class="h-[360px] min-h-0 overflow-hidden">
            <VChart class="h-full w-full" :option="breakdownOptions" autoresize />
          </div>
        </div>
      </div>

      <DataTable :value="data.breakdown" striped-rows class="overflow-hidden rounded-xl">
        <Column field="value" header="Value" />
        <Column field="visits" header="Visits" />
        <Column field="unique_visitors" header="Unique visitors" />
      </DataTable>
    </template>
  </section>
</template>
