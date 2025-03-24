<script setup lang="ts">
import { useAsyncState } from '@vueuse/core'
import { client } from '@/main.ts'
import ErrorContainer from '@/components/ErrorContainer.vue'
import type { Folder } from '@/types/folder.ts'
import FolderCard from '@/components/lists/FolderCard.vue'
import LoadingContainer from '@/components/LoadingContainer.vue'
import CreateFolderModal from '@/components/modals/CreateFolderModal.vue'
import { ref } from 'vue'

const props = defineProps<
  {
    parent?: string
  } & (
    | {
        route: string
        params?: Record<string, string | number>
      }
    | {
        folders: Folder[]
      }
  )
>()

const {
  isLoading,
  state: folders,
  error,
} = useAsyncState(async () => {
  if ('folders' in props && typeof props.folders !== 'undefined') return props.folders

  if (!('route' in props) || !props.route) return [] as Folder[]

  return (
    await client.get(props.route, {
      params: {
        shorten_content: 'true',
        hide_children: 'true',
        ...(props.params || {}),
      },
    })
  ).data as Folder[]
}, undefined)

const creatFolderModalVisible = ref(false)
</script>

<template>
  <ErrorContainer v-if="error" :error="error as any" />
  <LoadingContainer v-else-if="isLoading" class="flex items-center justify-center p-3" />
  <section v-if="folders" class="mx-auto w-full max-w-[1200px]">
    <div class="flex flex-wrap gap-4">
      <FolderCard v-for="folder of folders" :folder :key="folder.id" />

      <button
        @click="creatFolderModalVisible = true"
        class="group flex cursor-pointer flex-col items-center justify-center gap-2 rounded-xl"
      >
        <div
          class="flex aspect-square w-[5rem] flex-col items-center justify-center gap-2 rounded-xl border border-dashed border-neutral-300 bg-neutral-50 p-3 transition-all group-hover:bg-neutral-100 dark:border-neutral-700 dark:bg-neutral-900 dark:group-hover:bg-neutral-800"
        >
          <i class="ti ti-folder-plus text-4xl" />
        </div>
        <span class="text-sm"> new </span>
      </button>
    </div>
  </section>

  <CreateFolderModal v-model:visible="creatFolderModalVisible" :parent="parent" />
</template>
