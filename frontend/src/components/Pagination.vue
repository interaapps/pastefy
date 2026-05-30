<script setup lang="ts">
import Button from 'primevue/button'

defineProps<{
  loading?: boolean
}>()

const page = defineModel<number>('page', {
  required: true,
})

const next = () => {
  page.value++
}
const previous = () => {
  page.value--
}
</script>
<template>
  <div class="flex items-center justify-between gap-3">
    <div class="flex items-center gap-2 text-sm text-neutral-500 dark:text-neutral-400">
      <span>{{ $t('common.page', { page }) }}</span>
    </div>
    <div class="flex items-center gap-2">
      <Button
        @click="previous"
        :disabled="page === 1"
        size="small"
        outlined
        severity="contrast"
        :label="$t('common.previous')"
      />
      <Button
        @click="next"
        :disabled="loading"
        size="small"
        outlined
        severity="contrast"
        icon-pos="right"
        :label="$t('common.next')"
      />
    </div>
  </div>
</template>
