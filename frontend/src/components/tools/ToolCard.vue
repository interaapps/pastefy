<script setup lang="ts">
import ToolPinButton from '@/components/tools/ToolPinButton.vue'
import type { ToolCatalogEntry } from '@/utils/tool-categories.ts'

defineProps<{
  tool: ToolCatalogEntry
}>()
</script>

<template>
  <router-link
    :to="tool.to"
    class="group flex h-full flex-col justify-between rounded-xl border border-neutral-200 bg-neutral-100 p-5 transition-all hover:-translate-y-0.5 hover:bg-white dark:border-neutral-700 dark:bg-neutral-800 dark:hover:bg-neutral-900"
  >
    <div class="flex flex-col gap-4">
      <div class="flex items-start justify-between gap-3">
        <div class="flex items-center gap-3">
          <div
            class="flex h-12 w-12 items-center justify-center rounded-xl bg-white text-neutral-700 dark:bg-neutral-900 dark:text-neutral-200"
          >
            <i :class="`ti ti-${tool.icon} text-2xl`" />
          </div>
          <div>
            <h3 class="font-semibold">{{ tool.shortTitle }}</h3>
            <p class="text-xs uppercase tracking-[0.2em] text-neutral-400">
              {{ tool.meta }}
            </p>
          </div>
        </div>
        <div class="flex items-center gap-1">
          <ToolPinButton :kind="tool.kind" :slug="tool.slug" :label="false" text />
          <i
            class="ti ti-arrow-up-right text-lg text-neutral-300 transition-all group-hover:text-neutral-500 dark:group-hover:text-neutral-300"
          />
        </div>
      </div>
      <p class="text-sm text-neutral-600 dark:text-neutral-300">{{ tool.description }}</p>
    </div>

    <div class="mt-5 flex flex-wrap gap-2">
      <span
        v-for="keyword of tool.keywords.slice(0, 3)"
        :key="keyword"
        class="rounded-full bg-neutral-100 px-2.5 py-1 text-xs text-neutral-600 dark:bg-neutral-800 dark:text-neutral-300"
      >
        {{ keyword }}
      </span>
    </div>
  </router-link>
</template>
