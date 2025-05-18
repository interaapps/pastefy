<script setup lang="ts">
import PasteList from '@/components/lists/PasteList.vue'

const search = defineModel<string>('search')
</script>

<template>
  <div>
    <PasteList route="/api/v2/public-pastes/latest" :params="{ search }" />
  </div>
</template>
