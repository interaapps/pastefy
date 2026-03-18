<script setup lang="ts">
import Textarea from 'primevue/textarea'

defineProps<{
  label: string
  placeholder?: string
  rows?: number
}>()

const model = defineModel<string>({ required: true })
</script>

<template>
  <div>
    <label class="mb-1 block text-sm text-neutral-500 dark:text-neutral-400">{{ label }}</label>
    <Textarea
      v-model="model"
      auto-resize
      :rows="rows || 5"
      :placeholder="placeholder"
      fluid
    />
  </div>
</template>
