<script setup lang="ts">
import { useCurrentUserStore } from '@/stores/current-user.ts'
import { useTitle } from '@vueuse/core'
import UserHome from '@/views/UserHome.vue'
import Highlighted from '@/components/Highlighted.vue'
import FolderCard from '@/components/lists/FolderCard.vue'
const currentUserStore = useCurrentUserStore()
import Button from 'primevue/button'
import { useAppInfoStore } from '@/stores/app-info.ts'
import LoginModal from '@/components/modals/LoginModal.vue'
import { ref } from 'vue'
import { eventBus } from '@/main.ts'

useTitle(`Pastefy • Share your code snippets`)

const appInfo = useAppInfoStore()

const loginModalVisible = ref(false)

const exampleCode = `function sayHello(name: string) {
  const hello =  \`Hello \${name}!\`;
}

sayHello('World')`
const exampleIntegrationCode = `import axios from 'axios'

axios.create({
  baseURL: '${window.location.origin}/api/v2',
  headers: {
    Authorization: \`Bearer \${config.value.apiKey}\`
  }
})

await axios.post('/paste', {
  title: 'Hello',
  content: 'Hello, Pastefy!',
})`
</script>

<template>
  <main class="mx-auto w-full max-w-[1200px]">
    <UserHome v-if="currentUserStore.user" />
    <div v-else :ref="() => eventBus.emit('pageLoaded', 'home')">
      <div class="mx-auto mb-10 max-w-[1000px]">
        <div class="mb-5 flex items-center justify-between">
          <h1 class="text-3xl font-bold">Pastefy</h1>

          <Button
            v-if="!currentUserStore.user && currentUserStore.authTypes?.[0]"
            @click="currentUserStore.authTypes?.length > 1 ? (loginModalVisible = true) : null"
            :as="currentUserStore.authTypes?.length === 1 ? 'a' : 'button'"
            :href="`/api/v2/auth/oauth2/${currentUserStore.authTypes[0]}`"
            icon="ti ti-user text-lg"
            severity="contrast"
            label="login"
            text
            size="small"
            :loading="currentUserStore.userLoading"
          />
        </div>

        <div class="mb-3 flex flex-col gap-3 md:grid md:h-[18rem] md:grid-cols-[2fr_1fr]">
          <div
            class="flex h-full flex-col items-end overflow-hidden rounded-xl border border-neutral-200 bg-neutral-100 md:grid md:grid-cols-2 dark:border-neutral-700 dark:bg-neutral-800"
          >
            <div class="flex h-full flex-col gap-3 p-4">
              <h2 class="text-xl font-bold">Share Code</h2>
              <p>
                Share your code snippets with the world. Paste your code and share it with others.
              </p>
            </div>
            <div>
              <div
                class="rounded-tl-md border-t border-l border-neutral-300 dark:border-neutral-700"
              >
                <Highlighted :contents="exampleCode" file-name=".js" />
              </div>
            </div>
          </div>

          <div
            class="flex h-full flex-col justify-between overflow-hidden rounded-xl border border-neutral-200 bg-neutral-100 dark:border-neutral-700 dark:bg-neutral-800"
          >
            <div class="flex h-full flex-col gap-1 p-4 pb-0">
              <h2 class="text-xl font-bold">Organize</h2>
              <p>Create folders and organize your code snippets in a structured way.</p>
            </div>
            <div class="flex w-full items-center justify-between gap-2 p-2 px-5">
              <FolderCard
                disabled
                v-for="name of ['Game Scripts', 'Web', 'Mobile']"
                :key="name"
                :folder="{
                  id: '1',
                  name,
                  children: [],
                  created: '',
                  pastes: [],
                }"
              />
            </div>
          </div>
        </div>
        <div class="mb-3 flex flex-col gap-3 md:grid md:h-[18rem] md:grid-cols-[1fr_2fr]">
          <div
            class="flex h-full flex-col justify-between overflow-hidden rounded-xl border border-neutral-200 bg-neutral-100 dark:border-neutral-700 dark:bg-neutral-800"
          >
            <div class="flex h-full flex-col gap-1 p-4 pb-0">
              <h2 class="text-xl font-bold">Keep secret</h2>
              <p>
                Use client encryption to keep your code snippets secret. Even we can't read your
                encrypted pastes.
              </p>
            </div>
            <div class="flex w-full flex-col items-center justify-between gap-2 p-2">
              <i class="ti ti-lock text-3xl" />
              <div
                class="mono w-full rounded-lg border border-neutral-300 p-2 text-center dark:border-neutral-600"
              >
                * * * * * *
              </div>
            </div>
          </div>

          <div
            class="h-full items-start overflow-hidden rounded-xl border border-neutral-200 bg-neutral-100 md:grid md:grid-cols-2 dark:border-neutral-700 dark:bg-neutral-800"
          >
            <div class="flex h-full flex-col justify-between gap-3 p-4">
              <div class="flex-col gap-3">
                <h2 class="text-xl font-bold">Integration</h2>
                <p>
                  Use our API to integrate Pastefy with your applications or simply embed your paste
                  into your own website easily.
                </p>
              </div>

              <div>
                <Button
                  as="a"
                  icon="ti ti-book"
                  href="https://intera.dev/docs/pastefy"
                  target="_blank"
                  label="api docs"
                  size="small"
                  severity="contrast"
                  outlined
                />
              </div>
            </div>
            <div class="h-full overflow-hidden">
              <div
                class="h-full border-t border-neutral-300 md:border-t-0 md:border-l dark:border-neutral-700"
              >
                <Highlighted hide-divider :contents="exampleIntegrationCode" file-name=".js" />
              </div>
            </div>
          </div>
        </div>

        <div
          v-if="appInfo.appInfo?.public_pastes_enabled"
          class="flex h-full flex-col items-center gap-3 overflow-hidden rounded-xl border border-neutral-200 bg-neutral-100 p-3 dark:border-neutral-700 dark:bg-neutral-800"
        >
          <i class="ti ti-world text-4xl" />
          <h2 class="text-lg font-bold">Explore Public Pastes</h2>

          <Button
            as="router-link"
            :to="{ name: 'explore' }"
            label="explore"
            outlined
            fluid
            class="max-w-[10rem]"
          />
        </div>
      </div>
    </div>
  </main>
  <LoginModal v-model:visible="loginModalVisible" />
</template>
