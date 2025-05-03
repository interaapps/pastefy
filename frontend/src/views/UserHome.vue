<script setup lang="ts">
import PasteList from '@/components/lists/PasteList.vue'
import FolderList from '@/components/lists/FolderList.vue'
import { useTitle } from '@vueuse/core'
import ComponentInjection from '@/components/ComponentInjection.vue'
import { eventBus } from '@/main.ts'
useTitle(`Home | Pastefy`)
</script>

<template>
  <div class="flex flex-col gap-14">
    <ComponentInjection type="user-home-top" />
    <div>
      <h2 class="mb-4 text-2xl font-bold">Folders</h2>

      <FolderList route="/api/v2/user/folders" />
    </div>
    <ComponentInjection type="user-home-after-folders" />
    <div>
      <h2 class="mb-4 text-2xl font-bold">Pastes</h2>

      <PasteList
        @loaded="eventBus.emit('pageLoaded', 'home')"
        route="/api/v2/user/pastes"
        :params="{ page_limit: 8, hide_children: 'true' }"
      />
    </div>
    <ComponentInjection type="user-home-after-pastes" />
  </div>
</template>
