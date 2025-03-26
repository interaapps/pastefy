<script setup lang="ts">
import { onMounted } from 'vue'
import { useConfig } from '@/composables/config.ts'
import { useRoute, useRouter } from 'vue-router'
import { client } from '@/main.ts'
import { useCurrentUserStore } from '@/stores/current-user.ts'

const currentUserStore = useCurrentUserStore()
const config = useConfig()
const route = useRoute()
const router = useRouter()

onMounted(() => {
  config.value.apiKey = route.query.key as string
  client.defaults.headers.common.Authorization = `Bearer ${config.value.apiKey}`
  currentUserStore.fetchUser()
  router.replace({ name: 'home-page' })
})
</script>
<template>
  <div>
    <h1>Logging in...</h1>
  </div>
</template>
