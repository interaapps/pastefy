<script lang="ts" setup>
import { useRoute } from 'vue-router'
import Paste from '@/components/Paste.vue'
import ComponentInjection from '@/components/ComponentInjection.vue'
import { onMounted } from 'vue'

const route = useRoute()

onMounted(async () => {
  await new Promise((resolve) => setTimeout(resolve, 2000))
})
</script>
<template>
  <div class="mx-auto max-w-[1200px]">
    <ComponentInjection type="paste-view-top" />
    <Paste :paste-id="route.params.paste as string" />
    <ComponentInjection type="paste-view-bottom" />
  </div>
</template>
