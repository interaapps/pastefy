<script setup lang="ts">
import { useAsyncState } from '@vueuse/core'
import { client } from '@/main.ts'
import type { Paste } from '@/types/paste.ts'
import Button from 'primevue/button'
import PasteCard from '@/components/lists/PasteCard.vue'
import ErrorContainer from '@/components/ErrorContainer.vue'
import LoadingContainer from '@/components/LoadingContainer.vue'
import { ref } from 'vue'
import Pagination from '@/components/Pagination.vue'

const props = defineProps<
  | {
      route: string
      params?: Record<string, string | number>
    }
  | { pastes: Paste[] }
>()

const page = ref(1)

const {
  isLoading,
  state: pastes,
  error,
  execute: load,
} = useAsyncState(async () => {
  if ('pastes' in props && props.pastes) return props.pastes

  if (!('route' in props) || !props.route) return []

  return (
    await client.get(props.route, {
      params: {
        page_limit: 5,
        page: page.value,
        shorten_content: 'true',
        ...(props.params || {}),
      },
    })
  ).data as Paste[]
}, undefined)
</script>

<template>
  <ErrorContainer v-if="error" :error="error as any" />
  <LoadingContainer v-else-if="isLoading" />
  <section v-else-if="pastes" class="mx-auto w-full max-w-[1200px]">
    <div class="mb-3 flex flex-col gap-4">
      <PasteCard v-for="paste of pastes" :paste :key="paste.id" />
    </div>

    <Pagination v-model:page="page" @update:page="load" v-if="'route' in props && props.route" />
  </section>
</template>
