<script setup lang="ts">
import MarkdownViewer from '@/components/previews/MarkdownViewer.vue'
import CSVViewer from '@/components/previews/CSVViewer.vue'

defineProps<{
  fileName?: string
  contents: string
  type: string
}>()
</script>
<template>
  <CSVViewer v-if="type === 'csv'" :csv="contents" />
  <MarkdownViewer v-else-if="type === 'markdown'" :markdown="contents" />
</template>
