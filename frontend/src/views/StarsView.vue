<script setup lang="ts">
import PasteList from '@/components/lists/PasteList.vue'
import { useTitle } from '@vueuse/core'

useTitle(`Stars | Pastefy`)
</script>

<template>
  <main class="mx-auto w-full max-w-[1200px]">
    <h1 class="mb-7 text-3xl font-bold">Stars</h1>

    <PasteList route="/api/v2/user/starred-pastes" :params="{ page_limit: 7 }" />
  </main>
</template>
