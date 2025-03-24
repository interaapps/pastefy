<script setup lang="ts">
import Button from 'primevue/button'

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
  <div class="flex justify-end gap-1">
    <Button
      @click="previous"
      :disabled="page === 1"
      class="btn btn-primary"
      size="small"
      outlined
      severity="contrast"
      label="previous"
    />
    <Button
      @click="next"
      class="btn btn-primary"
      size="small"
      outlined
      severity="contrast"
      label="next"
    />
  </div>
</template>
