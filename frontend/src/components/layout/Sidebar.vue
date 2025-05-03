<script setup lang="ts">
import Button from 'primevue/button'
import Divider from 'primevue/divider'
import CreatePaste from '@/views/forms/CreatePaste.vue'
import { useAppInfoStore } from '@/stores/app-info.ts'
import ThemeSwitcher from '@/components/ThemeSwitcher.vue'
import { useAppStore } from '@/stores/app.ts'
import ComponentInjection from '@/components/ComponentInjection.vue'

const appInfo = useAppInfoStore()
const appStore = useAppStore()
</script>
<template>
  <div
    class="flex h-full flex-col justify-between overflow-auto border-r border-neutral-200 bg-neutral-100 p-2 dark:border-neutral-800 dark:bg-neutral-900"
    v-shortkey="['meta', 'k']"
    @shortkey="appStore.searchShown = true"
  >
    <div class="flex w-full flex-col gap-3 pt-[3.5rem]">
      <nav class="flex w-full gap-2">
        <Button
          as="router-link"
          :to="{ name: 'home-page' }"
          class="border-neutral-400 bg-gradient-to-b from-[transparent] to-neutral-100 dark:border-neutral-600 dark:to-neutral-900"
          size="small"
          icon="ti ti-home text-lg"
          label="home"
          severity="contrast"
          outlined
          fluid
        />
        <Button
          v-if="appInfo.appInfo?.public_pastes_enabled"
          as="router-link"
          :to="{ name: 'explore' }"
          class="border-neutral-400 bg-gradient-to-b from-[transparent] to-neutral-100 dark:border-neutral-600 dark:to-neutral-900"
          size="small"
          icon="ti ti-world text-lg"
          label="explore"
          severity="contrast"
          outlined
          fluid
        />
        <Button
          @click="appStore.searchShown = true"
          class="border-neutral-400 bg-gradient-to-b from-[transparent] to-neutral-100 dark:border-neutral-600 dark:to-neutral-900"
          size="small"
          icon="ti ti-search text-lg"
          label="search"
          severity="contrast"
          outlined
          fluid
        />
      </nav>
      <Divider class="m-0" />

      <CreatePaste />
    </div>

    <div id="sidenav-center" />
    <ComponentInjection type="sidebar-center" />

    <footer>
      <div id="sidenav-bottom" />
      <ComponentInjection type="sidebar-bottom" />

      <div class="flex w-full items-center justify-between gap-2">
        <div class="flex w-full items-center">
          <Button
            as="a"
            target="_blank"
            href="https://github.com/interaapps/pastefy"
            icon="ti ti-brand-github text-xl"
            text
            severity="contrast"
            aria-label="Source Code"
          />
          <template v-if="appInfo">
            <Button
              v-for="(link, label) of appInfo?.appInfo?.custom_footer || {}"
              :label="label"
              :key="label"
              as="a"
              :href="link"
              size="small"
              text
              severity="contrast"
            />
          </template>

          <ComponentInjection type="sidebar-footer-links" />
        </div>
        <ThemeSwitcher />
      </div>
    </footer>
  </div>
</template>
