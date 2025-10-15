<script setup lang="ts">
import { useAppStore } from '@/stores/app.ts'

const appStore = useAppStore()

const props = defineProps<{
  publicPastes?: boolean
  myPastes?: boolean
}>()

const showSearch = () => {
  appStore.searchShown = true
  appStore.searchShownEndpoints.publicPastes = props.publicPastes || false
  appStore.searchShownEndpoints.myPastes = props.myPastes || false
}
</script>

<template>
  <button
    class="flex cursor-pointer items-center gap-1 rounded-md border border-neutral-200 px-2 py-1 dark:border-neutral-700"
    @click="showSearch"
  >
    <i class="ti ti-search text-sm opacity-60" />
    <span class="text-sm opacity-60">Search</span>
  </button>
</template>
