<script setup lang="ts">
import { computed, defineAsyncComponent } from 'vue'
import Tag from 'primevue/tag'
const Highlighted = defineAsyncComponent(() => import('@/components/Highlighted.vue'))

const props = defineProps<{
  request: string
}>()

const parsed = computed(() => {
  const source = props.request.trim()

  if (source.startsWith('curl ')) {
    const methodMatch = source.match(/-X\s+([A-Z]+)/)
    const urlMatch = source.match(/(https?:\/\/[^\s'"]+)/)
    const headerMatches = Array.from(source.matchAll(/-H\s+['"]([^'"]+)['"]/g)).map((m) => m[1])
    return {
      kind: 'curl',
      method: methodMatch?.[1] || 'GET',
      url: urlMatch?.[1] || '',
      headers: headerMatches,
      body: source.match(/-d\s+['"]([\s\S]*?)['"]/)?.[1] || '',
    }
  }

  const [requestLine, ...rest] = source.split('\n')
  const requestMatch = requestLine.match(/^(GET|POST|PUT|PATCH|DELETE|HEAD|OPTIONS)\s+(\S+)/)
  const separatorIndex = rest.findIndex((line) => line.trim() === '')
  const headerLines = (separatorIndex === -1 ? rest : rest.slice(0, separatorIndex)).filter(Boolean)
  const bodyLines = separatorIndex === -1 ? [] : rest.slice(separatorIndex + 1)

  return {
    kind: 'http',
    method: requestMatch?.[1] || '',
    url: requestMatch?.[2] || '',
    headers: headerLines,
    body: bodyLines.join('\n'),
  }
})
</script>

<template>
  <div class="overflow-hidden text-sm">
    <div
      class="flex flex-col gap-4 border-b border-neutral-200 bg-neutral-50 px-4 py-4 dark:border-neutral-700 dark:bg-neutral-900/70"
    >
      <div class="flex flex-wrap items-center justify-between gap-3">
        <div>
          <div class="text-xs font-semibold tracking-[0.2em] uppercase opacity-60">
            HTTP / cURL View
          </div>
          <div class="mt-1 text-xs opacity-60">Inspect request method, endpoint, headers, and body quickly</div>
        </div>
        <div class="flex flex-wrap gap-2">
          <Tag v-if="parsed.method" :value="parsed.method" severity="info" />
          <Tag :value="parsed.kind.toUpperCase()" severity="contrast" />
          <Tag :value="`${parsed.headers.length} headers`" severity="contrast" />
        </div>
      </div>

      <div class="rounded-xl border border-neutral-200 bg-white p-4 dark:border-neutral-700 dark:bg-neutral-900">
        <div class="mb-2 text-xs font-semibold tracking-[0.2em] uppercase opacity-60">Request</div>
        <div class="font-[JetBrains_Mono_Variable] text-sm">
          <div><span class="font-semibold">{{ parsed.method || 'UNKNOWN' }}</span> {{ parsed.url }}</div>
        </div>
      </div>
    </div>

    <div class="grid gap-0 border-b border-neutral-200 dark:border-neutral-700 md:grid-cols-2">
      <div class="border-b border-neutral-200 p-4 md:border-r md:border-b-0 dark:border-neutral-700">
        <div class="mb-3 text-xs font-semibold tracking-[0.2em] uppercase opacity-60">Headers</div>
        <div class="space-y-2 font-[JetBrains_Mono_Variable] text-[0.9rem]">
          <div v-if="parsed.headers.length === 0" class="opacity-50">No headers detected.</div>
          <div v-for="header in parsed.headers" :key="header" class="break-all">{{ header }}</div>
        </div>
      </div>
      <div class="p-4">
        <div class="mb-3 text-xs font-semibold tracking-[0.2em] uppercase opacity-60">Body</div>
        <div v-if="parsed.body">
          <Highlighted file-name="request.http" :contents="parsed.body" show-copy-button />
        </div>
        <div v-else class="font-[JetBrains_Mono_Variable] text-[0.9rem] opacity-50">No body detected.</div>
      </div>
    </div>

    <Highlighted class="max-h-[20rem] overflow-auto" file-name="request.http" :contents="request" show-copy-button />
  </div>
</template>
