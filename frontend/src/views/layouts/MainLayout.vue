<script setup lang="ts">
import Sidebar from '@/components/layout/Sidebar.vue'
import Button from 'primevue/button'
import Popover from 'primevue/popover'
import { computed, defineAsyncComponent, ref, useTemplateRef } from 'vue'
import { useConfig } from '@/composables/config.ts'
import type { PopoverMethods } from 'primevue'
import { useCurrentUserStore } from '@/stores/current-user.ts'
import { useRoute } from 'vue-router'
import { useWindowSize } from '@vueuse/core'
import LoginModal from '@/components/modals/LoginModal.vue'
import { useAppInfoStore } from '@/stores/app-info.ts'
import GlobalSearch from '@/components/GlobalSearch.vue'
import UserMenu from '@/components/popovers/UserMenu.vue'
import ComponentInjection from '@/components/ComponentInjection.vue'
import { useCurrentPasteStore } from '@/stores/current-paste.ts'
const loginModalVisible = ref(false)

const Logo = defineAsyncComponent(() => import('@/components/Logo.vue'))

const userMenu = useTemplateRef<PopoverMethods>('userMenu')

const currentUserStore = useCurrentUserStore()

const windowSize = useWindowSize()
const route = useRoute()
const isMobile = computed(() => windowSize.width.value < 768)
const isHomeMobile = computed(() => route.name === 'home' && isMobile.value)

const config = useConfig()

const appInfo = useAppInfoStore()

const handleDrop = async (event: DragEvent) => {
  const currentPaste = useCurrentPasteStore()
  const files = event.dataTransfer?.files || []

  if (files?.length > 0) event.preventDefault()

  const contents: { name: string; content: string }[] = []
  if (files && files.length > 0) {
    for (const file of files) {
      const reader = new FileReader()

      const disallowedTypes = [
        'image/',
        'video/',
        'audio/',
        'application/pdf',
        'application/zip',
        'application/octet-stream',
      ]
      if (
        file.type !== 'image/svg+xml' &&
        disallowedTypes.some((type) => file.type.startsWith(type) || file.type === type)
      )
        continue

      const content = await new Promise<string>((res) => {
        reader.onload = (e) => {
          const content = e.target?.result
          if (content) {
            if (typeof content === 'string') {
              res(content)
            } else {
              res('')
            }
          }
        }
        reader.readAsText(file)
      })

      contents.push({
        name: file.name,
        content,
      })
    }
  }

  if (contents.length === 0) return

  let appendToMultiPart = currentPaste.type === 'MULTI_PASTE' || contents.length > 1
  if (!appendToMultiPart && currentPaste.contents !== '') {
    appendToMultiPart = true
    currentPaste.addMultiPart()
  }

  if (appendToMultiPart) {
    for (const { name, content } of contents) {
      currentPaste.addMultiPart()
      currentPaste.multiPastes[currentPaste.currentMultiPasteIndex!] = {
        name: name,
        contents: content,
      }
      currentPaste.selectMultiPart(currentPaste.currentMultiPasteIndex!)
    }
  } else {
    currentPaste.title ||= contents[0].name
    currentPaste.contents = contents[0].content
  }
}
</script>
<template>
  <div
    :class="`grid h-full w-full ${
      isHomeMobile ? 'grid-cols-1' : config.sideBarShown ? 'grid-cols-[340px_1fr]' : 'grid-cols-1'
    }`"
    @dragover.prevent
    @dragenter.prevent
    @dragleave.prevent
    @drop="handleDrop"
  >
    <Sidebar
      class="fixed top-0 left-0 w-[340px] transition-all"
      :is-hidden="!(isHomeMobile || config.sideBarShown)"
      :class="isHomeMobile ? 'w-full' : config.sideBarShown ? 'flex' : 'hidden'"
    />
    <div
      :class="`fixed z-100 flex max-w-full items-center justify-between ${isHomeMobile || !isMobile ? '' : 'bg-white/60 backdrop-blur-xl dark:bg-[#121212]/70'} p-2 pl-1 md:bg-none md:pl-2 ${config.sideBarShown && !isHomeMobile ? 'w-[340px]' : 'w-full'}`"
    >
      <Button
        as="router-link"
        class="flex md:hidden"
        :to="{ name: 'home' }"
        :icon="`ti ti-plus text-xl`"
        severity="contrast"
        :disabled="isHomeMobile"
        text
        aria-label="Create Paste"
      />
      <Button
        class="hidden md:flex"
        @click="config.sideBarShown = !config.sideBarShown"
        :icon="`ti ti-layout-sidebar text-xl`"
        severity="contrast"
        :disabled="isHomeMobile"
        text
        v-shortkey="['meta', 'b']"
        @shortkey="config.sideBarShown = !config.sideBarShown"
        aria-label="Toggle Sidebar"
      />

      <router-link
        v-if="config.sideBarShown || isMobile"
        :to="{ name: 'home' }"
        class="transition-all hover:scale-[1.05] active:scale-[0.95]"
        aria-label="Home"
      >
        <img
          v-if="appInfo.appInfo?.custom_logo"
          :src="appInfo.appInfo?.custom_logo"
          alt="logo"
          class="h-[2rem]"
        />
        <Logo v-else />
      </router-link>
      <Button
        v-if="!currentUserStore.user && currentUserStore.authTypes?.[0]"
        @click="currentUserStore.authTypes?.length > 1 ? (loginModalVisible = true) : null"
        :as="currentUserStore.authTypes?.length === 1 ? 'a' : 'button'"
        :href="`/api/v2/auth/oauth2/${currentUserStore.authTypes[0]}`"
        icon="ti ti-login text-xl"
        severity="contrast"
        text
        :loading="currentUserStore.userLoading"
        v-tooltip="{ value: 'Login', showDelay: 500 }"
        aria-label="Login"
      />
      <button
        v-else-if="currentUserStore.user"
        class="mr-1 cursor-pointer"
        @click="(e) => userMenu?.toggle(e)"
      >
        <img
          class="h-[30px] w-[30px] rounded-full border-1 border-neutral-200 object-cover dark:border-neutral-700"
          :src="currentUserStore.user.profile_picture"
          alt="Profile Picture"
        />
      </button>
      <Button v-else class="cursor-default opacity-0" icon="ti ti-login text-xl" text />
    </div>
    <div v-if="config.sideBarShown" />
    <div
      v-if="!isHomeMobile"
      class="w-full overflow-auto"
      :class="route.meta.noSpacing ? '' : 'p-4 pt-18 md:p-8 md:pt-20'"
    >
      <ComponentInjection type="main-layout-main-top" />
      <div id="full-screen-content"></div>
      <router-view :key="route.fullPath" />
      <ComponentInjection type="main-layout-main-bottom" />
    </div>
  </div>
  <Popover ref="userMenu" class="w-[14rem]" :pt="{ content: 'p-0' }">
    <UserMenu @element-clicked="userMenu?.hide()" />
  </Popover>

  <GlobalSearch />

  <LoginModal v-model:visible="loginModalVisible" />
</template>
