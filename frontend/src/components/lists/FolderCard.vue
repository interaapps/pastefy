<script setup lang="ts">
import type { Folder } from '@/types/folder.ts'

defineProps<{
  folder: Folder
  disabled?: boolean
}>()
</script>
<template>
  <component
    :is="disabled ? 'div' : 'router-link'"
    :to="{ name: 'folder', params: { folder: folder.id } }"
    class="group flex flex-col items-center justify-center gap-2 rounded-xl"
  >
    <div
      class="flex aspect-square w-[5rem] flex-col items-center justify-center gap-2 rounded-xl border border-neutral-300 bg-neutral-100 p-3 transition-all group-hover:bg-neutral-200 dark:border-neutral-700 dark:bg-neutral-800 dark:group-hover:bg-neutral-700"
    >
      <i class="ti ti-folder text-4xl" />
    </div>
    <span class="text-sm" v-view-transition-name="`folder-${folder.id}-title`">
      {{ folder.name }}
    </span>
  </component>
</template>
