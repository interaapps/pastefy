<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { useTitle } from '@vueuse/core'
import AnalyticsDashboard from '@/components/analytics/AnalyticsDashboard.vue'

useTitle('Paste analytics | pastefy')
const route = useRoute()
const endpoint = computed(() => `/api/v2/analytics/pastes/${route.params.paste as string}`)
</script>

<template>
  <div class="mx-auto max-w-[1200px]">
    <AnalyticsDashboard
      :endpoint="endpoint"
      title="Paste analytics"
      description="Traffic for this paste, including page and RAW visits."
    />
  </div>
</template>
