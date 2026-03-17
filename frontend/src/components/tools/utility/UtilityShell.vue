<script setup lang="ts">
import { computed, inject, type Ref } from 'vue'

const props = defineProps<{
  controlTitle?: string
  controlDescription?: string
  resultTitle?: string
  resultDescription?: string
  fillHeight?: boolean
}>()

const inheritedFocusMode = inject<Ref<boolean> | undefined>('tool-focus-mode', undefined)
const shouldFillHeight = computed(() => props.fillHeight || inheritedFocusMode?.value || false)
</script>

<template>
  <div
    class="grid gap-5 xl:grid-cols-[0.95fr_1.05fr]"
    :class="shouldFillHeight ? 'min-h-0 flex-1' : ''"
  >
    <div
      class="flex flex-col overflow-hidden rounded-xl border border-neutral-200 bg-neutral-100 dark:border-neutral-700 dark:bg-neutral-800"
      :class="shouldFillHeight ? 'min-h-0 h-full' : 'min-h-[42rem]'"
    >
      <div class="border-b border-neutral-200 bg-white px-4 py-3 dark:border-neutral-700 dark:bg-neutral-900">
        <h2 class="font-semibold">{{ controlTitle || 'Tool' }}</h2>
        <p class="text-sm text-neutral-500 dark:text-neutral-400">
          {{ controlDescription || 'Interactive controls for this utility.' }}
        </p>
      </div>

      <div class="flex min-h-0 flex-1 flex-col gap-4 overflow-auto p-4">
        <slot name="controls" />
      </div>
    </div>

    <div
      class="flex flex-col overflow-hidden rounded-xl border border-neutral-200 bg-neutral-100 dark:border-neutral-700 dark:bg-neutral-800"
      :class="shouldFillHeight ? 'min-h-0 h-full' : 'min-h-[42rem]'"
    >
      <div class="border-b border-neutral-200 bg-white px-4 py-3 dark:border-neutral-700 dark:bg-neutral-900">
        <h2 class="font-semibold">{{ resultTitle || 'Result' }}</h2>
        <p class="text-sm text-neutral-500 dark:text-neutral-400">
          {{ resultDescription || 'Output and inspection details for the current tool state.' }}
        </p>
      </div>

      <div class="min-h-0 flex-1 overflow-auto p-4">
        <slot name="result" />
      </div>

      <slot name="footer" />
    </div>
  </div>
</template>
