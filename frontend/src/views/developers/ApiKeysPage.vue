<script setup lang="ts">
import { useAsyncState, useClipboard, useTitle } from '@vueuse/core'
import { client } from '@/main.ts'
import Button from 'primevue/button'
import LoadingContainer from '@/components/LoadingContainer.vue'
import ErrorContainer from '@/components/ErrorContainer.vue'
import { useConfirm } from 'primevue/useconfirm'
import { AxiosError } from 'axios'
import { useToast } from 'primevue'
import router from '@/router'
import Tabs from 'primevue/tabs'
import TabList from 'primevue/tablist'
import TabPanels from 'primevue/tabpanels'
import TabPanel from 'primevue/tabpanel'
import Tab from 'primevue/tab'
import { useCurrentUserStore } from '@/stores/current-user.ts'

useTitle('Api-Keys | pastefy')

const toast = useToast()

const currentUserStore = useCurrentUserStore()

const {
  isLoading,
  state: keys,
  error,
  execute: load,
} = useAsyncState(async () => {
  try {
    return (await client.get('/api/v2/user/keys')).data as string[]
  } catch (e) {
    if (e instanceof AxiosError) {
      if (e?.status === 401) {
        toast.add({
          severity: 'error',
          summary: 'Login required',
          detail: 'You need to be logged in to view your api keys',
          life: 10000,
        })
        router.replace({ name: 'home' })
      }
    }
    throw e
  }
}, undefined)

const confirm = useConfirm()
const deleteKey = async (key: string) => {
  confirm.require({
    message: 'Are you sure you want to delete this key?',
    header: 'Delete key',
    accept: async () => {
      await client.delete(`/api/v2/user/keys/${key}`)
      await load()
    },
  })
}

const {
  isLoading: isCreating,
  error: creationError,
  execute: createKey,
} = useAsyncState(
  async () => {
    await client.post('/api/v2/user/keys')
    await load()
  },
  undefined,
  { immediate: false },
)

const clipboard = useClipboard()
</script>
<template>
  <div class="mx-auto max-w-[1200px] p-4">
    <Tabs
      value="apikeys"
      :pt="{
        root: 'bg-transparent',
      }"
    >
      <TabList :pt="{ root: 'bg-transparent', tabList: 'bg-transparent' }">
        <Tab value="apikeys">Api Keys</Tab>
        <Tab as="a" href="https://docs.pastefy.app/api/" value="apidocs"> API Docs </Tab>
        <Tab
          as="a"
          href="https://docs.pastefy.app/api/oauth.html"
          value="oauth2apps"
          v-if="currentUserStore.user?.auth_types?.includes('interaapps')"
        >
          OAuth2 Apps
        </Tab>
      </TabList>
      <TabPanels
        :pt="{
          root: 'bg-transparent p-0 pt-10',
        }"
      >
        <TabPanel value="apikeys" :pt="{ root: 'p-0' }">
          <div class="flex flex-col gap-2">
            <ErrorContainer v-if="error" :error="error as any" />
            <LoadingContainer v-else-if="isLoading" />
            <div v-else class="flex flex-col gap-2">
              <div
                v-for="key of keys"
                :key
                class="flex justify-between rounded-md border border-neutral-200 p-2 px-3 dark:border-neutral-600"
              >
                <span class="mono text-sm"> {{ key.substring(0, 5) }}*************** </span>

                <div class="flex">
                  <Button
                    @click="clipboard.copy(key)"
                    icon="ti ti-copy text-lg"
                    text
                    size="small"
                    severity="contrast"
                    class="p-0"
                  />
                  <Button
                    @click="deleteKey(key)"
                    icon="ti ti-trash text-lg"
                    text
                    size="small"
                    severity="danger"
                    class="p-0"
                  />
                </div>
              </div>
            </div>
            <div class="flex justify-end gap-1">
              <Button
                as="a"
                href="https://docs.pastefy.app"
                target="_blank"
                label="api-docs"
                text
                size="small"
              />
              <Button
                :loading="isCreating"
                @click="() => createKey()"
                label="add api-key"
                size="small"
                class="text-white"
              />
            </div>
            <ErrorContainer v-if="creationError" :error="creationError as any" />
          </div>
        </TabPanel>
        <TabPanel value="oauth2apps"></TabPanel>
        <TabPanel value="apidocs"></TabPanel>
      </TabPanels>
    </Tabs>
  </div>
</template>
