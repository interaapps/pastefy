<script setup lang="ts">
import PasteList from '@/components/lists/PasteList.vue'
import { useTitle } from '@vueuse/core'
import { useAppStore } from '@/stores/app.ts'

useTitle(`Explore | Pastefy`)

const appStore = useAppStore()

const showSearch = () => {
  appStore.searchShown = true
  appStore.searchShownEndpoints.publicPastes = true
  appStore.searchShownEndpoints.myPastes = false
}
</script>

<template>
  <main class="mx-auto w-full max-w-[1200px]">
    <div class="mb-8 flex items-center justify-between">
      <h1 class="text-3xl font-bold">Explore Pastefy</h1>
      <div>
        <button
          class="flex cursor-pointer items-center gap-1 rounded-md border border-neutral-200 px-2 py-1 dark:border-neutral-700"
          @click="showSearch"
        >
          <i class="ti ti-search text-sm opacity-60" />
          <span class="text-sm opacity-60">Search</span>
        </button>
      </div>
    </div>

    <div class="mb-14">
      <h2 class="mb-3 text-2xl font-bold">Pastes</h2>
      <PasteList route="/api/v2/public-pastes/latest" />
    </div>

    <div class="mb-14">
      <h2 class="mb-3 text-2xl font-bold">Trending</h2>
      <PasteList route="/api/v2/public-pastes/trending" :params="{ trending: 'true' }" />
    </div>

    <div class="mb-14">
      <h2 class="mb-3 text-2xl font-bold">All time Trending</h2>
      <PasteList route="/api/v2/public-pastes/trending" />
    </div>
  </main>
</template>
