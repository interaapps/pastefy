<script setup lang="ts">
import Button from 'primevue/button'
import { useCurrentUserStore } from '@/stores/current-user.ts'

const currentUserStore = useCurrentUserStore()
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
  />
  <Button
    text
    icon="ti ti-logout text-lg"
    label="Sign out"
    severity="contrast"
    size="small"
    fluid
    class="justify-start"
    @click="currentUserStore.logout()"
  />
</template>
