<script setup lang="ts">
import type { PasteCommentMarker } from '@/types/paste-comment.ts'

defineProps<{
  marker: PasteCommentMarker
}>()
</script>

<template>
  <span class="flex items-center pl-1">
    <span
      v-for="profile of marker.profiles"
      :key="profile.id"
      class="-ml-2.5 flex h-4 w-4 items-center justify-center overflow-hidden rounded-full border border-white bg-neutral-300 text-[0.5rem] font-bold text-neutral-700 first:ml-0 dark:border-neutral-800 dark:bg-neutral-600 dark:text-neutral-100"
      :title="profile.display_name || profile.name"
    >
      <img v-if="profile.avatar" :src="profile.avatar" alt="" class="h-full w-full object-cover" />
      <span v-else>{{ (profile.display_name || profile.name || '?').slice(0, 1) }}</span>
    </span>
    <span
      v-if="marker.additional_profiles"
      class="-ml-2.5 flex h-4 min-w-4 items-center justify-center rounded-full border border-white bg-neutral-300 px-0.5 text-[0.5rem] font-bold text-neutral-700 dark:border-neutral-800 dark:bg-neutral-600 dark:text-neutral-100"
    >
      +{{ marker.additional_profiles }}
    </span>
  </span>
</template>
