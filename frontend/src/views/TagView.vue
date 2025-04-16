<script setup lang="ts">
import { useAsyncState, useTitle } from '@vueuse/core'
import { useRoute } from 'vue-router'
import { client } from '@/main.ts'
import type { Folder } from '@/types/folder.ts'
import type { PublicUser } from '@/types/user.ts'
import ErrorContainer from '@/components/ErrorContainer.vue'
import LoadingContainer from '@/components/LoadingContainer.vue'
import PasteList from '@/components/lists/PasteList.vue'
import type { Tag } from '@/types/tags.ts'

useTitle(`Tag | Pastefy`)

const route = useRoute()

const {
  isLoading,
  state: tag,
  error,
} = useAsyncState(async () => {
  const tag = (await client.get(`/api/v2/public/tags/${route.params.tag}` as string)).data as Tag

  useTitle(`${tag.display_name || tag.tag} | Pastefy`)
  return tag
}, undefined)
</script>
<template>
  <main class="mx-auto w-full max-w-[1200px]">
    <ErrorContainer v-if="error" :error="error as any" />
    <LoadingContainer v-else-if="isLoading" class="flex items-center justify-center p-3" />
    <div v-else-if="tag">
      <div
        class="mb-9 flex items-center justify-between gap-3 rounded-md border border-neutral-200 object-cover p-3 dark:border-neutral-700"
      >
        <div class="flex flex-col gap-2 pl-2">
          <h1 class="flex items-center gap-2 text-xl font-bold">
            <span>{{ tag.display_name || tag.tag }}</span>
            <i v-if="'icon' in tag" :class="`ti ti-${tag.icon} text-md`" />
          </h1>
          <div class="flex flex-col gap-1 text-sm font-bold">
            <p class="max-w-[40rem] text-balance opacity-70" v-if="'description' in tag">
              {{ tag.description }}
            </p>
            <span class="opacity-50"> Pastes: {{ tag.paste_count }} </span>
            <a
              v-if="'website' in tag && tag.website"
              :href="tag.website"
              target="_blank"
              class="text-primary-500"
            >
              {{ tag.website }}
            </a>
          </div>
        </div>

        <img
          :src="tag.image_url"
          class="h-[7rem] w-[14rem] rounded-md border border-neutral-200 object-cover dark:border-neutral-700"
        />
      </div>

      <div>
        <h2 class="mb-3 text-2xl font-bold">Public Pastes</h2>
        <PasteList route="/api/v2/public-pastes/latest" :params="{ filter_tags: tag.tag }" />
      </div>
    </div>
  </main>
</template>
