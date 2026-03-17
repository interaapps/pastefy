<script setup lang="ts">
import Button from 'primevue/button'
import InputText from 'primevue/inputtext'
import Select from 'primevue/select'
import Tag from 'primevue/tag'
import { useDebounceFn, useTitle } from '@vueuse/core'
import { useRouteQuery } from '@vueuse/router'
import { computed, ref, watch } from 'vue'

import PasteList from '@/components/lists/PasteList.vue'
import { client } from '@/main.ts'
import { useAppStore } from '@/stores/app.ts'
import { useCurrentUserStore } from '@/stores/current-user.ts'
import type { PublicUser } from '@/types/user.ts'

type Scope = 'public' | 'mine' | 'starred'
type VisibilityFilter = 'all' | 'PUBLIC' | 'UNLISTED' | 'PRIVATE'
type TypeFilter = 'all' | 'PASTE' | 'MULTI_PASTE'
type EncryptionFilter = 'all' | 'plain' | 'encrypted'
type FolderFilter = 'all' | 'root' | 'foldered'

const appStore = useAppStore()
const currentUserStore = useCurrentUserStore()

useTitle('Search | Pastefy')

const page = useRouteQuery('page', 1, { transform: Number })
const search = useRouteQuery<string>('search', '')
const scope = useRouteQuery<Scope>('scope', currentUserStore.user ? 'mine' : 'public')
const sort = useRouteQuery('sort', 'createdAt')
const pageLimit = useRouteQuery('page_limit', 12, { transform: Number })
const visibility = useRouteQuery<VisibilityFilter>('visibility', 'all')
const pasteType = useRouteQuery<TypeFilter>('type', 'all')
const encryption = useRouteQuery<EncryptionFilter>('encrypted', 'all')
const folder = useRouteQuery<FolderFilter>('folder', 'all')
const tags = useRouteQuery<string>('tags', '')

const searchInput = ref(search.value)
const tagsInput = ref(tags.value)
const matchedUser = ref<PublicUser | null | undefined>(undefined)

const commitSearch = useDebounceFn((value: string) => {
  search.value = value.trim()
}, 250)

const commitTags = useDebounceFn((value: string) => {
  tags.value = value.trim()
}, 250)

watch(
  () => search.value,
  (value) => {
    if (value !== searchInput.value) searchInput.value = value
  },
)

watch(
  () => tags.value,
  (value) => {
    if (value !== tagsInput.value) tagsInput.value = value
  },
)

watch(
  () => currentUserStore.user,
  (user) => {
    if (user) return
    if (scope.value !== 'public') scope.value = 'public'
  },
)

const scopeOptions = computed(() =>
  [
    {
      label: 'Public',
      value: 'public' as Scope,
      icon: 'world',
      description: 'Search all public pastes.',
    },
    ...(currentUserStore.user
      ? [
          {
            label: 'Mine',
            value: 'mine' as Scope,
            icon: 'user',
            description: 'Search your own pastes.',
          },
          {
            label: 'Starred',
            value: 'starred' as Scope,
            icon: 'star',
            description: 'Search your starred collection.',
          },
        ]
      : []),
  ] as const,
)

const sortOptions = [
  { label: 'Newest first', value: 'createdAt' },
  { label: 'Oldest first', value: '+createdAt' },
  { label: 'Title A-Z', value: '+title' },
  { label: 'Title Z-A', value: 'title' },
]

const pageLimitOptions = [
  { label: '12 per page', value: 12 },
  { label: '24 per page', value: 24 },
  { label: '48 per page', value: 48 },
]

const visibilityOptions = [
  { label: 'All visibilities', value: 'all' as VisibilityFilter },
  { label: 'Public', value: 'PUBLIC' as VisibilityFilter },
  { label: 'Unlisted', value: 'UNLISTED' as VisibilityFilter },
  { label: 'Private', value: 'PRIVATE' as VisibilityFilter },
]

const typeOptions = [
  { label: 'All paste types', value: 'all' as TypeFilter },
  { label: 'Single pastes', value: 'PASTE' as TypeFilter },
  { label: 'Multi pastes', value: 'MULTI_PASTE' as TypeFilter },
]

const encryptionOptions = [
  { label: 'All encryption states', value: 'all' as EncryptionFilter },
  { label: 'Plain only', value: 'plain' as EncryptionFilter },
  { label: 'Encrypted only', value: 'encrypted' as EncryptionFilter },
]

const folderOptions = [
  { label: 'All folder states', value: 'all' as FolderFilter },
  { label: 'Root level only', value: 'root' as FolderFilter },
  { label: 'Only inside folders', value: 'foldered' as FolderFilter },
]

const endpoint = computed(() => {
  if (scope.value === 'mine') return '/api/v2/user/pastes'
  if (scope.value === 'starred') return '/api/v2/user/starred-pastes'
  return '/api/v2/public-pastes/latest'
})

const normalizedTags = computed(() =>
  tags.value
    .split(',')
    .map((entry) => entry.trim())
    .filter(Boolean)
    .join(','),
)

const userLookupName = computed(() => {
  const match = search.value.trim().match(/^@([a-zA-Z0-9-]+)$/)
  return match?.[1]
})

const effectiveSearchQuery = computed(() => {
  const value = search.value.trim()
  return userLookupName.value || value
})

const loadMatchedUser = useDebounceFn(async (name?: string) => {
  if (!name) {
    matchedUser.value = undefined
    return
  }

  try {
    matchedUser.value = (await client.get(`/api/v2/public/user/${name}`)).data as PublicUser
  } catch {
    matchedUser.value = null
  }
}, 250)

watch(
  userLookupName,
  (value) => {
    loadMatchedUser(value)
  },
  { immediate: true },
)

const filters = computed(() => {
  const andFilters: Record<string, unknown>[] = []

  if (matchedUser.value?.id) {
    andFilters.push({ userId: matchedUser.value.id })
  }

  if (visibility.value !== 'all' && scope.value !== 'public') {
    andFilters.push({ visibility: visibility.value })
  }

  if (pasteType.value !== 'all') {
    andFilters.push({ type: pasteType.value })
  }

  if (encryption.value !== 'all') {
    andFilters.push({ encrypted: encryption.value === 'encrypted' ? 'true' : 'false' })
  }

  if (folder.value === 'root') {
    andFilters.push({ folder: { $null: true } })
  } else if (folder.value === 'foldered') {
    andFilters.push({ folder: { $notNull: true } })
  }

  if (!andFilters.length) return undefined
  if (andFilters.length === 1) return JSON.stringify(andFilters[0])
  return JSON.stringify({ $and: andFilters })
})

const requestParams = computed(() => ({
  ...(effectiveSearchQuery.value && !matchedUser.value?.id
    ? { search: effectiveSearchQuery.value }
    : {}),
  ...(sort.value ? { sort: sort.value } : {}),
  ...(normalizedTags.value ? { filter_tags: normalizedTags.value } : {}),
  ...(filters.value ? { filters: filters.value } : {}),
}))

const requestSignature = computed(() =>
  JSON.stringify({
    endpoint: endpoint.value,
    params: requestParams.value,
    pageLimit: pageLimit.value,
  }),
)

watch(requestSignature, () => {
  page.value = 1
})

const activeFilters = computed(() => {
  const entries: string[] = []
  if (scope.value !== 'public') {
    const selectedScope = scopeOptions.value.find((entry) => entry.value === scope.value)
    if (selectedScope) entries.push(`Scope: ${selectedScope.label}`)
  }
  if (visibility.value !== 'all' && scope.value !== 'public') {
    entries.push(`Visibility: ${visibility.value.toLowerCase()}`)
  }
  if (pasteType.value !== 'all') {
    entries.push(`Type: ${pasteType.value === 'MULTI_PASTE' ? 'multi' : 'single'}`)
  }
  if (encryption.value !== 'all') {
    entries.push(`Encryption: ${encryption.value}`)
  }
  if (folder.value !== 'all') {
    entries.push(`Folder: ${folder.value}`)
  }
  if (normalizedTags.value) {
    entries.push(
      ...normalizedTags.value
        .split(',')
        .filter(Boolean)
        .map((tag) => `Tag: ${tag}`),
    )
  }
  if (userLookupName.value) {
    entries.push(
      matchedUser.value?.id
        ? `User: @${userLookupName.value}`
        : `User lookup: @${userLookupName.value}`,
    )
  }
  return entries
})

const clearFilters = () => {
  visibility.value = 'all'
  pasteType.value = 'all'
  encryption.value = 'all'
  folder.value = 'all'
  tags.value = ''
  sort.value = 'createdAt'
  pageLimit.value = 12
}

const openModalSearch = () => {
  appStore.searchShownEndpoints.myPastes = scope.value !== 'public'
  appStore.searchShownEndpoints.publicPastes = scope.value === 'public'
  appStore.searchShown = true
}

const resultDescription = computed(() => {
  if (scope.value === 'mine') return 'Search across your own pastes with deeper filters.'
  if (scope.value === 'starred') return 'Search the pastes you have starred and want to revisit.'
  return 'Search public pastes with richer filters than the quick modal.'
})
</script>

<template>
  <section class="mx-auto flex w-full max-w-[1440px] flex-col gap-6 p-4 md:p-8">
    <div
      class="overflow-hidden rounded-2xl border border-neutral-200 bg-neutral-100 p-5 dark:border-neutral-700 dark:bg-neutral-800 md:p-6"
    >
      <div class="flex flex-col gap-4 lg:flex-row lg:items-end lg:justify-between">
        <div class="max-w-[52rem]">
          <div class="text-sm font-medium text-neutral-500 dark:text-neutral-400">Search</div>
          <h1 class="mt-1 text-3xl font-bold tracking-tight md:text-4xl">Find the right paste faster</h1>
          <p class="mt-2 text-neutral-600 dark:text-neutral-300">
            {{ resultDescription }}
          </p>
        </div>

        <div class="flex flex-wrap gap-2">
          <Button
            @click="openModalSearch"
            icon="ti ti-search"
            label="quick modal"
            severity="contrast"
            outlined
          />
          <Button
            @click="clearFilters"
            icon="ti ti-filter-off"
            label="clear filters"
            severity="contrast"
            outlined
          />
        </div>
      </div>
    </div>

    <div class="grid gap-6 xl:grid-cols-[21rem_minmax(0,1fr)]">
      <aside class="flex flex-col gap-4 xl:sticky xl:top-6 xl:self-start">
        <div
          class="rounded-2xl border border-neutral-200 bg-neutral-100 p-4 dark:border-neutral-700 dark:bg-neutral-800"
        >
          <label class="mb-2 block text-sm font-medium">Search query</label>
          <InputText
            v-model="searchInput"
            fluid
            placeholder="Search titles, content, or enter @username"
            @update:model-value="commitSearch(String($event || ''))"
          />
          <p class="mt-3 text-xs text-neutral-500 dark:text-neutral-400">
            The backend searches title, content, username, and unique name. Enter `@username` for
            an exact public user lookup.
          </p>
        </div>

        <div
          class="rounded-2xl border border-neutral-200 bg-neutral-100 p-4 dark:border-neutral-700 dark:bg-neutral-800"
        >
          <div class="mb-3 text-sm font-medium">Scope</div>
          <div class="grid gap-2">
            <button
              v-for="entry of scopeOptions"
              :key="entry.value"
              type="button"
              @click="scope = entry.value"
              class="cursor-pointer rounded-xl border p-3 text-left transition-colors"
              :class="
                scope === entry.value
                  ? 'border-neutral-900 bg-white dark:border-neutral-200 dark:bg-neutral-900'
                  : 'border-neutral-200 bg-white/70 hover:bg-white dark:border-neutral-700 dark:bg-neutral-900/60 dark:hover:bg-neutral-900'
              "
            >
              <div class="flex items-center gap-2 font-medium">
                <i :class="`ti ti-${entry.icon}`" />
                {{ entry.label }}
              </div>
              <p class="mt-1 text-sm text-neutral-500 dark:text-neutral-400">
                {{ entry.description }}
              </p>
            </button>
          </div>
        </div>

        <div
          class="rounded-2xl border border-neutral-200 bg-neutral-100 p-4 dark:border-neutral-700 dark:bg-neutral-800"
        >
          <div class="mb-3 text-sm font-medium">Filters</div>
          <div class="grid gap-3">
            <div>
              <label class="mb-1 block text-sm text-neutral-500 dark:text-neutral-400">Sort</label>
              <Select v-model="sort" :options="sortOptions" option-label="label" option-value="value" fluid />
            </div>

            <div>
              <label class="mb-1 block text-sm text-neutral-500 dark:text-neutral-400">Results per page</label>
              <Select
                v-model="pageLimit"
                :options="pageLimitOptions"
                option-label="label"
                option-value="value"
                fluid
              />
            </div>

            <div v-if="scope !== 'public'">
              <label class="mb-1 block text-sm text-neutral-500 dark:text-neutral-400">Visibility</label>
              <Select
                v-model="visibility"
                :options="visibilityOptions"
                option-label="label"
                option-value="value"
                fluid
              />
            </div>

            <div>
              <label class="mb-1 block text-sm text-neutral-500 dark:text-neutral-400">Paste type</label>
              <Select
                v-model="pasteType"
                :options="typeOptions"
                option-label="label"
                option-value="value"
                fluid
              />
            </div>

            <div>
              <label class="mb-1 block text-sm text-neutral-500 dark:text-neutral-400">Encryption</label>
              <Select
                v-model="encryption"
                :options="encryptionOptions"
                option-label="label"
                option-value="value"
                fluid
              />
            </div>

            <div>
              <label class="mb-1 block text-sm text-neutral-500 dark:text-neutral-400">Folder state</label>
              <Select
                v-model="folder"
                :options="folderOptions"
                option-label="label"
                option-value="value"
                fluid
              />
            </div>

            <div>
              <label class="mb-1 block text-sm text-neutral-500 dark:text-neutral-400">Tags</label>
              <InputText
                v-model="tagsInput"
                fluid
                placeholder="markdown,json,lang-ts"
                @update:model-value="commitTags(String($event || ''))"
              />
              <p class="mt-1 text-xs text-neutral-500 dark:text-neutral-400">
                Separate multiple tags with commas.
              </p>
            </div>
          </div>
        </div>
      </aside>

      <div class="flex min-w-0 flex-col gap-4">
        <router-link
          v-if="matchedUser"
          :to="{ name: 'user', params: { user: matchedUser.name } }"
          class="rounded-2xl border border-neutral-200 bg-neutral-100 p-4 transition-all hover:-translate-y-0.5 hover:bg-white dark:border-neutral-700 dark:bg-neutral-800 dark:hover:bg-neutral-900"
        >
          <div class="flex items-center gap-3">
            <img
              :src="matchedUser.avatar"
              :alt="matchedUser.display_name"
              class="h-12 w-12 rounded-full border border-neutral-200 object-cover dark:border-neutral-700"
            />
            <div>
              <div class="text-sm text-neutral-500 dark:text-neutral-400">User match</div>
              <h2 class="font-semibold">{{ matchedUser.display_name }}</h2>
              <p class="text-sm text-neutral-500 dark:text-neutral-400">@{{ matchedUser.name }}</p>
            </div>
            <i class="ti ti-arrow-up-right ml-auto text-lg text-neutral-400" />
          </div>
        </router-link>

        <div
          v-else-if="matchedUser === null"
          class="rounded-2xl border border-dashed border-neutral-300 bg-white/70 p-4 text-sm text-neutral-500 dark:border-neutral-700 dark:bg-neutral-900/40 dark:text-neutral-400"
        >
          No public user matched <span class="font-medium">@{{ userLookupName }}</span>.
        </div>

        <div
          class="rounded-2xl border border-neutral-200 bg-neutral-100 p-4 dark:border-neutral-700 dark:bg-neutral-800"
        >
          <div class="flex flex-col gap-3 md:flex-row md:items-center md:justify-between">
            <div>
              <h2 class="text-xl font-bold">Results</h2>
              <p class="text-sm text-neutral-500 dark:text-neutral-400">
                {{ endpoint === '/api/v2/public-pastes/latest' ? 'Public endpoint' : endpoint }}
              </p>
            </div>

            <div v-if="activeFilters.length" class="flex flex-wrap gap-2">
              <Tag
                v-for="entry of activeFilters"
                :key="entry"
                :value="entry"
                severity="contrast"
              />
            </div>
          </div>
        </div>

        <PasteList
          :route="endpoint"
          :params="requestParams"
          :page-limit="pageLimit"
          empty-message="No pastes matched this search. Try broadening the query or clearing a few filters."
        />
      </div>
    </div>
  </section>
</template>
