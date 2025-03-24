<script setup lang="ts">
import { useAsyncState, useTitle } from '@vueuse/core'
import { client } from '@/main.ts'
import LoadingContainer from '@/components/LoadingContainer.vue'
import ErrorContainer from '@/components/ErrorContainer.vue'
import Button from 'primevue/button'
import type { UserType } from '@/types/user.ts'
import { ref } from 'vue'
import Pagination from '@/components/Pagination.vue'
import { useConfirm } from 'primevue/useconfirm'

useTitle('Users - Admin | pastefy')

type UserAsAdmin = {
  auth_id: string
  auth_provider: string
  avatar: string
  created_at: string
  e_mail: string
  id: string
  name: string
  type: UserType
  unique_name: string
  updated_at: string
}

const page = ref(1)

const {
  isLoading,
  state: users,
  error,
  execute: load,
} = useAsyncState(async () => {
  return (
    await client.get('/api/v2/admin/users', {
      params: {
        page: page.value,
      },
    })
  ).data as UserAsAdmin[]
}, undefined)

const confirm = useConfirm()

const setUserType = async (id: string, type: UserType) => {
  confirm.require({
    message: "Are you sure you want to change this user's type?",
    header: 'Change user type',
    accept: async () => {
      await client.put(`/api/v2/admin/users/${id}`, {
        type,
      })
      load()
    },
  })
}

const deleteUser = async (id: string) => {
  confirm.require({
    message: 'Are you sure you want to delete this user?',
    header: 'Delete user',
    accept: async () => {
      await client.delete(`/api/v2/admin/users/${id}`)
      load()
    },
  })
}
</script>
<template>
  <ErrorContainer v-if="error" :error="error as any" />
  <LoadingContainer v-else-if="isLoading" class="flex items-center justify-center p-3" />
  <div v-else-if="users">
    <div class="flex flex-col gap-2">
      <div
        v-for="user in users"
        :key="user.id"
        class="flex items-center justify-between rounded-lg border border-neutral-200 bg-white p-4 dark:border-neutral-700"
      >
        <div class="flex items-center gap-4">
          <img :src="user.avatar" class="h-8 w-8 rounded-full" alt="avatar" />
          <div>
            <h2 class="font-bold">{{ user.name }}</h2>
            <p class="text-sm opacity-60">{{ user.e_mail }}</p>
          </div>
        </div>
        <div class="flex items-center gap-0.5">
          <Button
            v-if="user.type === 'AWAITING_ACCESS'"
            severity="contrast"
            text
            rounded
            size="small"
            label="grant access"
            @click="setUserType(user.id, 'USER')"
          />
          <Button
            v-if="user.type === 'BLOCKED'"
            severity="contrast"
            text
            rounded
            size="small"
            label="unblock"
            @click="setUserType(user.id, 'USER')"
          />
          <Button
            v-else
            severity="contrast"
            text
            rounded
            size="small"
            label="block"
            @click="setUserType(user.id, 'BLOCKED')"
          />
          <Button
            severity="danger"
            text
            rounded
            icon="ti ti-trash text-lg"
            size="small"
            @click="deleteUser(user.id)"
          />
        </div>
      </div>
      <Pagination v-model:page="page" @update:page="() => load()" />
    </div>
  </div>
</template>
