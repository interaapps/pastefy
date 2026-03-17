<script setup lang="ts">
import Button from 'primevue/button'
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

const Logo = defineAsyncComponent(() => import('@/components/Logo.vue'))

const loginModalVisible = ref(false)
const currentUserStore = useCurrentUserStore()
const appInfo = useAppInfoStore()
const userMenu = useTemplateRef<PopoverMethods>('userMenu')

const topLinks = computed(() => [
  {
    label: 'tools',
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
                    class="w-[38rem] rounded-xl border border-neutral-200 bg-white p-3 shadow-xl dark:border-neutral-700 dark:bg-neutral-900"
                  >
                    <div class="grid grid-cols-2 gap-2">
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
