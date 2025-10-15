<script setup lang="ts">
import Button from 'primevue/button'
import { useCurrentUserStore } from '@/stores/current-user.ts'
import { useAppStore } from '@/stores/app.ts'

const currentUserStore = useCurrentUserStore()
const appStore = useAppStore()
import { useConfirm } from 'primevue/useconfirm'
const confirm = useConfirm()

const emit = defineEmits(['elementClicked'])
</script>
<template>
  <Button
    as="router-link"
    :to="{ name: 'home-page' }"
    text
    icon="ti ti-home text-lg"
    label="My Pastes"
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
    label="Explore"
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
    label="Stars"
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
    label="Api Keys"
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
    label="Settings"
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
    label="Account Settings"
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
    label="Admin"
    severity="contrast"
    size="small"
    fluid
    class="justify-start"
    @click="emit('elementClicked')"
  />
  <Button
    text
    icon="ti ti-logout text-lg"
    label="Sign out"
    severity="contrast"
    size="small"
    fluid
    class="justify-start"
    @click="
      () => {
        emit('elementClicked')
        confirm.require({
          message: 'Are you sure you want to log out?',
          header: 'Log out',
          accept: async () => {
            currentUserStore.logout()
          },
        })
      }
    "
  />
</template>
