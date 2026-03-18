<script setup lang="ts">
import Checkbox from 'primevue/checkbox'

defineProps<{
  label: string
  help?: string
}>()

const model = defineModel<boolean>({ required: true })
</script>

<template>
  <label class="flex items-start gap-2 rounded-xl border border-neutral-200 bg-white px-3 py-2 dark:border-neutral-700 dark:bg-neutral-900">
    <Checkbox v-model="model" binary class="mt-0.5" />
    <span class="min-w-0">
      <span class="block text-sm">{{ label }}</span>
      <span v-if="help" class="mt-0.5 block text-xs text-neutral-500 dark:text-neutral-400">
        {{ help }}
      </span>
    </span>
  </label>
</template>
