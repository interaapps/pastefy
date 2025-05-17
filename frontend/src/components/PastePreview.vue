<script setup lang="ts">
import { defineAsyncComponent } from 'vue'

const LaTeXViewer = defineAsyncComponent(() => import('@/components/previews/LaTeXViewer.vue'))
const MarkdownViewer = defineAsyncComponent(
  () => import('@/components/previews/MarkdownViewer.vue'),
)
const CSVViewer = defineAsyncComponent(() => import('@/components/previews/CSVViewer.vue'))

defineProps<{
  fileName?: string
  contents: string
  type: string
}>()
</script>
<template>
  <CSVViewer v-if="type === 'csv'" :csv="contents" />
  <MarkdownViewer v-else-if="type === 'markdown'" :markdown="contents" />
  <LaTeXViewer v-else-if="type === 'tex'" :latex="contents" />
</template>
