<script setup lang="ts">
import PasteVisibilityIcon from '@/components/PasteVisibilityIcon.vue'
import Select from 'primevue/select'

const filters = defineModel<{
  visibility: string
}>('filters', { required: true })
</script>
<template>
  <div>
    <Select
      :options="['UNLISTED', 'PUBLIC', 'PRIVATE']"
      v-model="filters.visibility"
      size="small"
      show-clear
      fluid
      class="min-h-[2.1rem]"
    >
      <template #value="{ value: option }">
        <div class="flex items-center gap-1">
          <PasteVisibilityIcon :visibility="option" class="text-lg" />
          <span>{{ option }}</span>
        </div>
      </template>
      <template #option="{ option }">
        <div class="flex items-center gap-1">
          <PasteVisibilityIcon :visibility="option" class="text-lg" />
          <span>{{ option }}</span>
        </div>
      </template>
    </Select>
  </div>
</template>
