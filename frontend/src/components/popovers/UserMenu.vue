<script setup lang="ts">
import Button from 'primevue/button'
import { useCurrentUserStore } from '@/stores/current-user.ts'
import { useAppStore } from '@/stores/app.ts'
import { useAppInfoStore } from '@/stores/app-info.ts'

const currentUserStore = useCurrentUserStore()
const appStore = useAppStore()
const appInfo = useAppInfoStore()
import { useConfirm } from 'primevue/useconfirm'
import { useTranslation } from 'i18next-vue'
const confirm = useConfirm()
const { t } = useTranslation()

const emit = defineEmits(['elementClicked'])
</script>
<template>
  <Button
    as="router-link"
    :to="{ name: 'home-page' }"
    text
    icon="ti ti-home text-lg"
    :label="$t('paste.myPastes')"
    severity="contrast"
    size="small"
    fluid
    class="flex justify-start md:hidden"
    @click="emit('elementClicked')"
  />
  <Button
    as="router-link"
    :to="{ name: 'explore' }"
    text
    icon="ti ti-world text-lg"
    :label="$t('nav.explore')"
    severity="contrast"
    size="small"
    fluid
    class="flex justify-start md:hidden"
    @click="emit('elementClicked')"
  />
  <Button
    as="router-link"
    :to="{ name: 'stars' }"
    v-if="currentUserStore.user?.logged_in"
    text
    icon="ti ti-star text-lg"
    :label="$t('nav.stars')"
    severity="contrast"
    size="small"
    fluid
    class="justify-start"
    @click="emit('elementClicked')"
  />
  <Button
    as="router-link"
    :to="{ name: 'api-keys' }"
    text
    icon="ti ti-key text-lg"
    :label="$t('nav.apiKeys')"
    severity="contrast"
    size="small"
    fluid
    class="justify-start"
    @click="emit('elementClicked')"
  />
  <Button
    as="router-link"
    :to="{ name: 'user-analytics' }"
    v-if="appInfo.appInfo?.analytics_enabled && currentUserStore.user?.logged_in"
    text
    icon="ti ti-chart-line text-lg"
    label="Analytics"
    severity="contrast"
    size="small"
    fluid
    class="justify-start"
    @click="emit('elementClicked')"
  />
  <Button
    text
    @click="
      () => {
        emit('elementClicked')
        appStore.settingsModalShown = true
      }
    "
    icon="ti ti-settings text-lg"
    :label="$t('settings.title')"
    severity="contrast"
    size="small"
    fluid
    class="justify-start"
  />

  <Button
    as="a"
    target="_blank"
    href="https://accounts.interaapps.de"
    v-if="currentUserStore.user?.logged_in && currentUserStore.user.auth_type === 'interaapps'"
    text
    icon="ti ti-user text-lg"
    :label="$t('auth.accountSettings')"
    severity="contrast"
    size="small"
    fluid
    class="justify-start"
    @click="emit('elementClicked')"
  />
  <Button
    as="router-link"
    :to="{ name: 'admin-home' }"
    v-if="currentUserStore.user?.type === 'ADMIN'"
    text
    icon="ti ti-terminal text-lg"
    :label="$t('components.userMenu.admin')"
    severity="contrast"
    size="small"
    fluid
    class="justify-start"
    @click="emit('elementClicked')"
  />
  <Button
    text
    icon="ti ti-logout text-lg"
    :label="$t('auth.signOut')"
    severity="contrast"
    size="small"
    fluid
    class="justify-start"
    @click="
      () => {
        emit('elementClicked')
        confirm.require({
          message: t('auth.logoutConfirm'),
          header: t('auth.logout'),
          accept: async () => {
            currentUserStore.logout()
          },
        })
      }
    "
  />
</template>
