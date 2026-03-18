<script setup lang="ts">
import Button from 'primevue/button'
import Checkbox from 'primevue/checkbox'
import InputNumber from 'primevue/inputnumber'
import InputText from 'primevue/inputtext'
import MultiSelect from 'primevue/multiselect'
import Select from 'primevue/select'
import Tag from 'primevue/tag'
import Textarea from 'primevue/textarea'
import { computed, ref, watch } from 'vue'
import { useStorage } from '@vueuse/core'

import Highlighted from '@/components/Highlighted.vue'
import MermaidViewer from '@/components/previews/MermaidViewer.vue'
import UtilityResultActions from '@/components/tools/utility/UtilityResultActions.vue'
import UtilityShell from '@/components/tools/utility/UtilityShell.vue'
import { parseYamlDocument } from '@/utils/yaml.ts'

type InputFormat = 'auto' | 'json' | 'yaml' | 'csv'
type ChartType = 'pie' | 'bar' | 'line' | 'grouped-bar' | 'multi-line' | 'combo'
type Aggregation = 'sum' | 'avg' | 'min' | 'max' | 'count'
type SortMode = 'input' | 'label-asc' | 'label-desc' | 'value-desc' | 'value-asc'

type FlatRow = Record<string, string | number | boolean | null>

type ParsedState = {
  format: Exclude<InputFormat, 'auto'>
  rows: FlatRow[]
  error?: string
}

type FieldOption = {
  label: string
  value: string
  numeric: boolean
  sample: string
}

type ExamplePreset = {
  id: string
  label: string
  description: string
  format: Exclude<InputFormat, 'auto'>
  source: string
  chartType: ChartType
  title: string
  yAxisLabel?: string
  categoryFields: string[]
  splitFields?: string[]
  valueFields?: string[]
  aggregation?: Aggregation
}

const jsonExample = `[
  { "month": "Jan", "region": "North", "revenue": 120, "profit": 32, "orders": 12 },
  { "month": "Jan", "region": "South", "revenue": 95, "profit": 20, "orders": 10 },
  { "month": "Feb", "region": "North", "revenue": 140, "profit": 44, "orders": 14 },
  { "month": "Feb", "region": "South", "revenue": 110, "profit": 28, "orders": 11 },
  { "month": "Mar", "region": "North", "revenue": 168, "profit": 51, "orders": 17 },
  { "month": "Mar", "region": "South", "revenue": 126, "profit": 35, "orders": 13 }
]`

const csvExample = `month,region,revenue,profit,orders
Jan,North,120,32,12
Jan,South,95,20,10
Feb,North,140,44,14
Feb,South,110,28,11
Mar,North,168,51,17
Mar,South,126,35,13`

const yamlExample = `sales:
  - month: Jan
    region: North
    revenue: 120
    profit: 32
    orders: 12
  - month: Jan
    region: South
    revenue: 95
    profit: 20
    orders: 10
  - month: Feb
    region: North
    revenue: 140
    profit: 44
    orders: 14
  - month: Feb
    region: South
    revenue: 110
    profit: 28
    orders: 11`

const source = useStorage('pastefy-utility-data-mermaid-source', jsonExample)
const inputFormat = useStorage<InputFormat>('pastefy-utility-data-mermaid-format', 'auto')
const chartType = useStorage<ChartType>('pastefy-utility-data-mermaid-chart-type', 'grouped-bar')
const chartTitle = useStorage('pastefy-utility-data-mermaid-title', 'Revenue by Month')
const yAxisLabel = useStorage('pastefy-utility-data-mermaid-y-axis', 'Value')
const categoryFields = useStorage<string[]>('pastefy-utility-data-mermaid-category-fields', [])
const splitFields = useStorage<string[]>('pastefy-utility-data-mermaid-split-fields', [])
const valueFields = useStorage<string[]>('pastefy-utility-data-mermaid-value-fields', [])
const aggregation = useStorage<Aggregation>('pastefy-utility-data-mermaid-aggregation', 'sum')
const sortMode = useStorage<SortMode>('pastefy-utility-data-mermaid-sort', 'input')
const limit = useStorage<number>('pastefy-utility-data-mermaid-limit', 12)
const includeEverythingElse = useStorage('pastefy-utility-data-mermaid-everything-else', false)
const everythingElseLabel = useStorage(
  'pastefy-utility-data-mermaid-everything-else-label',
  'Everything else',
)
const maxSeries = useStorage<number>('pastefy-utility-data-mermaid-max-series', 8)
const decimalPlaces = useStorage<number>('pastefy-utility-data-mermaid-decimals', 2)
const yAxisMode = useStorage<'auto' | 'manual'>('pastefy-utility-data-mermaid-y-mode', 'auto')
const yAxisMin = useStorage<number>('pastefy-utility-data-mermaid-y-min', 0)
const yAxisMax = useStorage<number>('pastefy-utility-data-mermaid-y-max', 200)
const emptyLabelText = useStorage('pastefy-utility-data-mermaid-empty-label', '(empty)')
const labelSeparator = useStorage('pastefy-utility-data-mermaid-label-separator', ' / ')
const includeSeriesComments = useStorage('pastefy-utility-data-mermaid-comments', true)
const hideZeroSeries = useStorage('pastefy-utility-data-mermaid-hide-zero-series', false)
const hideZeroCategories = useStorage('pastefy-utility-data-mermaid-hide-zero-categories', false)
const pieShowData = useStorage('pastefy-utility-data-mermaid-pie-show-data', true)
const selectedPresetId = ref<string | null>(null)

const formatOptions = [
  { label: 'Auto detect', value: 'auto' as InputFormat },
  { label: 'JSON', value: 'json' as InputFormat },
  { label: 'YAML', value: 'yaml' as InputFormat },
  { label: 'CSV', value: 'csv' as InputFormat },
]

const chartTypeOptions = [
  { label: 'Pie chart', value: 'pie' as ChartType },
  { label: 'Bar chart', value: 'bar' as ChartType },
  { label: 'Line chart', value: 'line' as ChartType },
  { label: 'Grouped bars', value: 'grouped-bar' as ChartType },
  { label: 'Multi-line', value: 'multi-line' as ChartType },
  { label: 'Combo chart', value: 'combo' as ChartType },
]

const aggregationOptions = [
  { label: 'Sum', value: 'sum' as Aggregation },
  { label: 'Average', value: 'avg' as Aggregation },
  { label: 'Min', value: 'min' as Aggregation },
  { label: 'Max', value: 'max' as Aggregation },
  { label: 'Count', value: 'count' as Aggregation },
]

const sortOptions = [
  { label: 'Keep input order', value: 'input' as SortMode },
  { label: 'Label A-Z', value: 'label-asc' as SortMode },
  { label: 'Label Z-A', value: 'label-desc' as SortMode },
  { label: 'Value high -> low', value: 'value-desc' as SortMode },
  { label: 'Value low -> high', value: 'value-asc' as SortMode },
]

const yAxisModeOptions = [
  { label: 'Auto range', value: 'auto' as const },
  { label: 'Manual range', value: 'manual' as const },
]

const examplePresets: ExamplePreset[] = [
  {
    id: 'grouped-bar-sales',
    label: 'Grouped bars',
    description: 'Revenue by month and region.',
    format: 'json',
    source: jsonExample,
    chartType: 'grouped-bar',
    title: 'Revenue by Month',
    yAxisLabel: 'Revenue',
    categoryFields: ['month'],
    splitFields: ['region'],
    valueFields: ['revenue'],
    aggregation: 'sum',
  },
  {
    id: 'multi-line-profit',
    label: 'Multi-line trends',
    description: 'Profit trend per region over time.',
    format: 'json',
    source: jsonExample,
    chartType: 'multi-line',
    title: 'Profit Trend',
    yAxisLabel: 'Profit',
    categoryFields: ['month'],
    splitFields: ['region'],
    valueFields: ['profit'],
    aggregation: 'sum',
  },
  {
    id: 'pie-orders',
    label: 'Pie share',
    description: 'Order share by region.',
    format: 'csv',
    source: csvExample,
    chartType: 'pie',
    title: 'Orders by Region',
    categoryFields: ['region'],
    valueFields: ['orders'],
    aggregation: 'sum',
  },
  {
    id: 'combo-revenue-profit',
    label: 'Combo chart',
    description: 'Revenue bars with profit line.',
    format: 'json',
    source: jsonExample,
    chartType: 'combo',
    title: 'Revenue vs Profit',
    yAxisLabel: 'Amount',
    categoryFields: ['month'],
    valueFields: ['revenue', 'profit'],
    aggregation: 'sum',
  },
  {
    id: 'yaml-bars',
    label: 'YAML bar chart',
    description: 'Revenue values from YAML grouped by month and region.',
    format: 'yaml',
    source: yamlExample,
    chartType: 'bar',
    title: 'Revenue Entries',
    yAxisLabel: 'Revenue',
    categoryFields: ['month', 'region'],
    valueFields: ['revenue'],
    aggregation: 'sum',
  },
]

const detectFormat = (value: string): Exclude<InputFormat, 'auto'> => {
  const trimmed = value.trim()
  if (!trimmed) return 'json'
  if (trimmed.startsWith('{') || trimmed.startsWith('[')) return 'json'
  const firstLine = trimmed.split(/\r?\n/, 1)[0] || ''
  if (firstLine.includes(',') && !firstLine.includes(':')) return 'csv'
  return 'yaml'
}

const parseCsv = (raw: string) => {
  const rows: string[][] = []
  let current = ''
  let row: string[] = []
  let inQuotes = false

  const pushCell = () => {
    row.push(current)
    current = ''
  }

  const pushRow = () => {
    if (row.length > 0 || current !== '') {
      pushCell()
      rows.push(row)
      row = []
    }
  }

  for (let i = 0; i < raw.length; i += 1) {
    const char = raw[i]
    const next = raw[i + 1]

    if (char === '"') {
      if (inQuotes && next === '"') {
        current += '"'
        i += 1
      } else {
        inQuotes = !inQuotes
      }
      continue
    }

    if (char === ',' && !inQuotes) {
      pushCell()
      continue
    }

    if ((char === '\n' || char === '\r') && !inQuotes) {
      if (char === '\r' && next === '\n') i += 1
      pushRow()
      continue
    }

    current += char
  }

  if (current !== '' || row.length > 0) pushRow()
  return rows
}

const isRecord = (value: unknown): value is Record<string, unknown> =>
  value !== null && typeof value === 'object' && !Array.isArray(value)

const normalizePrimitive = (value: unknown) => {
  if (value === null || value === undefined) return null
  if (typeof value === 'number' || typeof value === 'boolean' || typeof value === 'string') {
    return value
  }
  return JSON.stringify(value)
}

const flattenRecord = (
  value: unknown,
  prefix = '',
  output: FlatRow = {},
): FlatRow => {
  if (Array.isArray(value)) {
    output[prefix || 'value'] = value.every((entry) => !isRecord(entry))
      ? value.map((entry) => normalizePrimitive(entry)).join(', ')
      : JSON.stringify(value)
    return output
  }

  if (!isRecord(value)) {
    output[prefix || 'value'] = normalizePrimitive(value)
    return output
  }

  Object.entries(value).forEach(([key, entry]) => {
    const nextKey = prefix ? `${prefix}.${key}` : key
    if (isRecord(entry)) {
      flattenRecord(entry, nextKey, output)
      return
    }

    if (Array.isArray(entry)) {
      output[nextKey] = entry.every((item) => !isRecord(item))
        ? entry.map((item) => normalizePrimitive(item)).join(', ')
        : JSON.stringify(entry)
      return
    }

    output[nextKey] = normalizePrimitive(entry)
  })

  return output
}

const normalizeToRows = (value: unknown): FlatRow[] => {
  if (Array.isArray(value)) {
    if (!value.length) return []
    return value.map((entry, index) =>
      isRecord(entry)
        ? flattenRecord(entry)
        : {
            index: index + 1,
            value: normalizePrimitive(entry),
          },
    )
  }

  if (isRecord(value)) {
    const entries = Object.entries(value)
    const arrayEntries = entries.filter(([, entry]) => Array.isArray(entry))

    if (arrayEntries.length === 1) {
      return normalizeToRows(arrayEntries[0][1])
    }

    if (entries.length && entries.every(([, entry]) => isRecord(entry))) {
      return entries.map(([key, entry]) => ({
        key,
        ...flattenRecord(entry),
      }))
    }

    return [flattenRecord(value)]
  }

  return [{ value: normalizePrimitive(value) }]
}

const parseInput = (raw: string, format: InputFormat): ParsedState => {
  const resolvedFormat = format === 'auto' ? detectFormat(raw) : format

  try {
    if (resolvedFormat === 'json') {
      return {
        format: 'json',
        rows: normalizeToRows(JSON.parse(raw)),
      }
    }

    if (resolvedFormat === 'yaml') {
      return {
        format: 'yaml',
        rows: normalizeToRows(parseYamlDocument(raw)),
      }
    }

    const csvRows = parseCsv(raw)
    if (!csvRows.length) {
      return {
        format: 'csv',
        rows: [],
      }
    }

    const [headers, ...body] = csvRows
    return {
      format: 'csv',
      rows: body.map((row) =>
        headers.reduce<FlatRow>((acc, header, index) => {
          acc[header] = row[index] ?? ''
          return acc
        }, {}),
      ),
    }
  } catch (error) {
    return {
      format: resolvedFormat,
      rows: [],
      error: error instanceof Error ? error.message : 'Could not parse this input.',
    }
  }
}

const toNumber = (value: unknown) => {
  if (typeof value === 'number' && Number.isFinite(value)) return value
  if (typeof value === 'string' && value.trim() !== '' && Number.isFinite(Number(value))) {
    return Number(value)
  }
  return undefined
}

const normalizeMermaidText = (value: unknown, fallback = '(empty)') => {
  const normalized = String(value ?? '')
    .replace(/\r?\n|\r/g, ' ')
    .replace(/\s+/g, ' ')
    .trim()

  return normalized || fallback
}

const escapeMermaidLabel = (value: unknown, fallback = '(empty)') =>
  JSON.stringify(normalizeMermaidText(value, fallback))

const escapeMermaidComment = (value: unknown, fallback = 'Series') =>
  normalizeMermaidText(value, fallback).replace(/[%[\]{}]/g, '')

const parsedState = computed(() => parseInput(source.value, inputFormat.value))
const selectedPreset = computed(
  () => examplePresets.find((preset) => preset.id === selectedPresetId.value) || null,
)

const fieldOptions = computed<FieldOption[]>(() => {
  const fields = new Map<string, { numericCount: number; sample: string; total: number }>()

  parsedState.value.rows.forEach((row) => {
    Object.entries(row).forEach(([key, value]) => {
      const entry = fields.get(key) || { numericCount: 0, sample: '', total: 0 }
      entry.total += 1
      if (!entry.sample && value !== null && value !== undefined && String(value) !== '') {
        entry.sample = String(value)
      }
      if (toNumber(value) !== undefined) entry.numericCount += 1
      fields.set(key, entry)
    })
  })

  return Array.from(fields.entries())
    .map(([key, value]) => ({
      label: key,
      value: key,
      numeric: value.numericCount > 0,
      sample: value.sample,
    }))
    .sort((a, b) => a.label.localeCompare(b.label))
})

const numericFieldOptions = computed(() => fieldOptions.value.filter((field) => field.numeric))
const dimensionFieldOptions = computed(() => fieldOptions.value)

watch(
  fieldOptions,
  (fields) => {
    const available = new Set(fields.map((field) => field.value))
    const firstDimension =
      fields.find((field) => !field.numeric)?.value || fields[0]?.value || ''
    const firstNumeric = numericFieldOptions.value[0]?.value || ''

    const nextCategoryFields = categoryFields.value.filter((field) => available.has(field))
    if (!nextCategoryFields.length && firstDimension) {
      categoryFields.value = [firstDimension]
    } else if (nextCategoryFields.length !== categoryFields.value.length) {
      categoryFields.value = nextCategoryFields
    }

    const nextSplitFields = splitFields.value.filter((field) => available.has(field))
    if (nextSplitFields.length !== splitFields.value.length) {
      splitFields.value = nextSplitFields
    }

    const filteredValues = valueFields.value.filter((value) => available.has(value))
    if (!filteredValues.length && firstNumeric) {
      valueFields.value = [firstNumeric]
    } else if (filteredValues.length !== valueFields.value.length) {
      valueFields.value = filteredValues
    }
  },
  { immediate: true },
)

const buildLabelFromFields = (
  row: FlatRow,
  fields: string[],
  fallback: string,
) => {
  if (!fields.length) return fallback
  return normalizeMermaidText(
    fields
      .map((field) => normalizeMermaidText(row[field], emptyLabelText.value || '(empty)'))
      .join(labelSeparator.value || ' / '),
    fallback,
  )
}

const seriesConfig = computed(() => {
  const warnings: string[] = []

  if (!categoryFields.value.length) {
    return {
      warnings: ['Choose a category field to build a chart.'],
      categories: [] as string[],
      series: [] as { name: string; values: number[] }[],
    }
  }

  const chartNeedsSingleSeries =
    chartType.value === 'pie' || chartType.value === 'bar' || chartType.value === 'line'
  const effectiveValueFields =
    aggregation.value === 'count'
      ? ['__count__']
      : splitFields.value.length
        ? valueFields.value.slice(0, 1)
        : valueFields.value

  if (splitFields.value.length && valueFields.value.length > 1) {
    warnings.push('Split-by uses the first selected numeric field to keep Mermaid output readable.')
  }

  if (maxSeries.value < 1) {
    warnings.push('Set the max series value to at least 1.')
  }

  if (!effectiveValueFields.length) {
    return {
      warnings: ['Choose at least one numeric field or switch the aggregation to count.'],
      categories: [] as string[],
      series: [] as { name: string; values: number[] }[],
    }
  }

  const buckets = new Map<string, Map<string, number[]>>()
  const categoryOrder: string[] = []

  parsedState.value.rows.forEach((row) => {
    const category = buildLabelFromFields(
      row,
      categoryFields.value,
      emptyLabelText.value || '(empty)',
    )
    if (!buckets.has(category)) {
      buckets.set(category, new Map())
      categoryOrder.push(category)
    }

    if (splitFields.value.length) {
      const seriesName = buildLabelFromFields(
        row,
        splitFields.value,
        emptyLabelText.value || '(empty)',
      )
      const metricKey = effectiveValueFields[0]
      const metricValue = metricKey === '__count__' ? 1 : toNumber(row[metricKey])
      if (metricValue === undefined) return
      const group = buckets.get(category)!
      const values = group.get(seriesName) || []
      values.push(metricValue)
      group.set(seriesName, values)
      return
    }

    effectiveValueFields.forEach((field) => {
      const seriesName = field === '__count__' ? 'Count' : normalizeMermaidText(field, 'Value')
      const metricValue = field === '__count__' ? 1 : toNumber(row[field])
      if (metricValue === undefined) return
      const group = buckets.get(category)!
      const values = group.get(seriesName) || []
      values.push(metricValue)
      group.set(seriesName, values)
    })
  })

  const aggregateValues = (values: number[]) => {
    if (!values.length) return 0
    if (aggregation.value === 'avg') return values.reduce((sum, entry) => sum + entry, 0) / values.length
    if (aggregation.value === 'min') return Math.min(...values)
    if (aggregation.value === 'max') return Math.max(...values)
    return values.reduce((sum, entry) => sum + entry, 0)
  }

  const seriesNames = Array.from(
    new Set(
      Array.from(buckets.values()).flatMap((group) => Array.from(group.keys())),
    ),
  )

  let categories = categoryOrder.map((label) => ({
    label,
    total: Array.from(buckets.get(label)?.values() || []).reduce(
      (sum, values) => sum + aggregateValues(values),
      0,
    ),
  }))

  if (hideZeroCategories.value) {
    categories = categories.filter((entry) => entry.total !== 0)
  }

  if (sortMode.value === 'label-asc') {
    categories = [...categories].sort((a, b) => a.label.localeCompare(b.label))
  } else if (sortMode.value === 'label-desc') {
    categories = [...categories].sort((a, b) => b.label.localeCompare(a.label))
  } else if (sortMode.value === 'value-desc') {
    categories = [...categories].sort((a, b) => b.total - a.total)
  } else if (sortMode.value === 'value-asc') {
    categories = [...categories].sort((a, b) => a.total - b.total)
  }

  let cappedCategories = limit.value > 0 ? categories.slice(0, limit.value) : categories
  const hiddenCategories = limit.value > 0 ? categories.slice(limit.value) : []

  if (includeEverythingElse.value && hiddenCategories.length) {
    cappedCategories = [
      ...cappedCategories,
      {
        label: normalizeMermaidText(everythingElseLabel.value, 'Everything else'),
        total: hiddenCategories.reduce((sum, entry) => sum + entry.total, 0),
      },
    ]
  }

  const categoryLabels = cappedCategories.map((entry) => entry.label)

  let series = seriesNames.map((name) => ({
    name,
    values: categoryLabels.map((label) => {
      if (
        includeEverythingElse.value &&
        hiddenCategories.length &&
        label === normalizeMermaidText(everythingElseLabel.value, 'Everything else')
      ) {
        return hiddenCategories.reduce(
          (sum, hiddenCategory) => sum + aggregateValues(buckets.get(hiddenCategory.label)?.get(name) || []),
          0,
        )
      }

      return aggregateValues(buckets.get(label)?.get(name) || [])
    }),
  }))

  if (hideZeroSeries.value) {
    series = series.filter((entry) => entry.values.some((value) => value !== 0))
  }

  if (maxSeries.value > 0 && series.length > maxSeries.value) {
    warnings.push(`Series were limited to the first ${maxSeries.value} entries.`)
    series = series.slice(0, maxSeries.value)
  }

  if (chartNeedsSingleSeries && series.length > 1) {
    warnings.push('This chart type uses the first available series. Switch to grouped bars or multi-line to compare more.')
    series = series.slice(0, 1)
  }

  if (chartType.value === 'combo' && series.length > 2) {
    warnings.push('Combo mode uses the first two series: one bar and one line.')
    series = series.slice(0, 2)
  }

  if (chartType.value === 'pie' && series.length > 1) {
    warnings.push('Pie charts use the first series only.')
    series = series.slice(0, 1)
  }

  return {
    warnings,
    categories: categoryLabels,
    series,
  }
})

const yAxisRange = computed(() => {
  const allValues = seriesConfig.value.series.flatMap((entry) => entry.values)
  const precision = Math.max(0, decimalPlaces.value)

  if (!allValues.length) {
    return { min: 0, max: 1, warning: undefined as string | undefined, precision }
  }

  if (yAxisMode.value === 'manual') {
    const min = Number(yAxisMin.value)
    const max = Number(yAxisMax.value)
    if (!Number.isFinite(min) || !Number.isFinite(max)) {
      return { min: 0, max: 1, warning: 'Manual axis values must be valid numbers.', precision }
    }
    if (min === max) {
      return { min, max: min + 1, warning: 'Y axis max must differ from min. Auto-adjusted by 1.', precision }
    }
    if (min > max) {
      return { min: max, max: min, warning: 'Y axis min was greater than max, so both values were swapped.', precision }
    }
    return { min, max, warning: undefined as string | undefined, precision }
  }

  const rawMin = Math.min(0, ...allValues)
  const rawMax = Math.max(0, ...allValues)
  const span = rawMax - rawMin || 1
  const padding = span * 0.1
  return {
    min: Number((rawMin - padding).toFixed(precision)),
    max: Number((rawMax + padding).toFixed(precision)),
    warning: undefined as string | undefined,
    precision,
  }
})

const mermaidCode = computed(() => {
  if (parsedState.value.error) return ''
  if (!parsedState.value.rows.length) return ''
  if (!seriesConfig.value.categories.length || !seriesConfig.value.series.length) return ''
  const precision = Math.max(0, decimalPlaces.value)

  if (chartType.value === 'pie') {
    const series = seriesConfig.value.series[0]
    return [
      `pie${pieShowData.value ? ' showData' : ''}`,
      ...(chartTitle.value.trim() ? [`  title ${escapeMermaidLabel(chartTitle.value.trim())}`] : []),
      ...seriesConfig.value.categories.map((label, index) => {
        const value = Number(series.values[index].toFixed(precision))
        return `  ${escapeMermaidLabel(label)} : ${value}`
      }),
    ].join('\n')
  }

  const lines = [
    'xychart-beta',
    ...(chartTitle.value.trim() ? [`  title ${escapeMermaidLabel(chartTitle.value.trim())}`] : []),
    `  x-axis ${escapeMermaidLabel(categoryFields.value.join(' / '), 'Category')} [${seriesConfig.value.categories.map((label) => escapeMermaidLabel(label)).join(', ')}]`,
    `  y-axis ${escapeMermaidLabel(yAxisLabel.value.trim() || 'Value', 'Value')} ${yAxisRange.value.min} --> ${yAxisRange.value.max}`,
  ]

  if (chartType.value === 'combo') {
    const [first, second = first] = seriesConfig.value.series
    if (includeSeriesComments.value) {
      lines.push(`  %% ${escapeMermaidComment(first.name, 'Bar series')}`)
    }
    lines.push(`  bar [${first.values.map((value) => Number(value.toFixed(precision))).join(', ')}]`)
    if (includeSeriesComments.value) {
      lines.push(`  %% ${escapeMermaidComment(second.name, 'Line series')}`)
    }
    lines.push(`  line [${second.values.map((value) => Number(value.toFixed(precision))).join(', ')}]`)
    return lines.join('\n')
  }

  const renderer =
    chartType.value === 'line' || chartType.value === 'multi-line' ? 'line' : 'bar'

  seriesConfig.value.series.forEach((series) => {
    if (includeSeriesComments.value) {
      lines.push(`  %% ${escapeMermaidComment(series.name)}`)
    }
    lines.push(`  ${renderer} [${series.values.map((value) => Number(value.toFixed(precision))).join(', ')}]`)
  })

  return lines.join('\n')
})

const stats = computed(() => ({
  rows: parsedState.value.rows.length,
  fields: fieldOptions.value.length,
  series: seriesConfig.value.series.length,
  format: parsedState.value.format.toUpperCase(),
}))

const applyPreset = (preset: ExamplePreset) => {
  inputFormat.value = preset.format
  source.value = preset.source
  chartType.value = preset.chartType
  chartTitle.value = preset.title
  yAxisLabel.value = preset.yAxisLabel || 'Value'
  categoryFields.value = [...preset.categoryFields]
  splitFields.value = [...(preset.splitFields || [])]
  valueFields.value = [...(preset.valueFields || [])]
  aggregation.value = preset.aggregation || 'sum'
}
</script>

<template>
  <UtilityShell
    control-title="Dataset"
    control-description="Paste JSON, YAML, or CSV, pick a chart type, and generate Mermaid charts from structured data."
    result-title="Mermaid Chart"
    result-description="Live Mermaid output, chart diagnostics, and reusable Mermaid code."
  >
    <template #controls>
      <div class="grid gap-3 md:grid-cols-[minmax(0,1fr)_auto]">
        <div>
          <label class="mb-1 block text-sm text-neutral-500 dark:text-neutral-400">Example preset</label>
          <Select
            v-model="selectedPresetId"
            :options="examplePresets"
            option-label="label"
            option-value="id"
            placeholder="Choose a chart preset"
            fluid
          />
          <p v-if="selectedPreset" class="mt-2 text-sm text-neutral-500 dark:text-neutral-400">
            {{ selectedPreset.description }}
          </p>
        </div>
        <div class="flex items-end">
          <Button
            @click="selectedPreset && applyPreset(selectedPreset)"
            :disabled="!selectedPreset"
            label="load preset"
            icon="ti ti-wand"
            severity="contrast"
            outlined
          />
        </div>
      </div>

      <div class="grid gap-3 md:grid-cols-2">
        <div>
          <label class="mb-1 block text-sm text-neutral-500 dark:text-neutral-400">Input format</label>
          <Select
            v-model="inputFormat"
            :options="formatOptions"
            option-label="label"
            option-value="value"
            fluid
          />
        </div>

        <div>
          <label class="mb-1 block text-sm text-neutral-500 dark:text-neutral-400">Chart type</label>
          <Select
            v-model="chartType"
            :options="chartTypeOptions"
            option-label="label"
            option-value="value"
            fluid
          />
        </div>

        <div>
          <label class="mb-1 block text-sm text-neutral-500 dark:text-neutral-400">Chart title</label>
          <InputText v-model="chartTitle" fluid placeholder="Revenue by Month" />
        </div>

        <div v-if="chartType !== 'pie'">
          <label class="mb-1 block text-sm text-neutral-500 dark:text-neutral-400">Y axis label</label>
          <InputText v-model="yAxisLabel" fluid placeholder="Value" />
        </div>
      </div>

      <div class="grid gap-3 md:grid-cols-2 xl:grid-cols-4">
        <div>
          <label class="mb-1 block text-sm text-neutral-500 dark:text-neutral-400">Decimals</label>
          <InputNumber v-model="decimalPlaces" :min="0" :max="6" fluid />
        </div>

        <div>
          <label class="mb-1 block text-sm text-neutral-500 dark:text-neutral-400">Y axis range</label>
          <Select
            v-model="yAxisMode"
            :options="yAxisModeOptions"
            option-label="label"
            option-value="value"
            fluid
          />
        </div>

        <div>
          <label class="mb-1 block text-sm text-neutral-500 dark:text-neutral-400">Empty label</label>
          <InputText v-model="emptyLabelText" fluid placeholder="(empty)" />
        </div>

        <div>
          <label class="mb-1 block text-sm text-neutral-500 dark:text-neutral-400">Field separator</label>
          <InputText v-model="labelSeparator" fluid placeholder=" / " />
        </div>
      </div>

      <div v-if="chartType !== 'pie' && yAxisMode === 'manual'" class="grid gap-3 md:grid-cols-2">
        <div>
          <label class="mb-1 block text-sm text-neutral-500 dark:text-neutral-400">Y axis min</label>
          <InputNumber v-model="yAxisMin" fluid />
        </div>
        <div>
          <label class="mb-1 block text-sm text-neutral-500 dark:text-neutral-400">Y axis max</label>
          <InputNumber v-model="yAxisMax" fluid />
        </div>
      </div>

      <div>
        <label class="mb-1 block text-sm text-neutral-500 dark:text-neutral-400">Source data</label>
        <Textarea v-model="source" auto-resize rows="18" fluid />
      </div>

      <div class="grid gap-3 md:grid-cols-2">
        <div>
          <label class="mb-1 block text-sm text-neutral-500 dark:text-neutral-400">Category / group fields</label>
          <MultiSelect
            v-model="categoryFields"
            :options="dimensionFieldOptions"
            option-label="label"
            option-value="value"
            display="chip"
            fluid
            placeholder="Select one or more fields"
          />
        </div>

        <div v-if="chartType !== 'pie'">
          <label class="mb-1 block text-sm text-neutral-500 dark:text-neutral-400">Split into series by fields</label>
          <MultiSelect
            v-model="splitFields"
            :options="dimensionFieldOptions"
            option-label="label"
            option-value="value"
            display="chip"
            fluid
            placeholder="Optional series grouping"
          />
        </div>

        <div class="md:col-span-2">
          <label class="mb-1 block text-sm text-neutral-500 dark:text-neutral-400">Numeric fields to visualize</label>
          <MultiSelect
            v-model="valueFields"
            :options="numericFieldOptions"
            option-label="label"
            option-value="value"
            display="chip"
            fluid
            placeholder="Pick one or more numeric fields"
          />
        </div>

        <div>
          <label class="mb-1 block text-sm text-neutral-500 dark:text-neutral-400">Aggregation</label>
          <Select
            v-model="aggregation"
            :options="aggregationOptions"
            option-label="label"
            option-value="value"
            fluid
          />
        </div>

        <div>
          <label class="mb-1 block text-sm text-neutral-500 dark:text-neutral-400">Sort groups</label>
          <Select
            v-model="sortMode"
            :options="sortOptions"
            option-label="label"
            option-value="value"
            fluid
          />
        </div>

        <div>
          <label class="mb-1 block text-sm text-neutral-500 dark:text-neutral-400">Max groups</label>
          <InputNumber v-model="limit" :min="1" :max="100" fluid />
        </div>

        <label
          class="flex items-center gap-2 rounded-xl border border-neutral-200 bg-white px-3 py-2 dark:border-neutral-700 dark:bg-neutral-900"
        >
          <Checkbox v-model="includeEverythingElse" binary />
          <span class="text-sm">Group hidden entries into “everything else”</span>
        </label>

        <div v-if="includeEverythingElse">
          <label class="mb-1 block text-sm text-neutral-500 dark:text-neutral-400">Everything else label</label>
          <InputText v-model="everythingElseLabel" fluid placeholder="Everything else" />
        </div>

        <div>
          <label class="mb-1 block text-sm text-neutral-500 dark:text-neutral-400">Max series</label>
          <InputNumber v-model="maxSeries" :min="1" :max="50" fluid />
        </div>

        <label
          class="flex items-center gap-2 rounded-xl border border-neutral-200 bg-white px-3 py-2 dark:border-neutral-700 dark:bg-neutral-900"
        >
          <Checkbox v-model="includeSeriesComments" binary />
          <span class="text-sm">Include series comments in Mermaid code</span>
        </label>

        <label
          class="flex items-center gap-2 rounded-xl border border-neutral-200 bg-white px-3 py-2 dark:border-neutral-700 dark:bg-neutral-900"
        >
          <Checkbox v-model="hideZeroSeries" binary />
          <span class="text-sm">Hide zero-only series</span>
        </label>

        <label
          class="flex items-center gap-2 rounded-xl border border-neutral-200 bg-white px-3 py-2 dark:border-neutral-700 dark:bg-neutral-900"
        >
          <Checkbox v-model="hideZeroCategories" binary />
          <span class="text-sm">Hide zero-only groups</span>
        </label>

        <label
          v-if="chartType === 'pie'"
          class="flex items-center gap-2 rounded-xl border border-neutral-200 bg-white px-3 py-2 dark:border-neutral-700 dark:bg-neutral-900"
        >
          <Checkbox v-model="pieShowData" binary />
          <span class="text-sm">Show values in the pie chart</span>
        </label>
      </div>

      <div v-if="fieldOptions.length" class="flex flex-wrap gap-2">
        <Tag
          v-for="field of fieldOptions"
          :key="field.value"
          :value="`${field.label}${field.numeric ? ' • number' : ' • text'}`"
          severity="contrast"
        />
      </div>
    </template>

    <template #result>
      <div v-if="parsedState.error" class="rounded-xl border border-red-200 bg-red-50 p-4 text-sm text-red-700 dark:border-red-900/60 dark:bg-red-950/40 dark:text-red-300">
        {{ parsedState.error }}
      </div>

      <div v-else-if="!parsedState.rows.length" class="rounded-xl border border-dashed border-neutral-300 bg-white/70 p-4 text-sm text-neutral-500 dark:border-neutral-700 dark:bg-neutral-900/40 dark:text-neutral-400">
        Add some structured data to generate a Mermaid chart.
      </div>

      <template v-else>
        <div class="grid gap-3 md:grid-cols-4">
          <div
            v-for="entry of [
              ['Format', stats.format],
              ['Rows', stats.rows],
              ['Fields', stats.fields],
              ['Series', stats.series],
            ]"
            :key="entry[0]"
            class="rounded-xl border border-neutral-200 bg-white p-4 dark:border-neutral-700 dark:bg-neutral-900"
          >
            <div class="text-sm text-neutral-500 dark:text-neutral-400">{{ entry[0] }}</div>
            <div class="mt-2 text-xl font-semibold">{{ entry[1] }}</div>
          </div>
        </div>

        <div v-if="seriesConfig.warnings.length" class="mt-4 flex flex-wrap gap-2">
          <Tag
            v-for="warning of seriesConfig.warnings"
            :key="warning"
            :value="warning"
            severity="warn"
          />
        </div>

        <div v-if="yAxisRange.warning" class="mt-4 flex flex-wrap gap-2">
          <Tag :value="yAxisRange.warning" severity="warn" />
        </div>

        <div class="mt-4 overflow-hidden rounded-xl border border-neutral-200 bg-white dark:border-neutral-700 dark:bg-neutral-900">
          <MermaidViewer v-if="mermaidCode" :mermaid-code="mermaidCode" />
          <div
            v-else
            class="p-4 text-sm text-neutral-500 dark:text-neutral-400"
          >
            Pick a category field and at least one usable numeric field to render a chart.
          </div>
        </div>

        <div class="mt-4 overflow-hidden rounded-xl border border-neutral-200 bg-white dark:border-neutral-700 dark:bg-neutral-900">
          <Highlighted :contents="mermaidCode || '%% Mermaid chart will appear here'" file-name="chart.mmd" />
        </div>
      </template>
    </template>

    <template #footer>
      <UtilityResultActions :result="mermaidCode" file-name="chart.mmd" />
    </template>
  </UtilityShell>
</template>
