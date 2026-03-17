<script setup lang="ts">
import { useAsyncState } from '@vueuse/core'
import { client } from '@/main.ts'
import type { Paste } from '@/types/paste.ts'
import PasteCard from '@/components/lists/PasteCard.vue'
import ErrorContainer from '@/components/ErrorContainer.vue'
import LoadingContainer from '@/components/LoadingContainer.vue'
import Pagination from '@/components/Pagination.vue'
import { useRouteQuery } from '@vueuse/router'
import { computed, watch } from 'vue'

const props = defineProps<
  (
    | {
        route: string
        params?: Record<string, string | number>
        pageLimit?: number
      }
    | { pastes: Paste[] }
  ) & { queryPrefix?: string; emptyMessage?: string }
>()

const page = useRouteQuery(`${props.queryPrefix || ''}page`, 1, { transform: Number })
const resolvedPageLimit = computed(() => ('pageLimit' in props ? props.pageLimit || 5 : 5))

const emit = defineEmits(['loaded'])

const {
  isLoading,
  state: pastes,
  error,
  execute: load,
} = useAsyncState(async () => {
  if ('pastes' in props && props.pastes) return props.pastes

  if (!('route' in props) || !props.route) return []

  const res = (
    await client.get(props.route, {
      params: {
        page_limit: resolvedPageLimit.value,
        page: page.value,
        shorten_content: 'true',
        ...(props.params || {}),
      },
    })
  ).data as Paste[]
  emit('loaded')
  return res
}, undefined)

watch(
  () =>
    JSON.stringify({
      route: 'route' in props ? props.route : undefined,
      params: 'route' in props ? props.params || {} : {},
      pageLimit: resolvedPageLimit.value,
    }),
  () => {
    page.value = 1
    load()
  },
)
</script>

<template>
  <ErrorContainer v-if="error" :error="error as any" />
  <LoadingContainer v-else-if="isLoading" />
  <section v-else-if="pastes" class="mx-auto w-full max-w-[1200px] space-y-5">
    <div v-if="pastes.length" class="flex flex-col gap-4">
      <PasteCard v-for="paste of pastes" :paste :key="paste.id" />
    </div>
    <div
      v-else
      class="rounded-2xl border border-dashed border-neutral-300 bg-white/70 p-8 text-center text-sm text-neutral-500 dark:border-neutral-700 dark:bg-neutral-900/40 dark:text-neutral-400"
    >
      {{ props.emptyMessage || 'No pastes found for this selection.' }}
    </div>

    <Pagination
      v-if="'route' in props && props.route"
      v-model:page="page"
      :loading="isLoading"
      @update:page="load"
    />
  </section>
</template>
