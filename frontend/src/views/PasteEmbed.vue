<script lang="ts" setup>
import { useRoute } from 'vue-router'
import Paste from '@/components/Paste.vue'

const route = useRoute()

const sendHeight = () => {
  window.parent.postMessage({ height: 200 }, '*')
  setTimeout(() => {
    window.parent.postMessage({ height: document.body.scrollHeight }, '*')
  }, 50)
}
</script>
<template>
  <Paste :paste-id="route.params.paste as string" as-embed @update-height="sendHeight" />
</template>
