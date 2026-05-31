<script lang="ts" setup>
import { computed } from 'vue'
import type { PasteAiInfo } from '@/types/paste.ts'
import SeverityScore from '@/components/paste/SeverityScore.vue'

const props = defineProps<{
  aiInfo: PasteAiInfo
}>()

const averageSeverity = computed(() => {
  const warnings = props.aiInfo.warnings || []
  if (!warnings.length) return 0

  return Math.round(warnings.reduce((sum, warning) => sum + warning.severity, 0) / warnings.length)
})
</script>

<template>
  <div
    id="paste-ai-summary"
    class="relative space-y-4 rounded-xl border border-neutral-200 bg-white p-4 dark:border-neutral-700 dark:bg-neutral-900"
  >
    <span
      class="absolute top-1 right-1 flex items-center gap-0.5 rounded-md bg-purple-500/20 px-2 py-0.5 text-sm font-bold text-purple-500"
    >
      <i aria-hidden="true" class="ti ti-sparkle text-xs" />
      <span>AI</span>
    </span>

    <div class="space-y-1">
      <h2 class="text-lg font-bold">Description</h2>
      <p>{{ aiInfo.description }}</p>
    </div>

    <div v-if="aiInfo.warnings?.length" class="space-y-2">
      <div class="flex items-center justify-between">
        <h2 class="text-lg font-bold">Code Safety</h2>

        <SeverityScore :severity="averageSeverity" />
      </div>

      <div class="space-y-2">
        <div
          v-for="(warning, i) of aiInfo.warnings"
          :key="i"
          class="flex flex-col gap-2 md:flex-row"
        >
          <div v-if="aiInfo.warnings.length > 1">
            <SeverityScore :severity="warning.severity" />
          </div>

          <p>
            {{ warning.description }}
          </p>
        </div>
      </div>
    </div>
  </div>
</template>
