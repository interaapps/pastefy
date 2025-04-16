<script lang="ts" setup>
import { ref, useTemplateRef, watch } from 'vue'
import Button from 'primevue/button'
import PasteCard from '@/components/lists/PasteCard.vue'
import { useAsyncState, useDebounceFn } from '@vueuse/core'
import { client } from '@/main.ts'
import type { Paste } from '@/types/paste.ts'
import ErrorContainer from '@/components/ErrorContainer.vue'
import LoadingContainer from '@/components/LoadingContainer.vue'
import { useAppStore } from '@/stores/app.ts'
import router from '@/router'
import { useCurrentUserStore } from '@/stores/current-user.ts'

const currentUserStore = useCurrentUserStore()

const search = ref('')

const currentIndex = ref(1)

const {
  isLoading,
  state: pastes,
  error,
  execute: loadMyPastes,
} = useAsyncState(
  async () => {
    const pastes: (Paste | string)[] = []

    if (appStore.searchShownEndpoints.myPastes && currentUserStore.user) {
      const myPastes = (
        await client.get('/api/v2/user/pastes', {
          params: {
            page_limit: 5,
            shorten_content: 'true',
            search: search.value,
          },
        })
      ).data

      if (myPastes.length > 0) {
        pastes.push('My Pastes', ...myPastes)
      }
    }
    if (appStore.searchShownEndpoints.publicPastes) {
      const publicPastes = (
        await client.get('/api/v2/public-pastes/latest', {
          params: {
            page_limit: 5,
            shorten_content: 'true',
            search: search.value,
          },
        })
      ).data

      if (publicPastes.length > 0) {
        pastes.push('Public Pastes', ...publicPastes)
      }
    }

    currentIndex.value = 1

    return pastes
  },
  undefined,
  { immediate: false },
)

const debounceSearch = useDebounceFn(() => {
  loadMyPastes()
}, 500)

const appStore = useAppStore()

const focusInput = (input: HTMLInputElement) => {
  input?.focus()
  setTimeout(() => {
    input?.focus()
  }, 100)
}

const entriesRef = useTemplateRef<typeof PasteCard>('entriesRef')

watch(currentIndex, () => {
  entriesRef.value?.[currentIndex.value!]?.scrollIntoView({
    block: 'end',
    inline: 'nearest',
  })
})

const down = () => {
  currentIndex.value++

  while (pastes.value && typeof pastes.value[currentIndex.value] === 'string') {
    currentIndex.value++
  }

  if (pastes.value && currentIndex.value >= pastes.value.length) {
    currentIndex.value = 0
  }
}
const up = () => {
  currentIndex.value--
  while (pastes.value && typeof pastes.value[currentIndex.value] === 'string') {
    currentIndex.value--
  }
  if (pastes.value && currentIndex.value < 0) {
    currentIndex.value = pastes.value.length - 1
  }
}

let initialLoad = false
watch(
  () => appStore.searchShown,
  () => {
    if (!initialLoad) {
      loadMyPastes()
      initialLoad = true
    }
  },
)
</script>
<template>
  <div
    class="fixed top-0 left-0 flex h-full w-full items-center justify-center gap-2 bg-black/10 p-3 dark:bg-black/40"
    v-if="appStore.searchShown"
    @click="appStore.searchShown = false"
  >
    <div
      class="flex h-[30rem] max-h-[100%] w-[44rem] max-w-[100%] flex-col rounded-xl border border-neutral-200 bg-white shadow-md dark:border-neutral-700 dark:bg-neutral-900"
      @click.stop
    >
      <div class="flex items-center border-b border-neutral-200 dark:border-neutral-700">
        <input
          autofocus
          v-model="search"
          class="block w-full p-3 focus:outline-none"
          placeholder="search"
          :ref="(e) => focusInput(e as HTMLInputElement)"
          @input="debounceSearch"
          @keydown.esc="appStore.searchShown = false"
          @keydown.up="up"
          @keydown.down="down"
          @keydown.enter="
            () => {
              entriesRef?.[currentIndex]?.click()
              console.log('CLCIK', entriesRef?.[currentIndex])
            }
          "
        />
        <div class="flex gap-1 px-2">
          <Button
            v-if="currentUserStore.user"
            icon="ti ti-user"
            class="gap-0.5 rounded-md border px-2 py-0.5"
            @click="
              () => {
                appStore.searchShownEndpoints.myPastes = !appStore.searchShownEndpoints.myPastes
                loadMyPastes()
              }
            "
            :severity="appStore.searchShownEndpoints.myPastes ? 'primary' : 'secondary'"
            outlined
            label="mine"
          />
          <Button
            icon="ti ti-world"
            class="gap-0.5 rounded-md border px-2 py-0.5"
            @click="
              () => {
                appStore.searchShownEndpoints.publicPastes =
                  !appStore.searchShownEndpoints.publicPastes
                loadMyPastes()
              }
            "
            :severity="appStore.searchShownEndpoints.publicPastes ? 'primary' : 'secondary'"
            outlined
            label="public"
          />
        </div>
      </div>
      <div class="flex flex-col gap-1 overflow-auto p-3 pt-0">
        <ErrorContainer v-if="error" :error="error as any" />
        <LoadingContainer v-else-if="isLoading" class="h-full" />

        <template v-for="(paste, index) of pastes" :key="index">
          <label class="mt-3 text-sm opacity-60" v-if="typeof paste === 'string'">
            {{ paste }}
          </label>

          <PasteCard
            v-else
            :paste="paste"
            :selected="currentIndex === index"
            @click="appStore.searchShown = false"
            preview-max-height="max-h-[3rem]"
          />
          <div
            ref="entriesRef"
            @click="
              () => {
                if (typeof paste !== 'string' && 'id' in paste && paste.id) {
                  router.push({ name: 'paste', params: { paste: paste.id } })
                  appStore.searchShown = false
                }
              }
            "
          />
        </template>
      </div>
    </div>
  </div>
</template>
