<script setup lang="ts">
import InputText from 'primevue/inputtext'

const page = defineModel<number>('page', {
  required: true,
})

const search = defineModel<string>('search')
</script>
<template>
  <div class="flex justify-end gap-2">
    <InputText v-model="search" fluid />
    <Button icon="ti ti-filter" />
  </div>
</template>
