<script setup lang="ts">
import { defineAsyncComponent } from 'vue'
import SVGViewer from '@/components/previews/SVGViewer.vue'

// const LaTeXViewer = defineAsyncComponent(() => import('@/components/previews/LaTeXViewer.vue'))
const MarkdownViewer = defineAsyncComponent(
  () => import('@/components/previews/MarkdownViewer.vue'),
)
const CSVViewer = defineAsyncComponent(() => import('@/components/previews/CSVViewer.vue'))
const MermaidViewer = defineAsyncComponent(() => import('@/components/previews/MermaidViewer.vue'))

defineProps<{
  fileName?: string
  contents: string
  type: string
}>()
</script>
<template>
  <CSVViewer v-if="type === 'csv'" :csv="contents" />
  <MarkdownViewer v-else-if="type === 'markdown'" :markdown="contents" />
  <MermaidViewer v-else-if="type === 'mermaid' || type === 'mmd'" :mermaid-code="contents" />
  <SVGViewer v-else-if="fileName?.endsWith('.svg')" :svg="contents" />
  <!-- <LaTeXViewer v-else-if="type === 'tex'" :latex="contents" /> -->
</template>
