<script setup lang="ts">
import { defineAsyncComponent } from 'vue'
import SVGViewer from '@/components/previews/SVGViewer.vue'

// const LaTeXViewer = defineAsyncComponent(() => import('@/components/previews/LaTeXViewer.vue'))
const MarkdownViewer = defineAsyncComponent(
  () => import('@/components/previews/MarkdownViewer.vue'),
)
const CSVViewer = defineAsyncComponent(() => import('@/components/previews/CSVViewer.vue'))
const MermaidViewer = defineAsyncComponent(() => import('@/components/previews/MermaidViewer.vue'))
const GeoJSONViewer = defineAsyncComponent(() => import('@/components/previews/GeoJSONViewer.vue'))
const DiffViewer = defineAsyncComponent(() => import('@/components/previews/DiffViewer.vue'))
const ICSViewer = defineAsyncComponent(() => import('@/components/previews/ICSViewer.vue'))
const RegexViewer = defineAsyncComponent(() => import('@/components/previews/RegexViewer.vue'))

defineProps<{
  fileName?: string
  contents: string
  type: string
  inEditor?: boolean
}>()
</script>
<template>
  <CSVViewer v-if="type === 'csv'" :csv="contents" :inEditor />
  <MarkdownViewer v-else-if="type === 'markdown'" :markdown="contents" :inEditor />
  <MermaidViewer
    v-else-if="type === 'mermaid' || type === 'mmd'"
    :mermaid-code="contents"
    :inEditor
  />
  <SVGViewer v-else-if="fileName?.endsWith('.svg')" :svg="contents" :inEditor />
  <GeoJSONViewer v-else-if="fileName?.endsWith('.geojson')" :geo-json="contents" :inEditor />
  <DiffViewer v-else-if="type === 'diff'" :diff="contents" :inEditor />
  <ICSViewer v-else-if="type === 'ics'" :ics="contents" :inEditor />
  <RegexViewer v-else-if="type === 'regex'" :regex="contents" :inEditor />
  <!-- <LaTeXViewer v-else-if="type === 'tex'" :latex="contents" /> -->
</template>
