<script setup lang="ts">
import PasteList from '@/components/lists/PasteList.vue'
import FolderList from '@/components/lists/FolderList.vue'
import { useTitle } from '@vueuse/core'
import { computed } from 'vue'
import ComponentInjection from '@/components/ComponentInjection.vue'
import { eventBus } from '@/main.ts'
import ShowSearchButton from '@/components/ShowSearchButton.vue'
import { useCurrentUserStore } from '@/stores/current-user.ts'
import { useAppStore } from '@/stores/app.ts'
import { useConfig } from '@/composables/config.ts'
import { useRouter } from 'vue-router'

const currentUserStore = useCurrentUserStore()
const appStore = useAppStore()
const config = useConfig()
const router = useRouter()

const quickActions = computed(() => [
  {
    key: 'create-paste',
    title: 'Create Paste',
    description: 'Open the editor and start a new paste right away.',
    icon: 'edit',
  },
  {
    key: 'search-pastes',
    title: 'Search Pastes',
    description: 'Find snippets, titles, and content across your saved work.',
    icon: 'search',
  },
  {
    key: 'open-tools',
    title: 'Open Tools',
    description: 'Preview, convert, and inspect content without publishing it.',
    icon: 'tool',
    to: { name: 'tool-home' as const },
  },
  currentUserStore.user?.type === 'ADMIN'
    ? {
        key: 'admin-home',
        title: 'Admin Panel',
        description: 'Jump into moderation, users, and recent platform activity.',
        icon: 'shield',
        to: { name: 'admin-home' as const },
      }
    : {
        key: 'explore',
        title: 'Explore Public',
        description: 'Browse public pastes and discover what others are sharing.',
        icon: 'world',
        to: { name: 'explore' as const },
      },
])

const handleQuickAction = async (action: (typeof quickActions.value)[number]) => {
  if (action.key === 'create-paste') {
    if (window.innerWidth >= 1024) {
      config.value.sideBarShown = true
      appStore.createPasteFullscreenRequested = true
    }
    await router.push({ name: 'home-page' })
    return
  }

  if (action.key === 'search-pastes') {
    appStore.searchShownEndpoints.myPastes = true
    appStore.searchShownEndpoints.publicPastes = false
    appStore.searchShown = true
    return
  }

  if (action.to) {
    await router.push(action.to)
  }
}

useTitle(`Home | Pastefy`)
</script>

<template>
  <div class="flex flex-col gap-14">
    <ComponentInjection type="user-home-top" />

    <section class="hidden space-y-4 md:block">
      <div class="flex flex-col gap-1">
        <h2 class="text-2xl font-bold">Quick Actions</h2>
      </div>

      <div class="grid gap-3 md:grid-cols-2 xl:grid-cols-4">
        <button
          v-for="action of quickActions"
          :key="action.key"
          type="button"
          @click="handleQuickAction(action)"
          class="cursor-pointer rounded-xl border border-neutral-200 bg-neutral-100 p-3 text-left transition-all hover:-translate-y-0.5 hover:bg-white dark:border-neutral-700 dark:bg-neutral-800 dark:hover:bg-neutral-900"
        >
          <div class="flex items-start gap-3">
            <div
              class="flex h-10 w-10 shrink-0 items-center justify-center rounded-xl bg-white text-neutral-700 dark:bg-neutral-900 dark:text-neutral-200"
            >
              <i :class="`ti ti-${action.icon} text-lg`" />
            </div>
            <div>
              <h3 class="font-semibold">{{ action.title }}</h3>
              <p class="mt-1 text-sm text-neutral-500 dark:text-neutral-400">
                {{ action.description }}
              </p>
            </div>
          </div>
        </button>
      </div>
    </section>

    <div class="space-y-4">
      <h2 class="text-2xl font-bold">Folders</h2>

      <FolderList route="/api/v2/user/folders" />
    </div>
    <ComponentInjection type="user-home-after-folders" />
    <div>
      <div class="flex justify-between gap-2">
        <h2 class="mb-4 text-2xl font-bold">Pastes</h2>
        <div>
          <ShowSearchButton my-pastes />
        </div>
      </div>

      <PasteList
        @loaded="eventBus.emit('pageLoaded', 'home')"
        route="/api/v2/user/pastes"
        :params="{ page_limit: 8, hide_children: 'true' }"
      />
    </div>
    <ComponentInjection type="user-home-after-pastes" />
  </div>
</template>
