<script lang="ts" setup>
import Dialog from 'primevue/dialog'
import InputText from 'primevue/inputtext'
import Button from 'primevue/button'
import { ref, watch } from 'vue'
import { useAsyncState } from '@vueuse/core'
import { client } from '@/main.ts'
import router from '@/router'
import type { Folder } from '@/types/folder.ts'
import { useUserFoldersStore } from '@/stores/user-folders.ts'
import ErrorContainer from '@/components/ErrorContainer.vue'
const visible = defineModel<boolean>('visible')

const props = defineProps<{
  parent?: string
}>()

const name = ref('')

watch(visible, () => {
  name.value = ''
})

const userFolderStore = useUserFoldersStore()

const {
  execute: createFolder,
  isLoading,
  error,
} = useAsyncState(
  async () => {
    const folder = (
      await client.post('/api/v2/folder', {
        name: name.value,
        parent: props.parent,
      })
    ).data.folder as Folder

    visible.value = false
    userFolderStore.fetchFolders()
    router.push({
      name: 'folder',
      params: {
        folder: folder.id,
      },
    })
  },
  undefined,
  {
    immediate: false,
  },
)
</script>
<template>
  <Dialog v-model:visible="visible" modal header="Login" class="w-[30rem] max-w-full">
    <form class="flex flex-col justify-center gap-3" @submit.prevent="() => createFolder()">
      <InputText autofocus v-model="name" placeholder="name" fluid />

      <Button type="submit" label="create" :loading="isLoading" />

      <ErrorContainer v-if="error" :error="error as any" />
    </form>
  </Dialog>
</template>
