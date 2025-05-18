<script setup lang="ts">
import { useAsyncState, useTitle } from '@vueuse/core'
import { client } from '@/main.ts'
import LoadingContainer from '@/components/LoadingContainer.vue'
import ErrorContainer from '@/components/ErrorContainer.vue'
import StatsCard from '@/components/admin/StatsCard.vue'

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
