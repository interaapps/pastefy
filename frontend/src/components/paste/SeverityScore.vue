<script setup lang="ts">
defineProps<{
  severity: number
  single?: boolean
}>()

function getSeverityColor(severity: number) {
  if (severity <= 2) return 'bg-green-500/10 text-green-500'

  if (severity <= 5) return 'bg-yellow-500/10 text-yellow-500'

  if (severity <= 8) return 'bg-orange-500/10 text-orange-500'

  return 'bg-red-500/10 text-red-500'
}

function getSeverityLabel(severity: number) {
  if (severity <= 2) return 'Harmless'

  if (severity <= 5) return 'Suspicious'

  if (severity <= 8) return 'Likely Harmful'

  return 'Severe Malware'
}
</script>
<template>
  <span class="rounded-md px-2 py-1 font-mono text-xs" :class="getSeverityColor(severity)">
    <template v-if="single">Code Safety · </template>{{ severity }}/10
    <template v-if="!single">· {{ getSeverityLabel(severity) }}</template>
  </span>
</template>
