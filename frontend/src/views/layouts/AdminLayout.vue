<script setup lang="ts">
import Button from 'primevue/button'
import { useAppInfoStore } from '@/stores/app-info.ts'

const appInfo = useAppInfoStore()
</script>
<template>
  <div class="mx-auto max-w-[1200px]">
    <div class="flex flex-col gap-4">
      <h1 class="text-2xl font-bold">{{ $t('views.adminLayout.admin') }}</h1>
      <div class="flex gap-1">
        <Button
          as="router-link"
          :to="{ name: 'admin-home' }"
          :label="$t('nav.home')"
          size="small"
          outlined
          severity="contrast"
        />
        <Button
          as="router-link"
          :to="{ name: 'admin-users' }"
          :label="$t('common.users')"
          size="small"
          outlined
          severity="contrast"
        />
        <Button
          as="router-link"
          :to="{ name: 'admin-pastes' }"
          :label="$t('paste.pastes')"
          size="small"
          outlined
          severity="contrast"
        />
        <Button
          v-if="appInfo.appInfo?.analytics_enabled"
          as="router-link"
          :to="{ name: 'admin-analytics' }"
          label="Analytics"
          size="small"
          outlined
          severity="contrast"
        />
      </div>

      <div>
        <router-view />
      </div>
    </div>
  </div>
</template>
