<script setup lang="ts">
defineProps<{
  icon?: string
  label: string | number
  inBox?: boolean
}>()
</script>
<template>
  <span
    class="flex items-center gap-0.5 text-sm"
    :class="{
      'rounded-full border border-neutral-300 bg-neutral-200 px-2 dark:border-neutral-600 dark:bg-neutral-700':
        inBox,
    }"
  >
    <i v-if="icon" :class="`ti ti-${icon}`" />
    <span>{{ label }}</span>
  </span>
</template>
