<script setup lang="ts">
import { useAsyncState, useTitle } from '@vueuse/core'
import { client } from '@/main.ts'
import LoadingContainer from '@/components/LoadingContainer.vue'
import ErrorContainer from '@/components/ErrorContainer.vue'
import StatsCard from '@/components/admin/StatsCard.vue'
import Button from 'primevue/button'

useTitle('Home - Admin | pastefy')

const {
  isLoading,
  state: stats,
  error,
} = useAsyncState(async () => {
  return (await client.get('/api/v2/app/stats')).data as {
    created_pastes: number
    logged_in_pastes: number
    user_count: number
    tag_count: number
    folder_count: number
    indexed_pastes?: number
    s3pastes?: number
  }
}, undefined)
</script>
<template>
  <ErrorContainer v-if="error" :error="error as any" />
  <LoadingContainer v-else-if="isLoading" class="flex items-center justify-center p-3" />
  <div v-else-if="stats">
    <div
      class="mb-9 flex flex-col gap-4 rounded-2xl border border-neutral-200 bg-white/80 p-5 dark:border-neutral-800 dark:bg-neutral-900/70 lg:flex-row lg:items-center lg:justify-between"
    >
      <div>
        <h2 class="text-xl font-bold">Quick Actions</h2>
        <p class="text-sm text-neutral-600 dark:text-neutral-300">
          Jump straight into the two moderation queues you are most likely to use.
        </p>
      </div>
      <div class="flex flex-wrap gap-2">
        <Button
          as="router-link"
          :to="{ name: 'admin-users' }"
          icon="ti ti-users"
          label="Manage Users"
          size="small"
          outlined
          severity="contrast"
        />
        <Button
          as="router-link"
          :to="{ name: 'admin-pastes', query: { sort: 'createdAt', page: '1' } }"
          icon="ti ti-script"
          label="Latest Pastes"
          size="small"
        />
      </div>
    </div>

    <div class="mb-9">
      <h2 class="mb-3 text-2xl font-bold">Pastes</h2>
      <div class="grid w-full grid-cols-1 gap-3 md:grid-cols-2 lg:grid-cols-4">
        <StatsCard :top="stats.created_pastes" description="Created Pastes" />
        <StatsCard :top="stats.logged_in_pastes" description="Logged in Pastes" />
        <StatsCard
          v-if="'indexed_pastes' in stats"
          :top="stats.indexed_pastes"
          description="Indexed Pastes"
        />
        <StatsCard v-if="'s3pastes' in stats" :top="stats.s3pastes" description="S3 Pastes" />
      </div>
    </div>

    <div class="mb-9">
      <h2 class="mb-3 text-2xl font-bold">Folders</h2>
      <div class="grid w-full grid-cols-1 gap-3 md:grid-cols-2 lg:grid-cols-4">
        <StatsCard :top="stats.folder_count" description="folders" />
      </div>
    </div>
    <div class="mb-9">
      <h2 class="mb-3 text-2xl font-bold">Users</h2>
      <div class="grid w-full grid-cols-1 gap-3 md:grid-cols-2 lg:grid-cols-4">
        <StatsCard :top="stats.user_count" description="Users" />
      </div>
    </div>
    <div class="mb-9">
      <h2 class="mb-3 text-2xl font-bold">Tags</h2>
      <div class="grid w-full grid-cols-1 gap-3 md:grid-cols-2 lg:grid-cols-4">
        <StatsCard :top="stats.tag_count" description="Tags" />
      </div>
    </div>
  </div>
</template>
