<script setup lang="ts">
import Button from 'primevue/button'
import InputText from 'primevue/inputtext'
import { useDebounceFn, useIntersectionObserver, useTitle } from '@vueuse/core'
import { useRouteQuery } from '@vueuse/router'
import { computed, ref, watch } from 'vue'

import ErrorContainer from '@/components/ErrorContainer.vue'
import LoadingContainer from '@/components/LoadingContainer.vue'
import PasteExploreCard from '@/components/lists/PasteExploreCard.vue'
import { client } from '@/main.ts'
import type { Paste } from '@/types/paste.ts'
import type { Tag } from '@/types/tags.ts'
import type { PublicUser } from '@/types/user.ts'
import { formatCompactNumber } from '@/utils/format.ts'

type PasteFeedSource = 'latest' | 'trending'
type FeedItem =
  | { key: string; type: 'paste'; source: PasteFeedSource; paste: Paste }
  | { key: string; type: 'tag'; tag: Tag }
  | { key: string; type: 'user'; user: PublicUser; pasteCount: number }
  | {
      key: string
      type: 'collection'
      title: string
      description: string
      params: Record<string, string>
    }

useTitle('Infinite Explore | Pastefy')

const search = useRouteQuery<string>('search', '')
const searchInput = ref(search.value)
const feed = ref<FeedItem[]>([])
const page = ref(1)
const isLoading = ref(false)
const isExhausted = ref(false)
const error = ref<unknown>(null)
const sentinel = ref<HTMLElement | null>(null)
const requestVersion = ref(0)

const seenPastes = new Set<string>()
const seenTags = new Set<string>()
const seenUsers = new Set<string>()
const collectionTags = new Set<string>()
const userPasteCounts = new Map<string, number>()

const normalizedSearch = computed(() => search.value.trim())

const commitSearch = useDebounceFn((value: string) => {
  search.value = value.trim()
}, 250)

const resetSeen = () => {
  seenPastes.clear()
  seenTags.clear()
  seenUsers.clear()
  collectionTags.clear()
  userPasteCounts.clear()
}

const fetchPastes = async (
  route: string,
  currentPage: number,
  pageLimit: number,
  extraParams: Record<string, string> = {},
) => {
  const response = await client.get(route, {
    params: {
      page: currentPage,
      page_limit: pageLimit,
      shorten_content: 'true',
      search: normalizedSearch.value || undefined,
      ...extraParams,
    },
  })
  return response.data as Paste[]
}

const fetchTags = async (currentPage: number) => {
  const response = await client.get('/api/v2/public/tags', {
    params: {
      page: currentPage,
      page_limit: 8,
      search: normalizedSearch.value || undefined,
    },
  })
  return response.data as Tag[]
}

const addPaste = (items: FeedItem[], paste: Paste | undefined, source: PasteFeedSource) => {
  if (!paste?.id || seenPastes.has(paste.id)) return
  seenPastes.add(paste.id)
  items.push({ key: `paste:${paste.id}`, type: 'paste', source, paste })

  if (paste.user?.id) {
    userPasteCounts.set(paste.user.id, (userPasteCounts.get(paste.user.id) || 0) + 1)
  }
}

const addTag = (items: FeedItem[], tag: Tag | undefined) => {
  if (!tag?.tag || seenTags.has(tag.tag)) return
  seenTags.add(tag.tag)
  items.push({ key: `tag:${tag.tag}`, type: 'tag', tag })
}

const addUser = (items: FeedItem[], user: PublicUser | undefined) => {
  if (!user?.id || seenUsers.has(user.id)) return
  seenUsers.add(user.id)
  items.push({
    key: `user:${user.id}`,
    type: 'user',
    user,
    pasteCount: userPasteCounts.get(user.id) || 1,
  })
}

const addCollection = (items: FeedItem[], tag: Tag | undefined) => {
  if (!tag?.tag || collectionTags.has(tag.tag)) return
  collectionTags.add(tag.tag)
  items.push({
    key: `collection:${tag.tag}`,
    type: 'collection',
    title: `#${tag.display_name || tag.tag}`,
    description: tag.description || `A fast lane into public pastes tagged #${tag.tag}.`,
    params: {
      filter_tags: tag.tag,
      shorten_content: 'true',
    },
  })
}

const mixBatch = (latest: Paste[], trending: Paste[], tags: Tag[], users: PublicUser[]) => {
  const items: FeedItem[] = []
  const maxRows = Math.max(latest.length, trending.length, tags.length, users.length)

  for (let index = 0; index < maxRows; index++) {
    addPaste(items, latest[index], 'latest')
    if (index % 2 === 0) addTag(items, tags[Math.floor(index / 2)])
    addPaste(items, trending[index], 'trending')
    if (index % 3 === 1) addUser(items, users[Math.floor(index / 3)])
    if (index % 5 === 2) addCollection(items, tags[Math.floor(index / 2)])
  }

  return items
}

const collectUsers = (...pasteGroups: Paste[][]) => {
  const users = new Map<string, PublicUser>()

  pasteGroups.flat().forEach((paste) => {
    if (!paste.user?.id) return
    users.set(paste.user.id, paste.user)
  })

  return [...users.values()]
}

const loadNextBatch = async () => {
  if (isLoading.value || isExhausted.value) return

  const version = requestVersion.value
  const currentPage = page.value
  isLoading.value = true
  error.value = null

  try {
    const [latest, trending, tags] = await Promise.all([
      fetchPastes('/api/v2/public-pastes/latest', currentPage, 6),
      fetchPastes(
        '/api/v2/public-pastes/trending',
        currentPage,
        4,
        currentPage === 1 ? { trending: 'true' } : {},
      ),
      fetchTags(currentPage),
    ])

    if (version !== requestVersion.value) return

    const users = collectUsers(latest, trending)
    const items = mixBatch(latest, trending, tags, users)
    feed.value.push(...items)
    page.value = currentPage + 1

    if (latest.length === 0 && trending.length === 0 && tags.length === 0) {
      isExhausted.value = true
    }
  } catch (caught) {
    if (version === requestVersion.value) error.value = caught
  } finally {
    if (version === requestVersion.value) isLoading.value = false
  }
}

const resetFeed = () => {
  requestVersion.value++
  feed.value = []
  page.value = 1
  isExhausted.value = false
  error.value = null
  resetSeen()
  void loadNextBatch()
}

watch(
  () => search.value,
  (value) => {
    if (value !== searchInput.value) searchInput.value = value
    resetFeed()
  },
)

useIntersectionObserver(
  sentinel,
  ([entry]) => {
    if (entry?.isIntersecting) void loadNextBatch()
  },
  { rootMargin: '700px' },
)

void loadNextBatch()
</script>

<template>
  <main class="mx-auto flex w-full max-w-[900px] flex-col gap-5">
    <section
      class="overflow-hidden rounded-2xl border border-neutral-200 bg-neutral-100 p-5 md:p-7 dark:border-neutral-700 dark:bg-neutral-800"
    >
      <div class="flex flex-col gap-5 md:flex-row md:items-end md:justify-between">
        <div class="flex flex-col gap-3">
          <span
            class="text-sm font-semibold tracking-[0.25em] text-neutral-500 uppercase dark:text-neutral-400"
          >
            {{ $t('views.infiniteScrollExploreView.eyebrow') }}
          </span>
          <div>
            <h1 class="text-4xl leading-none font-bold md:text-5xl">
              {{ $t('views.infiniteScrollExploreView.title') }}
            </h1>
            <p class="mt-3 max-w-[58ch] text-neutral-600 dark:text-neutral-300">
              {{ $t('views.infiniteScrollExploreView.description') }}
            </p>
          </div>
        </div>

        <div
          class="rounded-xl border border-neutral-200 bg-white p-4 text-sm dark:border-neutral-700 dark:bg-neutral-900"
        >
          <span class="text-neutral-500 dark:text-neutral-400">{{ $t('common.results') }}</span>
          <strong class="ml-2">{{ formatCompactNumber(feed.length) }}</strong>
        </div>
      </div>
    </section>

    <section
      class="sticky top-18 z-10 rounded-xl border border-neutral-200 bg-white/90 p-3 backdrop-blur dark:border-neutral-700 dark:bg-neutral-900/90"
    >
      <InputText
        v-model="searchInput"
        :placeholder="$t('views.infiniteScrollExploreView.searchPlaceholder')"
        fluid
        @update:model-value="commitSearch(String($event || ''))"
      />
    </section>

    <ErrorContainer v-if="error" :error="error as any" />

    <section class="flex flex-col gap-4">
      <article v-for="item of feed" :key="item.key" class="">
        <div v-if="item.type === 'paste'" class="flex flex-col gap-3">
          <div
            class="flex items-center justify-between gap-3 px-1 text-xs font-semibold tracking-[0.18em] text-neutral-500 uppercase dark:text-neutral-400"
          >
            <span>{{
              item.source === 'trending'
                ? $t('paste.trending')
                : $t('views.infiniteScrollExploreView.freshPaste')
            }}</span>
            <span v-if="item.paste.user">@{{ item.paste.user.name }}</span>
          </div>
          <PasteExploreCard :paste="item.paste" />
        </div>

        <router-link
          v-else-if="item.type === 'tag'"
          :to="{ name: 'tag', params: { tag: item.tag.tag } }"
          class="group grid gap-4 rounded-xl bg-neutral-100 p-4 md:grid-cols-[1fr_12rem] dark:bg-neutral-800"
        >
          <div class="flex flex-col gap-3">
            <span
              class="text-xs font-semibold tracking-[0.18em] text-neutral-500 uppercase dark:text-neutral-400"
            >
              {{ $t('views.infiniteScrollExploreView.tagSpotlight') }}
            </span>
            <div class="flex items-center gap-2">
              <i v-if="item.tag.icon" :class="`ti ti-${item.tag.icon} text-xl`" />
              <h2 class="text-2xl font-bold">{{ item.tag.display_name || item.tag.tag }}</h2>
            </div>
            <p class="line-clamp-3 text-neutral-600 dark:text-neutral-300">
              {{
                item.tag.description ||
                $t('views.infiniteScrollExploreView.tagFallback', { tag: item.tag.tag })
              }}
            </p>
            <div class="flex items-center gap-2 text-sm text-neutral-500 dark:text-neutral-400">
              <i class="ti ti-files" />
              <span
                >{{ formatCompactNumber(item.tag.paste_count) }}
                {{ $t('paste.pastes').toLowerCase() }}</span
              >
            </div>
          </div>
          <img
            v-if="item.tag.image_url"
            :src="item.tag.image_url"
            alt=""
            class="h-36 w-full rounded-lg object-cover md:h-full"
          />
          <div
            v-else
            class="flex h-36 items-center justify-center rounded-lg bg-neutral-200 font-mono text-3xl font-bold text-neutral-400 dark:bg-neutral-900 dark:text-neutral-600"
          >
            #{{ item.tag.tag.slice(0, 2) }}
          </div>
        </router-link>

        <router-link
          v-else-if="item.type === 'user'"
          :to="{ name: 'user', params: { user: item.user.name } }"
          class="group flex items-center gap-4 rounded-xl bg-neutral-100 p-4 dark:bg-neutral-800"
        >
          <img
            :src="item.user.avatar"
            alt=""
            class="h-16 w-16 rounded-full border border-neutral-200 object-cover dark:border-neutral-700"
          />
          <div class="min-w-0 flex-1">
            <span
              class="text-xs font-semibold tracking-[0.18em] text-neutral-500 uppercase dark:text-neutral-400"
            >
              {{ $t('views.infiniteScrollExploreView.creatorSpotlight') }}
            </span>
            <h2 class="truncate text-xl font-bold">{{ item.user.display_name }}</h2>
            <p class="truncate text-sm text-neutral-500 dark:text-neutral-400">
              @{{ item.user.name }}
            </p>
          </div>
          <span
            class="hidden rounded-full bg-neutral-200 px-3 py-1 text-sm sm:block dark:bg-neutral-900"
          >
            {{ $t('views.infiniteScrollExploreView.seenInFeed', { count: item.pasteCount }) }}
          </span>
        </router-link>

        <router-link
          v-else
          :to="{ name: 'search-pastes', query: item.params }"
          class="group from-primary-500/15 flex flex-col gap-3 rounded-xl bg-gradient-to-br to-neutral-100 p-4 dark:to-neutral-800"
        >
          <span
            class="text-xs font-semibold tracking-[0.18em] text-neutral-500 uppercase dark:text-neutral-400"
          >
            {{ $t('views.infiniteScrollExploreView.collection') }}
          </span>
          <h2 class="text-2xl font-bold">{{ item.title }}</h2>
          <p class="text-neutral-600 dark:text-neutral-300">{{ item.description }}</p>
          <span
            class="text-primary-500 flex items-center gap-1 transition group-hover:translate-x-1"
          >
            {{ $t('views.infiniteScrollExploreView.openCollection') }}
            <i class="ti ti-arrow-right" />
          </span>
        </router-link>
      </article>
    </section>

    <div ref="sentinel" class="flex min-h-24 items-center justify-center">
      <LoadingContainer v-if="isLoading" />
      <Button
        v-else-if="!isExhausted"
        :label="$t('views.infiniteScrollExploreView.loadMore')"
        icon="ti ti-arrow-down"
        severity="contrast"
        outlined
        @click="loadNextBatch"
      />
      <span v-else class="text-sm text-neutral-500 dark:text-neutral-400">
        {{ $t('views.infiniteScrollExploreView.endOfFeed') }}
      </span>
    </div>
  </main>
</template>
