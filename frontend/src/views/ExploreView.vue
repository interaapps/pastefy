<script setup lang="ts">
import PasteList from '@/components/lists/PasteList.vue'
import { useAsyncState, useTitle } from '@vueuse/core'
import Button from 'primevue/button'
import { client } from '@/main.ts'
import type { Tag } from '@/types/tags.ts'
import ErrorContainer from '@/components/ErrorContainer.vue'
import LoadingContainer from '@/components/LoadingContainer.vue'
import TagCard from '@/components/TagCard.vue'
import ShowSearchButton from '@/components/ShowSearchButton.vue'

useTitle(`Explore | Pastefy`)

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
      <h1 class="text-3xl font-bold">{{ $t('explore.title') }}</h1>
      <div>
        <ShowSearchButton public-pastes />
      </div>
    </div>

    <div class="mb-14 flex flex-col">
      <div class="mb-3 flex items-center justify-between gap-3">
        <h2 class="text-2xl font-bold">{{ $t('common.tags') }}</h2>
        <Button
          as="router-link"
          :to="{ name: 'tags' }"
          :label="$t('views.exploreTagsView.openExploreTags')"
          icon="ti ti-tags"
          size="small"
          severity="contrast"
          outlined
        />
      </div>
      <ErrorContainer v-if="tagsError" :error="tagsError as any" />
      <LoadingContainer v-else-if="tagsLoading" />
      <div v-else-if="tags" class="flex flex-col gap-3 md:flex-row">
        <TagCard v-for="tag of tags" :tag="tag" :key="tag.tag" />
      </div>
    </div>

    <div class="mb-14">
      <h2 class="mb-3 text-2xl font-bold">{{ $t('paste.trending') }}</h2>
      <PasteList
        route="/api/v2/public-pastes/trending"
        :params="{ trending: 'true', page_limit: 6 }"
      />
    </div>

    <div class="mb-14">
      <h2 class="mb-3 text-2xl font-bold">{{ $t('paste.pastes') }}</h2>
      <PasteList route="/api/v2/public-pastes/latest" :params="{ page_limit: 6 }" />
    </div>

    <div class="mb-14">
      <h2 class="mb-3 text-2xl font-bold">{{ $t('paste.allTimeTrending') }}</h2>
      <PasteList route="/api/v2/public-pastes/trending" :params="{ page_limit: 6 }" />
    </div>
  </main>
</template>
