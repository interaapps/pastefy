<script setup lang="ts">
import PasteList from '@/components/lists/PasteList.vue'
import { useAsyncState, useTitle } from '@vueuse/core'
import { useAppStore } from '@/stores/app.ts'
import { client } from '@/main.ts'
import type { Paste } from '@/types/paste.ts'
import type { Tag } from '@/types/tags.ts'
import ErrorContainer from '@/components/ErrorContainer.vue'
import LoadingContainer from '@/components/LoadingContainer.vue'
import TagCard from '@/components/TagCard.vue'
import Pagination from '@/components/Pagination.vue'

useTitle(`Explore | Pastefy`)

const appStore = useAppStore()

const showSearch = () => {
  appStore.searchShown = true
  appStore.searchShownEndpoints.publicPastes = true
  appStore.searchShownEndpoints.myPastes = false
}

const {
  isLoading: tagsLoading,
  state: tags,
  error: tagsError,
} = useAsyncState(async () => {
  return (
    await client.get('/api/v2/public/tags', {
      params: {
        page_limit: 4,
      },
    })
  ).data as Tag[]
}, undefined)
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

    <div class="mb-14 flex flex-col">
      <ErrorContainer v-if="tagsError" :error="tagsError as any" />
      <LoadingContainer v-else-if="tagsLoading" />
      <div v-else-if="tags" class="flex flex-col gap-3 md:flex-row">
        <TagCard v-for="tag of tags" :tag="tag" :key="tag.tag" />
      </div>
    </div>

    <div class="mb-14">
      <h2 class="mb-3 text-2xl font-bold">Pastes</h2>
      <PasteList route="/api/v2/public-pastes/latest" :params="{ page_limit: 3 }" />
    </div>

    <div class="mb-14">
      <h2 class="mb-3 text-2xl font-bold">Trending</h2>
      <PasteList
        route="/api/v2/public-pastes/trending"
        :params="{ trending: 'true', page_limit: 3 }"
      />
    </div>

    <div class="mb-14">
      <h2 class="mb-3 text-2xl font-bold">All time Trending</h2>
      <PasteList route="/api/v2/public-pastes/trending" :params="{ page_limit: 3 }" />
    </div>
  </main>
</template>
