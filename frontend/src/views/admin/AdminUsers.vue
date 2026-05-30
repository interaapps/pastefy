<script setup lang="ts">
import { useTranslation } from 'i18next-vue'
import { computed, watch } from 'vue'
import { useDebounceFn, useAsyncState, useTitle } from '@vueuse/core'
import { useRouteQuery } from '@vueuse/router'
import Button from 'primevue/button'
import InputText from 'primevue/inputtext'
import Select from 'primevue/select'
import Tag from 'primevue/tag'
import { useConfirm } from 'primevue/useconfirm'
import { client } from '@/main.ts'
import LoadingContainer from '@/components/LoadingContainer.vue'
import ErrorContainer from '@/components/ErrorContainer.vue'
import Pagination from '@/components/Pagination.vue'
import type { UserType } from '@/types/user.ts'

useTitle('Users - Admin | pastefy')

const { t } = useTranslation()
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

const page = useRouteQuery('page', 1, { transform: Number })
const pageLimit = useRouteQuery('pageLimit', 10, { transform: Number })
const search = useRouteQuery('search', '')
const userType = useRouteQuery<UserType | ''>('type', '')

const hasNext = computed(() => (users.value?.length || 0) >= pageLimit.value)

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
        page_limit: pageLimit.value,
        search: search.value || undefined,
        ...(userType.value ? { 'filter[type]': userType.value } : {}),
      },
    })
  ).data as UserAsAdmin[]
}, undefined)

const confirm = useConfirm()

const reloadFirstPage = () => {
  if (page.value !== 1) {
    page.value = 1
    return
  }
  load()
}

const debouncedReloadFirstPage = useDebounceFn(reloadFirstPage, 250)

watch(page, () => load())
watch([pageLimit, userType], () => reloadFirstPage())
watch(search, () => debouncedReloadFirstPage())

const setUserType = async (id: string, type: UserType) => {
  confirm.require({
    message: t('admin.users.changeTypeConfirm'),
    header: t('admin.users.changeTypeTitle'),
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
    message: t('admin.users.deleteConfirm'),
    header: t('admin.users.deleteTitle'),
    accept: async () => {
      await client.delete(`/api/v2/admin/users/${id}`)
      load()
    },
  })
}

const typeOptions: Array<{ label: string; value: UserType | '' }> = [
  { get label() { return t('views.adminUsers.options.allRoles') }, value: '' },
  { get label() { return t('views.adminUsers.options.admins') }, value: 'ADMIN' },
  { get label() { return t('views.adminUsers.options.users') }, value: 'USER' },
  { get label() { return t('views.adminUsers.options.awaitingAccess') }, value: 'AWAITING_ACCESS' },
  { get label() { return t('views.adminUsers.options.blocked') }, value: 'BLOCKED' },
]

const pageSizeOptions = [10, 20, 50].map((value) => ({
  label: `${value} / page`,
  value,
}))

const getUserTypeSeverity = (type: UserType) => {
  if (type === 'ADMIN') return 'danger'
  if (type === 'BLOCKED') return 'warn'
  if (type === 'AWAITING_ACCESS') return 'contrast'
  return 'success'
}
</script>
<template>
  <div class="flex flex-col gap-5">
    <section
      class="grid gap-3 rounded-2xl border border-neutral-200 bg-white/80 p-4 lg:grid-cols-[minmax(0,1fr)_220px_180px] dark:border-neutral-800 dark:bg-neutral-900/70"
    >
      <InputText v-model="search" fluid :placeholder="$t('views.adminUsers.searchByUsernameEmailOrProvider')" />
      <Select
        v-model="userType"
        :options="typeOptions"
        option-label="label"
        option-value="value"
        fluid
      />
      <Select
        v-model="pageLimit"
        :options="pageSizeOptions"
        option-label="label"
        option-value="value"
        fluid
      />
    </section>

    <div class="flex flex-wrap items-center gap-2 text-sm text-neutral-500 dark:text-neutral-400">
      <span>{{ $t('common.filters') }}</span>
      <Tag :value="search ? `Search: ${search}` : 'No search'" severity="contrast" />
      <Tag
        :value="userType ? `Role: ${userType}` : 'All roles'"
        :severity="userType ? 'info' : 'contrast'"
      />
    </div>

    <ErrorContainer v-if="error" :error="error as any" />
    <LoadingContainer v-else-if="isLoading" class="flex items-center justify-center p-3" />
    <div v-else-if="users" class="flex flex-col gap-3">
      <div
        v-if="users.length === 0"
        class="rounded-xl border border-dashed border-neutral-300 bg-white/80 p-8 text-center text-sm text-neutral-500 dark:border-neutral-700 dark:bg-neutral-900/60 dark:text-neutral-400"
      >
        {{ $t('views.adminUsers.noUsersMatchTheCurrentFilters') }}
      </div>

      <article
        v-for="user in users"
        :key="user.id"
        class="rounded-xl border border-neutral-200 bg-white p-4 dark:border-neutral-700 dark:bg-neutral-800/80"
      >
        <div class="flex flex-col gap-4 xl:flex-row xl:items-start xl:justify-between">
          <div class="flex items-start gap-4">
            <img :src="user.avatar" class="h-12 w-12 rounded-full object-cover" alt="avatar" />
            <div class="space-y-2">
              <div class="flex flex-wrap items-center gap-2">
                <h2 class="text-lg font-bold">{{ user.name }}</h2>
                <Tag :value="user.type" :severity="getUserTypeSeverity(user.type)" />
              </div>
              <div class="space-y-1 text-sm text-neutral-500 dark:text-neutral-400">
                <p>{{ user.e_mail || 'No email available' }}</p>
                <p class="font-mono">@{{ user.unique_name || user.name }}</p>
                <p>
                  {{ $t('views.adminUsers.provider') }}
                  <span class="font-medium text-neutral-700 dark:text-neutral-200">
                    {{ user.auth_provider }}
                  </span>
                </p>
              </div>
            </div>
          </div>

          <div class="flex flex-wrap gap-2 xl:max-w-[32rem] xl:justify-end">
            <Button
              v-if="user.type === 'AWAITING_ACCESS'"
              severity="contrast"
              size="small"
              outlined
              :label="$t('views.adminUsers.grantAccess')"
              icon="ti ti-circle-check"
              @click="setUserType(user.id, 'USER')"
            />
            <Button
              v-if="user.type !== 'ADMIN'"
              severity="danger"
              size="small"
              outlined
              :label="$t('views.adminUsers.makeAdmin')"
              icon="ti ti-crown"
              @click="setUserType(user.id, 'ADMIN')"
            />
            <Button
              v-if="user.type !== 'USER'"
              severity="success"
              size="small"
              outlined
              :label="$t('views.adminUsers.setUser')"
              icon="ti ti-user-check"
              @click="setUserType(user.id, 'USER')"
            />
            <Button
              v-if="user.type !== 'BLOCKED'"
              severity="warn"
              size="small"
              outlined
              :label="$t('views.adminUsers.block')"
              icon="ti ti-ban"
              @click="setUserType(user.id, 'BLOCKED')"
            />
            <Button
              as="router-link"
              :to="{ name: 'user', params: { user: user.unique_name || user.name } }"
              severity="contrast"
              size="small"
              text
              :label="$t('common.profile')"
              icon="ti ti-user"
            />
            <Button
              as="router-link"
              :to="{
                name: 'admin-pastes',
                query: {
                  userId: user.id,
                  userName: user.name,
                  page: '1',
                },
              }"
              severity="contrast"
              size="small"
              text
              :label="$t('views.adminUsers.viewPastes')"
              icon="ti ti-script"
            />
            <Button
              severity="danger"
              size="small"
              text
              :label="$t('common.delete')"
              icon="ti ti-trash"
              @click="deleteUser(user.id)"
            />
          </div>
        </div>
      </article>

      <Pagination
        v-model:page="page"
        :has-next="hasNext"
        :loading="isLoading"
        @update:page="load"
      />
    </div>
  </div>
</template>
