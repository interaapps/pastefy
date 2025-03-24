<script lang="ts" setup>
import { useRoute } from 'vue-router'
import Paste from '@/components/Paste.vue'

const route = useRoute()
</script>
<template>
  <div class="mx-auto max-w-[1200px]">
    <Paste :paste-id="route.params.paste as string" />
  </div>
</template>
