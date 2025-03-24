<script setup lang="ts">
import { useAsyncState, useTitle } from '@vueuse/core'
import { client } from '@/main.ts'
import LoadingContainer from '@/components/LoadingContainer.vue'
import ErrorContainer from '@/components/ErrorContainer.vue'

useTitle('Home - Admin | pastefy')

const {
  isLoading,
  state: stats,
  error,
} = useAsyncState(async () => {
  return (await client.get('/api/v2/app/stats')).data as {
    created_pastes: number
    logged_in_pastes: number
  }
}, undefined)
</script>
<template>
  <ErrorContainer v-if="error" :error="error as any" />
  <LoadingContainer v-else-if="isLoading" class="flex items-center justify-center p-3" />
  <div v-else-if="stats">
    Created pastes: {{ stats.created_pastes }} <br />
    Created pastes (logged in): {{ stats.logged_in_pastes }} <br />
  </div>
</template>
