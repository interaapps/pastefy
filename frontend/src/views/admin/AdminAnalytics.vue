<script setup lang="ts">
import { useTitle } from '@vueuse/core'
import AnalyticsDashboard from '@/components/analytics/AnalyticsDashboard.vue'

useTitle('Analytics - Admin | pastefy')
</script>

<template>
  <AnalyticsDashboard
    endpoint="/api/v2/analytics/admin"
    title="Admin analytics"
    description="Analyze paste traffic across the instance."
  />
</template>
