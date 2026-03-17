<script setup lang="ts">
import Button from 'primevue/button'
import { useAsyncState } from '@vueuse/core'
import { client } from '@/main.ts'
import type { Paste } from '@/types/paste.ts'
import { useCurrentUserStore } from '@/stores/current-user.ts'

const props = defineProps<{
  paste: Paste
  pasteId: string
  rounded?: boolean
  text?: boolean
  severity?: string
  size?: 'small' | 'large'
}>()

const currentUser = useCurrentUserStore()

const { isLoading: starLoading, execute: toggleStar } = useAsyncState(
  async () => {
    if (props.paste?.starred) {
      await client.delete(`/api/v2/paste/${props.pasteId}/star`)
      props.paste.starred = false
    } else {
      await client.post(`/api/v2/paste/${props.pasteId}/star`)
      props.paste.starred = true
    }
  },
  undefined,
  { immediate: false },
)
</script>

<template>
  <Button
    v-if="currentUser.user?.logged_in"
    @click="() => toggleStar()"
    :severity="severity || 'contrast'"
    rounded
    text
    :icon="`ti ti-star text-xl ${paste.starred ? 'text-yellow-500' : ''}`"
    v-tooltip="{ value: 'Star', showDelay: 500 }"
    :loading="starLoading"
    aria-label="Star"
  />
</template>
