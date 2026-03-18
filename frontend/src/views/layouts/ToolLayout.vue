<script setup lang="ts">
import Button from 'primevue/button'
import InputText from 'primevue/inputtext'
import Popover from 'primevue/popover'
import LoginModal from '@/components/modals/LoginModal.vue'
import UserMenu from '@/components/popovers/UserMenu.vue'
import { computed, defineAsyncComponent, ref, useTemplateRef } from 'vue'
import { useCurrentUserStore } from '@/stores/current-user.ts'
import { useAppInfoStore } from '@/stores/app-info.ts'
import type { PopoverMethods } from 'primevue'
import { previewTools } from '@/utils/preview-tools.ts'
import { conversionTools } from '@/utils/conversion-tools.ts'
import { utilityTools } from '@/utils/utility-tools.ts'
import {
  allToolCatalogEntries,
  getCategoryCount,
  toolCategoryDefinitions,
} from '@/utils/tool-categories.ts'

const Logo = defineAsyncComponent(() => import('@/components/Logo.vue'))

const loginModalVisible = ref(false)
const toolSearch = ref('')
const currentUserStore = useCurrentUserStore()
const appInfo = useAppInfoStore()
const userMenu = useTemplateRef<PopoverMethods>('userMenu')

const toolSearchResults = computed(() => {
  const query = toolSearch.value.trim().toLowerCase()
  if (!query) return []

  return allToolCatalogEntries
    .filter((entry) =>
      [entry.title, entry.shortTitle, entry.description, entry.category, ...entry.keywords]
        .join(' ')
        .toLowerCase()
        .includes(query),
    )
    .slice(0, 8)
})

const topLinks = computed(() => [
  {
    label: 'categories',
    icon: 'ti ti-category',
    to: { name: 'tool-home' },
    panelClass: 'w-[56rem]',
    cards: toolCategoryDefinitions
      .filter((category) => getCategoryCount(category.slug) > 0)
      .map((category) => ({
        title: category.title,
        description: `${category.description} ${getCategoryCount(category.slug)} tools.`,
        icon: category.icon,
        to: { name: 'tool-category', params: { category: category.slug } },
      })),
  },
  {
    label: 'preview & inspect',
    icon: 'ti ti-sparkles',
    to: { name: 'tool-home', hash: '#preview-tools' },
    cards: previewTools.slice(0, 6).map((tool) => ({
      title: tool.shortTitle,
      description: tool.description,
      icon: tool.icon,
      to: { name: 'tool-preview', params: { tool: tool.slug } },
    })),
  },
  {
    label: 'convert',
    icon: 'ti ti-arrows-exchange',
    to: { name: 'tool-home', hash: '#conversions' },
    cards: conversionTools.slice(0, 6).map((tool) => ({
      title: tool.shortTitle,
      description: tool.description,
      icon: tool.icon,
      to: { name: 'tool-conversion', params: { tool: tool.slug } },
    })),
  },
  {
    label: 'utilities',
    icon: 'ti ti-tool',
    to: { name: 'tool-home', hash: '#utilities' },
    cards: utilityTools.slice(0, 6).map((tool) => ({
      title: tool.shortTitle,
      description: tool.description,
      icon: tool.icon,
      to: { name: 'tool-utility', params: { tool: tool.slug } },
    })),
  },
  {
    label: 'explore',
    icon: 'ti ti-world',
    to: { name: 'explore' },
    hidden: !appInfo.appInfo?.public_pastes_enabled,
  },
])
</script>

<template>
  <div class="min-h-screen bg-white dark:bg-[#121212]">
    <header class="sticky top-0 z-50 bg-white/80 backdrop-blur-xl dark:bg-[#121212]/80">
      <div class="flex w-full items-center justify-between gap-3 px-4 py-3 md:px-6">
        <div class="flex items-center gap-2 md:gap-3">
          <router-link
            :to="{ name: 'home' }"
            class="flex items-center gap-2 py-2 transition-opacity hover:opacity-80"
            aria-label="Pastefy Tools"
          >
            <img
              v-if="appInfo.appInfo?.custom_logo"
              :src="appInfo.appInfo.custom_logo"
              alt="logo"
              class="h-[1.7rem]"
            />
            <Logo v-else />
          </router-link>

          <nav class="hidden items-center gap-1 md:flex">
            <template v-for="link of topLinks.filter((entry) => !entry.hidden)" :key="link.label">
              <div v-if="'cards' in link" class="group relative">
                <Button
                  as="router-link"
                  :to="link.to"
                  text
                  severity="contrast"
                  size="small"
                  :icon="link.icon"
                  :label="link.label"
                />
                <div
                  class="pointer-events-none absolute top-full left-0 pt-2 opacity-0 transition-all duration-150 group-hover:pointer-events-auto group-hover:opacity-100"
                >
                  <div
                    :class="[
                      'rounded-xl border border-neutral-200 bg-white p-3 shadow-xl dark:border-neutral-700 dark:bg-neutral-900',
                      link.panelClass || 'w-[38rem]',
                    ]"
                  >
                    <div class="grid grid-cols-2 gap-2 xl:grid-cols-3">
                      <router-link
                        v-for="card of link.cards"
                        :key="card.title"
                        :to="card.to"
                        class="rounded-xl border border-neutral-200 bg-neutral-100 p-3 transition-all hover:bg-white dark:border-neutral-700 dark:bg-neutral-800 dark:hover:bg-neutral-950"
                      >
                        <div class="flex items-start gap-3">
                          <div
                            class="flex h-10 w-10 shrink-0 items-center justify-center rounded-lg bg-white text-neutral-700 dark:bg-neutral-900 dark:text-neutral-200"
                          >
                            <i :class="`ti ti-${card.icon}`" />
                          </div>
                          <div class="min-w-0">
                            <div class="font-medium">{{ card.title }}</div>
                            <p
                              class="mt-1 line-clamp-2 text-xs text-neutral-500 dark:text-neutral-400"
                            >
                              {{ card.description }}
                            </p>
                          </div>
                        </div>
                      </router-link>
                    </div>
                  </div>
                </div>
              </div>

              <Button
                v-else
                as="router-link"
                :to="link.to"
                text
                severity="contrast"
                size="small"
                :icon="link.icon"
                :label="link.label"
              />
            </template>
          </nav>
        </div>

        <div class="flex items-center gap-2">
          <div class="relative hidden md:block">
            <InputText
              v-model="toolSearch"
              placeholder="Search tools..."
              class="w-[10rem]"
              size="small"
            />
            <div
              v-if="toolSearchResults.length"
              class="absolute top-full right-0 mt-2 w-[24rem] rounded-xl border border-neutral-200 bg-white p-2 shadow-xl dark:border-neutral-700 dark:bg-neutral-900"
            >
              <router-link
                v-for="entry of toolSearchResults"
                :key="`${entry.kind}:${entry.slug}`"
                :to="entry.to"
                class="flex items-start gap-3 rounded-xl p-3 transition-colors hover:bg-neutral-100 dark:hover:bg-neutral-800"
                @click="toolSearch = ''"
              >
                <div
                  class="flex h-10 w-10 shrink-0 items-center justify-center rounded-lg bg-neutral-100 text-neutral-700 dark:bg-neutral-800 dark:text-neutral-200"
                >
                  <i :class="`ti ti-${entry.icon}`" />
                </div>
                <div class="min-w-0">
                  <div class="font-medium">{{ entry.shortTitle }}</div>
                  <p class="mt-1 line-clamp-2 text-xs text-neutral-500 dark:text-neutral-400">
                    {{ entry.description }}
                  </p>
                </div>
              </router-link>
            </div>
          </div>
          <Button
            v-if="!currentUserStore.user && currentUserStore.authTypes?.[0]"
            @click="currentUserStore.authTypes?.length > 1 ? (loginModalVisible = true) : null"
            :as="currentUserStore.authTypes?.length === 1 ? 'a' : 'button'"
            :href="`/api/v2/auth/oauth2/${currentUserStore.authTypes[0]}`"
            icon="ti ti-login"
            label="login"
            severity="contrast"
            outlined
            size="small"
            :loading="currentUserStore.userLoading"
          />
          <button
            v-if="currentUserStore.user"
            class="cursor-pointer"
            @click="(e) => userMenu?.toggle(e)"
          >
            <img
              class="h-[32px] w-[32px] rounded-full border border-neutral-200 object-cover dark:border-neutral-700"
              :src="currentUserStore.user.profile_picture"
              alt="Profile Picture"
            />
          </button>
        </div>
      </div>
    </header>

    <main class="mx-auto w-full max-w-[1500px] px-4 py-6 md:px-6 md:py-8">
      <router-view />
    </main>
  </div>

  <Popover ref="userMenu" class="w-[14rem]" :pt="{ content: 'p-0' }">
    <UserMenu @element-clicked="userMenu?.hide()" />
  </Popover>

  <LoginModal v-model:visible="loginModalVisible" />
</template>
