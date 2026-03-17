<script setup lang="ts">
import { computed, ref } from 'vue'
import InputText from 'primevue/inputtext'
import Select from 'primevue/select'
import Tag from 'primevue/tag'

const props = defineProps<{
  logs: string
}>()

const search = ref('')
const levelFilter = ref<'ALL' | 'ERROR' | 'WARN' | 'INFO' | 'DEBUG'>('ALL')

const entries = computed(() =>
  props.logs.split('\n').map((line, index) => {
    const levelMatch = line.match(/\b(ERROR|WARN|WARNING|INFO|DEBUG|TRACE|FATAL)\b/i)
    const level = (levelMatch?.[1] || 'RAW').toUpperCase().replace('WARNING', 'WARN')
    return { index, line, level }
  }),
)

const filteredEntries = computed(() => {
  return entries.value.filter((entry) => {
    if (levelFilter.value !== 'ALL' && entry.level !== levelFilter.value) return false
    if (search.value.trim() && !entry.line.toLowerCase().includes(search.value.trim().toLowerCase()))
      return false
    return true
  })
})

const stats = computed(() => {
  const count = (level: string) => entries.value.filter((entry) => entry.level === level).length
  return {
    error: count('ERROR') + count('FATAL'),
    warn: count('WARN'),
    info: count('INFO'),
    debug: count('DEBUG') + count('TRACE'),
  }
})

const levelOptions = ['ALL', 'ERROR', 'WARN', 'INFO', 'DEBUG']

const levelClass = (level: string) => {
  if (level === 'ERROR' || level === 'FATAL') return 'text-red-600 dark:text-red-300'
  if (level === 'WARN') return 'text-amber-600 dark:text-amber-300'
  if (level === 'INFO') return 'text-sky-600 dark:text-sky-300'
  if (level === 'DEBUG' || level === 'TRACE') return 'text-emerald-600 dark:text-emerald-300'
  return 'text-neutral-500 dark:text-neutral-400'
}
</script>

<template>
  <div class="overflow-hidden text-sm">
    <div
      class="flex flex-col gap-4 border-b border-neutral-200 bg-neutral-50 px-4 py-4 dark:border-neutral-700 dark:bg-neutral-900/70"
    >
      <div class="flex flex-wrap items-center justify-between gap-3">
        <div>
          <div class="text-xs font-semibold tracking-[0.2em] uppercase opacity-60">Log View</div>
          <div class="mt-1 text-xs opacity-60">Filter levels, search incidents, and scan output faster</div>
        </div>
        <div class="flex flex-wrap gap-2">
          <Tag :value="`${stats.error} errors`" severity="danger" />
          <Tag :value="`${stats.warn} warnings`" severity="warn" />
          <Tag :value="`${stats.info} info`" severity="info" />
          <Tag :value="`${stats.debug} debug`" severity="contrast" />
        </div>
      </div>

      <div class="grid gap-3 xl:grid-cols-[minmax(0,1fr)_180px]">
        <InputText v-model="search" placeholder="Search log lines" fluid />
        <Select v-model="levelFilter" :options="levelOptions" fluid />
      </div>
    </div>

    <div class="max-h-[42rem] overflow-auto font-[JetBrains_Mono_Variable] text-[0.9rem]">
      <div v-if="filteredEntries.length === 0" class="p-4 opacity-60">No matching log lines.</div>
      <div
        v-for="entry in filteredEntries"
        :key="entry.index"
        class="grid grid-cols-[auto_auto_minmax(0,1fr)] gap-3 border-b border-neutral-100 px-4 py-2 last:border-b-0 dark:border-neutral-800"
      >
        <span class="text-right text-xs opacity-40">{{ entry.index + 1 }}</span>
        <span class="text-xs font-semibold" :class="levelClass(entry.level)">{{ entry.level }}</span>
        <span class="break-all">{{ entry.line }}</span>
      </div>
    </div>
  </div>
</template>
