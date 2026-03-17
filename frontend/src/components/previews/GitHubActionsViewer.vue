<script setup lang="ts">
import { computed, defineAsyncComponent } from 'vue'
import Tag from 'primevue/tag'
const Highlighted = defineAsyncComponent(() => import('@/components/Highlighted.vue'))

const props = defineProps<{
  workflow: string
}>()

const triggers = computed(() =>
  Array.from(props.workflow.matchAll(/^\s{0,2}([a-z_]+)\s*:/gim))
    .map((match) => match[1])
    .filter((name) => ['push', 'pull_request', 'workflow_dispatch', 'schedule'].includes(name)),
)

const jobs = computed(() =>
  Array.from(props.workflow.matchAll(/^\s{2}([a-zA-Z0-9_-]+)\s*:\s*$/gm)).map((match) => ({
    name: match[1],
  })),
)

const steps = computed(() =>
  Array.from(props.workflow.matchAll(/^\s{6,}-\s+name:\s+(.+)$/gm)).map((match) => match[1]),
)
</script>

<template>
  <div class="overflow-hidden text-sm">
    <div
      class="flex flex-col gap-4 border-b border-neutral-200 bg-neutral-50 px-4 py-4 dark:border-neutral-700 dark:bg-neutral-900/70"
    >
      <div class="flex flex-wrap items-center justify-between gap-3">
        <div>
          <div class="text-xs font-semibold tracking-[0.2em] uppercase opacity-60">
            GitHub Actions
          </div>
          <div class="mt-1 text-xs opacity-60">Workflow triggers, jobs, and named steps at a glance</div>
        </div>
        <div class="flex flex-wrap gap-2">
          <Tag :value="`${jobs.length} jobs`" severity="contrast" />
          <Tag :value="`${steps.length} steps`" severity="contrast" />
        </div>
      </div>

      <div v-if="triggers.length" class="flex flex-wrap gap-2">
        <Tag v-for="trigger in triggers" :key="trigger" :value="trigger" severity="info" />
      </div>
    </div>

    <div class="grid gap-0 border-b border-neutral-200 dark:border-neutral-700 md:grid-cols-2">
      <div class="border-b border-neutral-200 p-4 md:border-r md:border-b-0 dark:border-neutral-700">
        <div class="mb-3 text-xs font-semibold tracking-[0.2em] uppercase opacity-60">Jobs</div>
        <div class="flex flex-wrap gap-2">
          <Tag v-for="job in jobs" :key="job.name" :value="job.name" severity="contrast" />
        </div>
      </div>
      <div class="p-4">
        <div class="mb-3 text-xs font-semibold tracking-[0.2em] uppercase opacity-60">Named Steps</div>
        <div class="space-y-2 font-[JetBrains_Mono_Variable] text-[0.9rem]">
          <div v-if="steps.length === 0" class="opacity-50">No named steps detected.</div>
          <div v-for="step in steps.slice(0, 16)" :key="step" class="break-all">{{ step }}</div>
        </div>
      </div>
    </div>

    <Highlighted class="max-h-[24rem] overflow-auto" file-name="workflow.yml" :contents="workflow" show-copy-button />
  </div>
</template>
