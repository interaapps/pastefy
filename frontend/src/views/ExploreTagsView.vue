<script setup lang="ts">
import Button from 'primevue/button'
import InputText from 'primevue/inputtext'
import Select from 'primevue/select'
import { useTranslation } from 'i18next-vue'
import { useDebounceFn, useTitle } from '@vueuse/core'
import { useRouteQuery } from '@vueuse/router'
import { computed, ref, watch } from 'vue'

import ErrorContainer from '@/components/ErrorContainer.vue'
import LoadingContainer from '@/components/LoadingContainer.vue'
import { client } from '@/main.ts'
import type { Tag } from '@/types/tags.ts'
import { formatCompactNumber } from '@/utils/format.ts'

type TagSort = 'popular' | 'name' | 'rich'

const { t } = useTranslation()

useTitle('Explore Tags | Pastefy')

const search = useRouteQuery<string>('search', '')
const sort = useRouteQuery<TagSort>('sort', 'popular')
const pageLimit = useRouteQuery('page_limit', 60, { transform: Number })

const searchInput = ref(search.value)
const tags = ref<Tag[]>([])
const isLoading = ref(false)
const error = ref<unknown>(null)

const sortOptions = computed(() => [
  { label: t('views.exploreTagsView.sortOptions.popular'), value: 'popular' as TagSort },
  { label: t('views.exploreTagsView.sortOptions.name'), value: 'name' as TagSort },
  { label: t('views.exploreTagsView.sortOptions.rich'), value: 'rich' as TagSort },
])

const normalizedSearch = computed(() => search.value.trim().toLowerCase())
const normalizedPageLimit = computed(() =>
  Math.min(Math.max(Number(pageLimit.value) || 60, 1), 100),
)

const loadTags = async () => {
  isLoading.value = true
  error.value = null

  try {
    tags.value = (
      await client.get('/api/v2/public/tags', {
        params: {
          search: search.value.trim() || undefined,
          page_limit: normalizedPageLimit.value,
        },
      })
    ).data as Tag[]
  } catch (caught) {
    error.value = caught
  } finally {
    isLoading.value = false
  }
}

const commitSearch = useDebounceFn((value: string) => {
  search.value = value.trim()
}, 250)

const metadataScore = (tag: Tag) =>
  Number(Boolean(tag.image_url)) * 4 +
  Number(Boolean(tag.description)) * 3 +
  Number(Boolean(tag.icon)) * 2 +
  Number(Boolean(tag.website))

const matchesClientSearch = (tag: Tag) => {
  if (!normalizedSearch.value) return true

  return [tag.tag, tag.display_name || '', tag.description || '', tag.website || '']
    .join(' ')
    .toLowerCase()
    .includes(normalizedSearch.value)
}

const sortedTags = computed(() => {
  const values = tags.value.filter(matchesClientSearch)

  return [...values].sort((left, right) => {
    if (sort.value === 'name') {
      return (left.display_name || left.tag).localeCompare(right.display_name || right.tag)
    }

    if (sort.value === 'rich') {
      const richDiff = metadataScore(right) - metadataScore(left)
      if (richDiff !== 0) return richDiff
    }

    return (right.paste_count || 0) - (left.paste_count || 0)
  })
})

const totalPastes = computed(() => tags.value.reduce((sum, tag) => sum + (tag.paste_count || 0), 0))

const canLoadMore = computed(
  () => tags.value.length >= normalizedPageLimit.value && normalizedPageLimit.value < 100,
)

const loadMore = () => {
  pageLimit.value = Math.min(normalizedPageLimit.value + 40, 100)
}

watch(
  () => search.value,
  (value) => {
    if (value !== searchInput.value) searchInput.value = value
    void loadTags()
  },
)

watch(
  () => normalizedPageLimit.value,
  () => {
    void loadTags()
  },
)

void loadTags()
</script>

<template>
  <main class="mx-auto flex w-full max-w-[1200px] flex-col gap-8">
    <section>
      <h1 class="max-w-[18ch] text-4xl leading-none font-bold md:text-5xl">
        {{ $t('views.exploreTagsView.title') }}
      </h1>
    </section>

    <section
      class="grid gap-3 rounded-xl border border-neutral-200 bg-neutral-100 p-4 md:grid-cols-[1fr_14rem_auto] dark:border-neutral-700 dark:bg-neutral-800"
    >
      <InputText
        v-model="searchInput"
        :placeholder="$t('views.exploreTagsView.searchPlaceholder')"
        fluid
        @update:model-value="commitSearch(String($event || ''))"
      />
      <Select
        v-model="sort"
        :options="sortOptions"
        option-label="label"
        option-value="value"
        fluid
        :aria-label="$t('common.sort')"
      />
      <Button
        :label="$t('search.clearFilters')"
        icon="ti ti-filter-x"
        severity="contrast"
        outlined
        :disabled="!search && sort === 'popular'"
        @click="
          () => {
            search = ''
            sort = 'popular'
          }
        "
      />
    </section>

    <ErrorContainer v-if="error" :error="error as any" />
    <LoadingContainer v-else-if="isLoading && tags.length === 0" />

    <section v-else-if="sortedTags.length" class="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
      <router-link
        v-for="tag of sortedTags"
        :key="tag.tag"
        :to="{ name: 'tag', params: { tag: tag.tag } }"
        class="group flex min-h-[12rem] flex-col overflow-hidden rounded-xl border border-neutral-200 bg-neutral-100 transition hover:-translate-y-0.5 hover:bg-neutral-200 dark:border-neutral-700 dark:bg-neutral-800 dark:hover:bg-neutral-700"
      >
        <div
          class="relative flex h-28 items-start justify-between gap-3 overflow-hidden bg-neutral-200 p-4 dark:bg-neutral-900"
          :class="tag.image_url ? 'text-white' : ''"
        >
          <img
            v-if="tag.image_url"
            :src="tag.image_url"
            alt=""
            class="absolute inset-0 h-full w-full object-cover"
          />
          <div
            v-if="tag.image_url"
            class="absolute inset-0 bg-gradient-to-b from-black/20 to-black/60"
          />

          <div class="relative flex min-w-0 items-center gap-2">
            <i v-if="tag.icon" :class="`ti ti-${tag.icon} shrink-0 text-xl`" />
            <h2 class="truncate text-xl font-bold">{{ tag.display_name || tag.tag }}</h2>
          </div>
          <span
            class="relative shrink-0 rounded-full bg-black/10 px-2 py-1 text-xs font-semibold backdrop-blur dark:bg-white/10"
          >
            {{ formatCompactNumber(tag.paste_count) }}
          </span>
        </div>

        <div class="flex grow flex-col justify-between gap-4 p-4">
          <p class="line-clamp-3 text-sm text-neutral-600 dark:text-neutral-300">
            {{
              tag.description || $t('views.exploreTagsView.fallbackDescription', { tag: tag.tag })
            }}
          </p>

          <div class="flex items-center justify-end gap-3 text-sm">
            <span
              class="text-primary-500 flex items-center gap-1 transition group-hover:translate-x-1"
            >
              {{ $t('common.open') }}
              <i class="ti ti-arrow-right" />
            </span>
          </div>
        </div>
      </router-link>
    </section>

    <section
      v-else
      class="rounded-xl border border-dashed border-neutral-300 p-8 text-center text-neutral-500 dark:border-neutral-700 dark:text-neutral-400"
    >
      {{ $t('views.exploreTagsView.noTagsFound') }}
    </section>

    <div v-if="canLoadMore" class="flex justify-center">
      <Button
        :label="$t('views.exploreTagsView.loadMoreTags')"
        icon="ti ti-arrow-down"
        severity="contrast"
        outlined
        :loading="isLoading"
        @click="loadMore"
      />
    </div>
  </main>
</template>
