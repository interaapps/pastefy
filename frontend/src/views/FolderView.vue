<script setup lang="ts">
import PasteList from '@/components/lists/PasteList.vue'
import Button from 'primevue/button'
import FolderList from '@/components/lists/FolderList.vue'
import { useAsyncState, useTitle } from '@vueuse/core'
import type { Folder } from '@/types/folder.ts'
import { client } from '@/main.ts'
import { useRoute } from 'vue-router'
import ErrorContainer from '@/components/ErrorContainer.vue'
import LoadingContainer from '@/components/LoadingContainer.vue'
import { useConfirm } from 'primevue/useconfirm'
import router from '@/router'

useTitle(`Folder | Pastefy`)

const route = useRoute()

const {
  isLoading,
  state: folder,
  error,
} = useAsyncState(async () => {
  const folder = (
    await client.get(`/api/v2/folder/${route.params.folder}` as string, {
      params: {
        shorten_content: 'true',
      },
    })
  ).data as Folder

  useTitle(`${folder.name} | Pastefy`)
  return folder
}, undefined)

const confirm = useConfirm()
const deleteFolder = () => {
  confirm.require({
    message: 'Are you sure you want to delete this folder?',
    header: 'Delete folder',
    accept: async () => {
      await client.delete(`/api/v2/folder/${folder.value?.id}`)
      router.push({ name: 'home-page' })
    },
  })
}
</script>

<template>
  <main class="mx-auto w-full max-w-[1200px]">
    <ErrorContainer v-if="error" :error="error as any" />
    <LoadingContainer v-else-if="isLoading" class="flex items-center justify-center p-3" />
    <div v-else-if="folder">
      <div class="mb-6 flex items-center justify-between">
        <h1 class="text-2xl font-bold">{{ folder.name }}</h1>

        <Button icon="ti ti-trash text-lg" text severity="contrast" @click="deleteFolder" />
      </div>
      <div class="mb-14">
        <h2 class="mb-4 text-xl font-bold">Folders</h2>

        <FolderList :folders="folder.children" :parent="folder.id" />
      </div>

      <div>
        <h2 class="mb-4 text-xl font-bold">Pastes</h2>

        <PasteList :pastes="folder.pastes" />
      </div>
    </div>
  </main>
</template>
