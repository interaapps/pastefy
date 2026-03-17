<script setup lang="ts">
import { computed, defineAsyncComponent } from 'vue'
import Tag from 'primevue/tag'
const Highlighted = defineAsyncComponent(() => import('@/components/Highlighted.vue'))

const props = defineProps<{
  config: string
}>()

const alerts = computed(() =>
  Array.from(props.config.matchAll(/\b(alert|title)\s*[:=]\s*["']?([^"'\n]+)["']?/gim)).map(
    (match) => ({
      kind: match[1].toLowerCase(),
      name: match[2].trim(),
    }),
  ),
)

const expressions = computed(
  () => Array.from(props.config.matchAll(/\b(expr|expression)\s*[:=]\s*(.+)$/gim)).length,
)

const panels = computed(() => Array.from(props.config.matchAll(/"panels"\s*:/gim)).length)
</script>

<template>
  <div class="overflow-hidden text-sm">
    <div
      class="flex flex-col gap-4 border-b border-neutral-200 bg-neutral-50 px-4 py-4 dark:border-neutral-700 dark:bg-neutral-900/70"
    >
      <div class="flex flex-wrap items-center justify-between gap-3">
        <div>
          <div class="text-xs font-semibold tracking-[0.2em] uppercase opacity-60">
            Monitoring Rules
          </div>
          <div class="mt-1 text-xs opacity-60">Prometheus, Grafana, and alert rule overviews</div>
        </div>
        <div class="flex flex-wrap gap-2">
          <Tag :value="`${alerts.length} alerts/titles`" severity="contrast" />
          <Tag :value="`${expressions} expressions`" severity="contrast" />
          <Tag :value="`${panels} panel groups`" severity="contrast" />
        </div>
      </div>

      <div v-if="alerts.length" class="flex flex-wrap gap-2">
        <Tag
          v-for="alert in alerts.slice(0, 20)"
          :key="`${alert.kind}-${alert.name}`"
          :value="alert.name"
          :severity="alert.kind === 'alert' ? 'warn' : 'info'"
        />
      </div>
    </div>

    <Highlighted class="max-h-[30rem] overflow-auto" file-name="alerts.yml" :contents="config" show-copy-button />
  </div>
</template>
