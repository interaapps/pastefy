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
const exampleIntegrationCode = `import { PastefyClient }
    from '@interaapps/pastefy';

const client
  = new PastefyClient(apiKey${window.location.origin !== 'https://pastefy.app' ? `, "${window.location.origin}"` : ''});

console.log(
  await client.getPaste('id')
);

await client.createPaste({
  title: 'Hello World',
  content: 'This is a sample paste.'
});`
</script>

<template>
  <main class="mx-auto w-full max-w-[1200px]">
    <UserHome v-if="currentUserStore.user" />
    <div v-else :ref="() => eventBus.emit('pageLoaded', 'home')">
      <div class="mx-auto mb-10 max-w-[1000px]">
        <div class="mb-5 flex items-center justify-between">
          <h1 class="text-3xl font-bold">{{ $t('views.homeView.pastefy') }}</h1>

          <Button
            v-if="!currentUserStore.user && currentUserStore.authTypes?.[0]"
            @click="currentUserStore.authTypes?.length > 1 ? (loginModalVisible = true) : null"
            :as="currentUserStore.authTypes?.length === 1 ? 'a' : 'button'"
            :href="`/api/v2/auth/oauth2/${currentUserStore.authTypes[0]}`"
            icon="ti ti-user text-lg"
            severity="contrast"
            :label="$t('auth.login')"
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
              <h2 class="text-xl font-bold">{{ $t('home.shareCode') }}</h2>
              <p>{{ $t('home.shareCodeDescription') }}</p>
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
              <h2 class="text-xl font-bold">{{ $t('home.organize') }}</h2>
              <p>{{ $t('home.organizeDescription') }}</p>
            </div>
            <div class="flex w-full items-center justify-between gap-2 p-2 px-5">
              <FolderCard
                disabled
                v-for="(name, i) of ['Game Scripts', 'Web', 'Mobile']"
                :key="name"
                :folder="{
                  id: String(i),
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
              <h2 class="text-xl font-bold">{{ $t('home.keepSecret') }}</h2>
              <p>{{ $t('home.keepSecretDescription') }}</p>
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
                <h2 class="text-xl font-bold">{{ $t('home.integration') }}</h2>
                <p>{{ $t('home.integrationDescription') }}</p>
              </div>

              <div>
                <Button
                  as="a"
                  icon="ti ti-book"
                  href="https://docs.pastefy.app/api/"
                  target="_blank"
                  :label="$t('views.homeView.apiDocs')"
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
          class="mb-3 flex h-full flex-col items-center gap-3 overflow-hidden rounded-xl border border-neutral-200 bg-neutral-100 p-3 dark:border-neutral-700 dark:bg-neutral-800"
        >
          <i class="ti ti-world text-4xl" />
          <h2 class="text-lg font-bold">{{ $t('views.homeView.explorePublicPastes') }}</h2>

          <Button
            as="router-link"
            :to="{ name: 'explore' }"
            :label="$t('views.homeView.explore')"
            outlined
            fluid
            class="max-w-[10rem]"
          />
        </div>

        <div class="mb-3 flex flex-col gap-3 md:grid md:grid-cols-[3fr_1.4fr]">
          <div
            class="grid h-full gap-3 rounded-xl border border-neutral-200 bg-neutral-100 p-3 md:grid-cols-3 dark:border-neutral-700 dark:bg-neutral-800"
          >
            <router-link
              :to="{ name: 'tool-home', hash: '#preview-tools' }"
              class="rounded-xl border border-neutral-200 bg-white p-4 transition-all hover:-translate-y-0.5 hover:border-neutral-300 hover:bg-neutral-50 dark:border-neutral-700 dark:bg-neutral-900 dark:hover:border-neutral-600 dark:hover:bg-neutral-950"
            >
              <div class="flex items-center gap-2 text-sm text-neutral-500 dark:text-neutral-400">
                <i class="ti ti-code-dots" />
                {{ $t('common.preview') }}
              </div>
              <h3 class="mt-3 font-semibold">{{ $t('views.homeView.richFileViews') }}</h3>
              <p class="mt-2 text-sm text-neutral-600 dark:text-neutral-300">
                {{ $t('views.homeView.richFileViewsDescription') }}
              </p>
            </router-link>

            <router-link
              :to="{ name: 'tool-home', hash: '#conversions' }"
              class="rounded-xl border border-neutral-200 bg-white p-4 transition-all hover:-translate-y-0.5 hover:border-neutral-300 hover:bg-neutral-50 dark:border-neutral-700 dark:bg-neutral-900 dark:hover:border-neutral-600 dark:hover:bg-neutral-950"
            >
              <div class="flex items-center gap-2 text-sm text-neutral-500 dark:text-neutral-400">
                <i class="ti ti-arrows-exchange" />
                {{ $t('views.homeView.convert') }}
              </div>
              <h3 class="mt-3 font-semibold">{{ $t('views.homeView.formatTransforms') }}</h3>
              <p class="mt-2 text-sm text-neutral-600 dark:text-neutral-300">
                {{ $t('views.homeView.formatTransformsDescription') }}
              </p>
            </router-link>

            <router-link
              :to="{ name: 'tool-home', hash: '#utilities' }"
              class="rounded-xl border border-neutral-200 bg-white p-4 transition-all hover:-translate-y-0.5 hover:border-neutral-300 hover:bg-neutral-50 dark:border-neutral-700 dark:bg-neutral-900 dark:hover:border-neutral-600 dark:hover:bg-neutral-950"
            >
              <div class="flex items-center gap-2 text-sm text-neutral-500 dark:text-neutral-400">
                <i class="ti ti-sparkles" />
                {{ $t('views.homeView.utilities') }}
              </div>
              <h3 class="mt-3 font-semibold">{{ $t('views.homeView.smallDevHelpers') }}</h3>
              <p class="mt-2 text-sm text-neutral-600 dark:text-neutral-300">
                {{ $t('views.homeView.utilitiesDescription') }}
              </p>
            </router-link>
          </div>

          <div
            class="flex h-full flex-col justify-between overflow-hidden rounded-xl border border-neutral-200 bg-neutral-100 dark:border-neutral-700 dark:bg-neutral-800"
          >
            <div class="flex h-full flex-col gap-3 p-4">
              <div class="flex items-center gap-2 text-neutral-500 dark:text-neutral-400">
                <i class="ti ti-tool text-lg" />
                <span class="text-sm font-medium">{{ $t('views.homeView.tools') }}</span>
              </div>

              <div>
                <h2 class="text-xl font-bold">{{ $t('views.homeView.previewConvertAndInspect') }}</h2>
                <p class="mt-1">
                  {{ $t('views.homeView.toolsDescription') }}
                </p>
              </div>
            </div>

            <div class="flex flex-wrap gap-2 p-4 pt-0">
              <Button
                as="router-link"
                :to="{ name: 'tool-home' }"
                :label="$t('views.homeView.openTools')"
                icon="ti ti-arrow-up-right"
                severity="contrast"
              />
            </div>
          </div>
        </div>
      </div>
    </div>
  </main>
  <LoginModal v-model:visible="loginModalVisible" />
</template>
