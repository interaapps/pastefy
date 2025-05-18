<script setup lang="ts">
import Button from 'primevue/button'
import Divider from 'primevue/divider'
import InputText from 'primevue/inputtext'
import { useUrlSearchParams } from '@vueuse/core'
import PasteFilters from '@/components/filters/PasteFilters.vue'

const params = useUrlSearchParams<{
  search: string
}>('history')
</script>

<template>
  <div class="grid h-full w-full grid-cols-[300px_1fr]">
    <div class="h-full border-r border-neutral-300 p-4">
      <h1 class="mb-4 px-3 font-bold">Search</h1>
      <div class="flex flex-col gap-1">
        <Button
          as="router-link"
          size="small"
          label="pastes"
          icon="ti ti-script"
          text
          fluid
          class="justify-start"
          severity="contrast"
        />
        <Button
          as="router-link"
          size="small"
          label="users"
          icon="ti ti-users"
          text
          fluid
          class="justify-start"
          severity="contrast"
        />
        <Button
          size="small"
          label="tags"
          icon="ti ti-tag"
          text
          fluid
          class="justify-start"
          severity="contrast"
        />
        <Divider />
        <PasteFilters v-model:filters="params" />
      </div>
    </div>
    <div class="flex w-full flex-col">
      <div class="border-b border-neutral-300 px-5 py-2">
        <InputText autofocus size="small" v-model="params.search" fluid />
      </div>
      <div class="p-5">
        <router-view v-model:search="params.search" />
      </div>
    </div>
  </div>
</template>

<style scoped></style>
