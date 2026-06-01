<script setup lang="ts">
import { useTitle } from '@vueuse/core'
import AnalyticsDashboard from '@/components/analytics/AnalyticsDashboard.vue'

useTitle('My analytics | pastefy')
</script>

<template>
  <div class="mx-auto max-w-[1200px]">
    <AnalyticsDashboard
      endpoint="/api/v2/analytics/user"
      title="My analytics"
      description="Analyze visits across your pastes."
    />
  </div>
</template>
