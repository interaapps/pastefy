<script setup lang="ts">
import { useAsyncState, useTitle } from '@vueuse/core'
import { useRoute } from 'vue-router'
import { client } from '@/main.ts'
import type { Folder } from '@/types/folder.ts'
import type { PublicUser } from '@/types/user.ts'
import ErrorContainer from '@/components/ErrorContainer.vue'
import LoadingContainer from '@/components/LoadingContainer.vue'
import PasteList from '@/components/lists/PasteList.vue'
import ComponentInjection from '@/components/ComponentInjection.vue'

useTitle(`User | Pastefy`)

const route = useRoute()

const {
  isLoading,
  state: user,
  error,
} = useAsyncState(async () => {
  const user = (
    await client.get(`/api/v2/public/user/${route.params.user}` as string, {
      params: {
        shorten_content: 'true',
      },
    })
  ).data as PublicUser

  useTitle(`${user.display_name} | Pastefy`)
  return user
}, undefined)
</script>
<template>
  <main class="mx-auto w-full max-w-[1200px]">
    <ErrorContainer v-if="error" :error="error as any" />
    <LoadingContainer v-else-if="isLoading" class="flex items-center justify-center p-3" />
    <div v-else-if="user">
      <ComponentInjection type="public-user-top" />
      <div
        class="mb-9 flex flex-col items-center gap-3 rounded-md border border-neutral-200 object-cover p-3 md:flex-row dark:border-neutral-700"
      >
        <img
          alt="User Avatar"
          :src="user.avatar"
          class="h-[2.5rem] w-[2.5rem] rounded-full border border-neutral-200 object-cover dark:border-neutral-700"
        />
        <div class="flex flex-col text-center md:text-left">
          <h1 class="text-xl font-bold">{{ user.display_name }}</h1>
          <span class="mono text-sm font-bold opacity-50">@{{ user.name }}</span>
        </div>
      </div>

      <div>
        <h2 class="mb-3 text-2xl font-bold">Public Pastes</h2>
        <PasteList route="/api/v2/public-pastes/latest" :params="{ 'filter[userId]': user.id }" />
      </div>
    </div>
  </main>
</template>
