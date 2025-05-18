<script setup lang="ts">
import { computed } from 'vue'
import 'diff2html/bundles/css/diff2html.min.css'
import { html } from 'diff2html'

const props = defineProps<{
  diff: string
  inEditor?: boolean
}>()

const renderedDiff = computed(() => html(props.diff, {}))
</script>

<template>
  <div class="p-3">
    <div v-html="renderedDiff" class="diff-container m-[-1px]" />
  </div>
</template>

<style scoped>
.diff-container {
  font-family: monospace;
  overflow-x: auto;
  max-width: 100%;
}
</style>
