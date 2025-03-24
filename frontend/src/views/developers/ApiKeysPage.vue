<script setup lang="ts">
import { useAsyncState, useClipboard, useTitle } from '@vueuse/core'
import { client } from '@/main.ts'
import Button from 'primevue/button'
import LoadingContainer from '@/components/LoadingContainer.vue'
import ErrorContainer from '@/components/ErrorContainer.vue'
import { useConfirm } from 'primevue/useconfirm'

useTitle('Api-Keys | pastefy')

const {
  isLoading,
  state: keys,
  error,
  execute: load,
} = useAsyncState(async () => {
  return (await client.get('/api/v2/user/keys')).data as string[]
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
  <div class="mx-auto flex max-w-[1200px] flex-col gap-2">
    <ErrorContainer v-if="error" :error="error as any" />
    <LoadingContainer v-else-if="isLoading" />
    <div v-else class="flex flex-col gap-2">
      <h1 class="mb-2 text-2xl font-bold">Api Keys</h1>
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
        href="https://intera.dev/docs/pastefy/spec"
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
</template>
