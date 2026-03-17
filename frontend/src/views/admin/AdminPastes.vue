<script setup lang="ts">
import { computed, watch } from 'vue'
import { useAsyncState, useDebounceFn, useTitle } from '@vueuse/core'
import { useRouteQuery } from '@vueuse/router'
import Button from 'primevue/button'
import InputText from 'primevue/inputtext'
import Select from 'primevue/select'
import Tag from 'primevue/tag'
import { useConfirm } from 'primevue/useconfirm'
import { client } from '@/main.ts'
import type { Paste, PasteVisibility } from '@/types/paste.ts'
import LoadingContainer from '@/components/LoadingContainer.vue'
import ErrorContainer from '@/components/ErrorContainer.vue'
import Pagination from '@/components/Pagination.vue'
import PasteCard from '@/components/lists/PasteCard.vue'

useTitle('Pastes - Admin | pastefy')

const page = useRouteQuery('page', 1, { transform: Number })
const pageLimit = useRouteQuery('pageLimit', 10, { transform: Number })
const search = useRouteQuery('search', '')
const visibility = useRouteQuery<PasteVisibility | ''>('visibility', '')
const sort = useRouteQuery('sort', 'createdAt')
const userId = useRouteQuery('userId', '')
const userName = useRouteQuery('userName', '')

const visibilityOptions: Array<{ label: string; value: PasteVisibility | '' }> = [
  { label: 'All visibilities', value: '' },
  { label: 'Public', value: 'PUBLIC' },
  { label: 'Unlisted', value: 'UNLISTED' },
  { label: 'Private', value: 'PRIVATE' },
]

const sortOptions = [
  { label: 'Newest first', value: 'createdAt' },
  { label: 'Oldest first', value: '+createdAt' },
]

const pageSizeOptions = [10, 20, 50].map((value) => ({
  label: `${value} / page`,
  value,
}))

const {
  isLoading,
  state: pastes,
  error,
  execute: load,
} = useAsyncState(async () => {
  return (
    await client.get('/api/v2/paste', {
      params: {
        page: page.value,
        page_limit: pageLimit.value,
        search: search.value || undefined,
        sort: sort.value || undefined,
        shorten_content: 'true',
        ...(visibility.value ? { 'filter[visibility]': visibility.value } : {}),
        ...(userId.value ? { 'filter[userId]': userId.value } : {}),
      },
    })
  ).data as Paste[]
}, undefined)

const hasNext = computed(() => (pastes.value?.length || 0) >= pageLimit.value)
const confirm = useConfirm()

const reloadFirstPage = () => {
  if (page.value !== 1) {
    page.value = 1
    return
  }
  load()
}

const debouncedReloadFirstPage = useDebounceFn(reloadFirstPage, 250)

watch(page, () => load())
watch([pageLimit, visibility, sort, userId], () => reloadFirstPage())
watch(search, () => debouncedReloadFirstPage())

const clearUserFilter = () => {
  userId.value = ''
  userName.value = ''
}

const deletePaste = async (id?: string) => {
  if (!id) return

  confirm.require({
    message: 'Are you sure you want to delete this paste?',
    header: 'Delete paste',
    accept: async () => {
      await client.delete(`/api/v2/paste/${id}`)
      load()
    },
  })
}

const formatDate = (value?: string) => {
  if (!value) return 'Unknown date'
  return new Date(value.replace(' ', 'T')).toLocaleString()
}
</script>
<template>
  <div class="flex flex-col gap-5">
    <section
      class="grid gap-3 rounded-2xl border border-neutral-200 bg-white/80 p-4 xl:grid-cols-[minmax(0,1fr)_220px_220px_180px] dark:border-neutral-800 dark:bg-neutral-900/70"
    >
      <InputText v-model="search" fluid placeholder="Search by title, content, or creator" />
      <Select
        v-model="visibility"
        :options="visibilityOptions"
        option-label="label"
        option-value="value"
        fluid
      />
      <Select
        v-model="sort"
        :options="sortOptions"
        option-label="label"
        option-value="value"
        fluid
      />
      <Select
        v-model="pageLimit"
        :options="pageSizeOptions"
        option-label="label"
        option-value="value"
        fluid
      />
    </section>

    <div class="flex flex-wrap items-center gap-2 text-sm text-neutral-500 dark:text-neutral-400">
      <span>Filters:</span>
      <Tag :value="search ? `Search: ${search}` : 'No search'" severity="contrast" />
      <Tag
        :value="visibility ? `Visibility: ${visibility}` : 'All visibilities'"
        :severity="visibility ? 'info' : 'contrast'"
      />
      <Tag v-if="userId" :value="`User: ${userName || userId}`" severity="warn" />
      <Button
        v-if="userId"
        size="small"
        text
        severity="contrast"
        icon="ti ti-x"
        label="Clear User Filter"
        @click="clearUserFilter"
      />
    </div>

    <ErrorContainer v-if="error" :error="error as any" />
    <LoadingContainer v-else-if="isLoading" class="flex items-center justify-center p-3" />
    <div v-else-if="pastes" class="flex flex-col gap-5">
      <div
        v-if="pastes.length === 0"
        class="rounded-2xl border border-dashed border-neutral-300 bg-white/80 p-8 text-center text-sm text-neutral-500 dark:border-neutral-700 dark:bg-neutral-900/60 dark:text-neutral-400"
      >
        No pastes match the current filters.
      </div>

      <div v-for="paste in pastes" :key="paste.id">
        <PasteCard :paste link-class="rounded-b-none" />
        <div
          class="flex items-center justify-between rounded-xl rounded-t-none border border-t-0 border-neutral-200 bg-neutral-100 p-3 dark:border-neutral-700 dark:bg-neutral-800"
        >
          <div class="min-w-0 space-y-3">
            <div
              class="flex flex-wrap items-center gap-3 text-sm text-neutral-500 dark:text-neutral-400"
            >
              <span>{{ formatDate(paste.created_at) }}</span>
              <span v-if="paste.user?.name">
                by
                <router-link
                  :to="{ name: 'user', params: { user: paste.user.name } }"
                  class="font-medium text-neutral-700 hover:underline dark:text-neutral-200"
                >
                  @{{ paste.user.name }}
                </router-link>
              </span>
              <span v-else-if="paste.user_id">User ID: {{ paste.user_id }}</span>
              <span v-if="paste.tags?.length">#{{ paste.tags.join(' #') }}</span>
            </div>
          </div>

          <div class="flex flex-wrap gap-2 xl:max-w-[28rem] xl:justify-end">
            <Button
              as="router-link"
              :to="{ name: 'paste', params: { paste: paste.id } }"
              size="small"
              outlined
              severity="contrast"
              label="Open"
              icon="ti ti-external-link"
            />
            <Button
              v-if="paste.user_id"
              as="router-link"
              :to="{
                name: 'admin-pastes',
                query: {
                  userId: paste.user_id,
                  userName: paste.user?.display_name || paste.user?.name || paste.user_id,
                  page: '1',
                },
              }"
              size="small"
              text
              severity="contrast"
              label="More From User"
              icon="ti ti-filter"
            />
            <Button
              v-if="paste.user?.name"
              as="router-link"
              :to="{ name: 'user', params: { user: paste.user.name } }"
              size="small"
              text
              severity="contrast"
              label="Profile"
              icon="ti ti-user"
            />
            <Button
              severity="danger"
              size="small"
              text
              label="Delete"
              icon="ti ti-trash"
              @click="deletePaste(paste.id)"
            />
          </div>
        </div>
      </div>

      <Pagination
        v-model:page="page"
        :has-next="hasNext"
        :loading="isLoading"
        @update:page="load"
      />
    </div>
  </div>
</template>
