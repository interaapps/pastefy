<script setup lang="ts">
import Highlighted from '@/components/Highlighted.vue'
import UtilityResultActions from '@/components/tools/utility/UtilityResultActions.vue'
import UtilityShell from '@/components/tools/utility/UtilityShell.vue'
import InputText from 'primevue/inputtext'
import Select from 'primevue/select'
import Textarea from 'primevue/textarea'
import { computed } from 'vue'
import { useStorage } from '@vueuse/core'

const props = defineProps<{
  server: 'nginx' | 'apache'
}>()

type LogRow = {
  ip: string
  time: string
  request: string
  method: string
  path: string
  protocol: string
  status: number
  size: number | null
  referrer: string
  userAgent: string
  raw: string
}

const sampleByServer = {
  nginx: `203.0.113.10 - - [18/Mar/2026:12:14:20 +0100] "GET / HTTP/1.1" 200 1240 "-" "Mozilla/5.0"
203.0.113.10 - - [18/Mar/2026:12:14:23 +0100] "GET /assets/app.js HTTP/1.1" 200 82431 "https://example.com/" "Mozilla/5.0"
203.0.113.11 - - [18/Mar/2026:12:14:27 +0100] "POST /api/login HTTP/1.1" 401 231 "https://example.com/login" "Mozilla/5.0"`,
  apache: `198.51.100.20 - - [18/Mar/2026:12:16:10 +0100] "GET / HTTP/1.1" 200 5120 "-" "Mozilla/5.0"
198.51.100.21 - - [18/Mar/2026:12:16:14 +0100] "GET /health HTTP/1.1" 200 16 "-" "curl/8.5.0"
198.51.100.22 - - [18/Mar/2026:12:16:19 +0100] "GET /admin HTTP/1.1" 403 296 "-" "Mozilla/5.0"`,
}

const source = useStorage(`pastefy-utility-${props.server}-logs-source`, sampleByServer[props.server])
const search = useStorage(`pastefy-utility-${props.server}-logs-search`, '')
const statusFilter = useStorage<'all' | '2xx' | '3xx' | '4xx' | '5xx'>(
  `pastefy-utility-${props.server}-logs-status`,
  'all',
)

const statusOptions = [
  { label: 'All statuses', value: 'all' as const },
  { label: '2xx', value: '2xx' as const },
  { label: '3xx', value: '3xx' as const },
  { label: '4xx', value: '4xx' as const },
  { label: '5xx', value: '5xx' as const },
]

const parseLine = (line: string) => {
  const match = line.match(
    /^(\S+)\s+\S+\s+\S+\s+\[([^\]]+)\]\s+"([^"]*)"\s+(\d{3})\s+(\S+)(?:\s+"([^"]*)"\s+"([^"]*)")?/,
  )
  if (!match) return undefined

  const requestParts = match[3].split(' ')
  return {
    ip: match[1],
    time: match[2],
    request: match[3],
    method: requestParts[0] || '-',
    path: requestParts[1] || '-',
    protocol: requestParts[2] || '-',
    status: Number(match[4]),
    size: match[5] === '-' ? null : Number(match[5]),
    referrer: match[6] || '-',
    userAgent: match[7] || '-',
    raw: line,
  } satisfies LogRow
}

const parsedRows = computed(() => {
  const rawLines = source.value.split(/\r?\n/).map((line) => line.trim()).filter(Boolean)
  const rows = rawLines.map(parseLine)
  return {
    parsed: rows.filter((row): row is LogRow => Boolean(row)),
    invalid: rows.filter((row) => !row).length,
  }
})

const filteredRows = computed(() => {
  const query = search.value.trim().toLowerCase()

  return parsedRows.value.parsed.filter((row) => {
    const matchesQuery =
      !query ||
      [row.ip, row.method, row.path, row.status, row.referrer, row.userAgent, row.time]
        .join(' ')
        .toLowerCase()
        .includes(query)

    if (!matchesQuery) return false
    if (statusFilter.value === 'all') return true
    return String(row.status).startsWith(statusFilter.value[0])
  })
})

const stats = computed(() => {
  const rows = filteredRows.value
  const errorRows = rows.filter((row) => row.status >= 400)
  return {
    requests: rows.length,
    uniqueIps: new Set(rows.map((row) => row.ip)).size,
    errors: errorRows.length,
    invalid: parsedRows.value.invalid,
  }
})

const topPaths = computed(() =>
  Object.entries(
    filteredRows.value.reduce<Record<string, number>>((acc, row) => {
      acc[row.path] = (acc[row.path] || 0) + 1
      return acc
    }, {}),
  )
    .sort((a, b) => b[1] - a[1])
    .slice(0, 6),
)

const topIps = computed(() =>
  Object.entries(
    filteredRows.value.reduce<Record<string, number>>((acc, row) => {
      acc[row.ip] = (acc[row.ip] || 0) + 1
      return acc
    }, {}),
  )
    .sort((a, b) => b[1] - a[1])
    .slice(0, 6),
)

const topUserAgents = computed(() =>
  Object.entries(
    filteredRows.value.reduce<Record<string, number>>((acc, row) => {
      acc[row.userAgent] = (acc[row.userAgent] || 0) + 1
      return acc
    }, {}),
  )
    .sort((a, b) => b[1] - a[1])
    .slice(0, 6),
)

const statusBreakdown = computed(() =>
  Object.entries(
    filteredRows.value.reduce<Record<string, number>>((acc, row) => {
      acc[String(row.status)] = (acc[String(row.status)] || 0) + 1
      return acc
    }, {}),
  )
    .sort((a, b) => b[1] - a[1])
    .slice(0, 8),
)

const statusDotClass = (status: number) => {
  if (status >= 500) return 'bg-red-500'
  if (status >= 400) return 'bg-amber-500'
  if (status >= 300) return 'bg-sky-500'
  return 'bg-emerald-500'
}

const exportResult = computed(() => JSON.stringify(filteredRows.value, null, 2))
</script>

<template>
  <UtilityShell
    :control-title="props.server === 'nginx' ? 'NGINX Access Logs' : 'Apache Access Logs'"
    control-description="Paste access logs, filter requests, and inspect parsed traffic details."
    result-title="Parsed Requests"
    result-description="Inspect status groups, top paths, and structured request rows."
    fill-height
  >
    <template #controls>
      <div class="grid gap-3 md:grid-cols-2">
        <InputText v-model="search" placeholder="Search IP, path, status, referrer..." fluid />
        <Select v-model="statusFilter" :options="statusOptions" option-label="label" option-value="value" fluid />
      </div>

      <div>
        <label class="mb-1 block text-sm text-neutral-500 dark:text-neutral-400">Access logs</label>
        <Textarea v-model="source" auto-resize rows="20" fluid />
      </div>
    </template>

    <template #result>
      <div class="space-y-4">
        <div class="grid gap-3 md:grid-cols-4">
          <div
            v-for="entry of [
              ['Requests', stats.requests],
              ['Unique IPs', stats.uniqueIps],
              ['Errors', stats.errors],
              ['Invalid lines', stats.invalid],
            ]"
            :key="entry[0]"
            class="rounded-xl border border-neutral-200 bg-white p-4 dark:border-neutral-700 dark:bg-neutral-900"
          >
            <div class="text-sm text-neutral-500 dark:text-neutral-400">{{ entry[0] }}</div>
            <div class="mt-2 text-2xl font-semibold">{{ entry[1] }}</div>
          </div>
        </div>

        <div class="grid gap-4 xl:grid-cols-[0.95fr_1.05fr]">
          <div class="space-y-4">
            <div class="rounded-xl border border-neutral-200 bg-white p-4 dark:border-neutral-700 dark:bg-neutral-900">
              <h3 class="font-semibold">Top status codes</h3>
              <div class="mt-3 space-y-2">
                <div
                  v-for="[status, count] of statusBreakdown"
                  :key="status"
                  class="flex items-center justify-between gap-3 rounded-lg bg-neutral-100 px-3 py-2 text-sm dark:bg-neutral-800"
                >
                  <span class="flex items-center gap-2">
                    <span class="h-2.5 w-2.5 rounded-full" :class="statusDotClass(Number(status))" />
                    <span>{{ status }}</span>
                  </span>
                  <span class="text-neutral-500 dark:text-neutral-400">{{ count }}</span>
                </div>
                <div v-if="!statusBreakdown.length" class="text-sm text-neutral-500 dark:text-neutral-400">
                  No parsed requests match the current filters.
                </div>
              </div>
            </div>

            <div class="rounded-xl border border-neutral-200 bg-white p-4 dark:border-neutral-700 dark:bg-neutral-900">
              <h3 class="font-semibold">Top paths</h3>
              <div class="mt-3 space-y-2">
                <div
                  v-for="[path, count] of topPaths"
                  :key="path"
                  class="flex items-center justify-between gap-3 rounded-lg bg-neutral-100 px-3 py-2 text-sm dark:bg-neutral-800"
                >
                  <span class="truncate">{{ path }}</span>
                  <span class="text-neutral-500 dark:text-neutral-400">{{ count }}</span>
                </div>
                <div v-if="!topPaths.length" class="text-sm text-neutral-500 dark:text-neutral-400">
                  No parsed requests match the current filters.
                </div>
              </div>
            </div>

            <div class="rounded-xl border border-neutral-200 bg-white p-4 dark:border-neutral-700 dark:bg-neutral-900">
              <h3 class="font-semibold">Top IPs</h3>
              <div class="mt-3 space-y-2">
                <div
                  v-for="[ip, count] of topIps"
                  :key="ip"
                  class="flex items-center justify-between gap-3 rounded-lg bg-neutral-100 px-3 py-2 text-sm dark:bg-neutral-800"
                >
                  <span class="truncate font-mono">{{ ip }}</span>
                  <span class="text-neutral-500 dark:text-neutral-400">{{ count }}</span>
                </div>
                <div v-if="!topIps.length" class="text-sm text-neutral-500 dark:text-neutral-400">
                  No parsed requests match the current filters.
                </div>
              </div>
            </div>

            <div class="rounded-xl border border-neutral-200 bg-white p-4 dark:border-neutral-700 dark:bg-neutral-900">
              <h3 class="font-semibold">Top user agents</h3>
              <div class="mt-3 space-y-2">
                <div
                  v-for="[agent, count] of topUserAgents"
                  :key="agent"
                  class="flex items-center justify-between gap-3 rounded-lg bg-neutral-100 px-3 py-2 text-sm dark:bg-neutral-800"
                >
                  <span class="truncate">{{ agent }}</span>
                  <span class="text-neutral-500 dark:text-neutral-400">{{ count }}</span>
                </div>
                <div v-if="!topUserAgents.length" class="text-sm text-neutral-500 dark:text-neutral-400">
                  No parsed requests match the current filters.
                </div>
              </div>
            </div>
          </div>

          <div class="rounded-xl border border-neutral-200 bg-white p-4 dark:border-neutral-700 dark:bg-neutral-900">
            <h3 class="font-semibold">Request table</h3>
            <div class="mt-3 overflow-auto">
              <table class="min-w-full text-left text-sm">
                <thead class="text-neutral-500 dark:text-neutral-400">
                  <tr>
                    <th class="px-2 py-2 font-medium">Status</th>
                    <th class="px-2 py-2 font-medium">Method</th>
                    <th class="px-2 py-2 font-medium">Path</th>
                    <th class="px-2 py-2 font-medium">IP</th>
                  </tr>
                </thead>
                <tbody>
                  <tr
                    v-for="row of filteredRows.slice(0, 40)"
                    :key="`${row.time}-${row.ip}-${row.path}`"
                    class="border-t border-neutral-200 dark:border-neutral-700"
                  >
                    <td class="px-2 py-2">
                      <span class="flex items-center gap-2">
                        <span class="h-2.5 w-2.5 rounded-full" :class="statusDotClass(row.status)" />
                        <span>{{ row.status }}</span>
                      </span>
                    </td>
                    <td class="px-2 py-2">{{ row.method }}</td>
                    <td class="max-w-[18rem] truncate px-2 py-2">{{ row.path }}</td>
                    <td class="px-2 py-2">{{ row.ip }}</td>
                  </tr>
                </tbody>
              </table>
            </div>
          </div>
        </div>

        <div class="overflow-hidden rounded-xl border border-neutral-200 bg-white dark:border-neutral-700 dark:bg-neutral-900">
          <Highlighted :contents="exportResult" file-name="access-log.json" />
        </div>
      </div>
    </template>

    <template #footer>
      <UtilityResultActions :result="exportResult" file-name="access-log.json" />
    </template>
  </UtilityShell>
</template>
