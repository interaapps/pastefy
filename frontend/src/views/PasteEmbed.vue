<script lang="ts" setup>
import { useRoute } from 'vue-router'
import Paste from '@/components/Paste.vue'
import ComponentInjection from '@/components/ComponentInjection.vue'
import { onMounted } from 'vue'
import { useUrlSearchParams } from '@vueuse/core'

const route = useRoute()

const settings = useUrlSearchParams<{
  hideTitle?: boolean
  hideActions?: boolean
  addPadding?: boolean
  hideEmbedBottom?: boolean
}>('history')

const sendHeight = () => {
  window.parent.postMessage({ height: 200 }, '*')
  setTimeout(() => {
    window.parent.postMessage({ height: document.body.scrollHeight }, '*')
  }, 50)
}

onMounted(() => {})
</script>
<template>
  <div :class="settings.addPadding ? 'p-4' : ''">
    <ComponentInjection type="paste-embed-top" />
    <Paste
      :paste-id="route.params.paste as string"
      as-embed
      @update-height="sendHeight"
      :hide-actions="settings.hideActions"
      :hide-title="settings.hideTitle"
      :hide-embed-bottom="settings.hideEmbedBottom"
    />
    <ComponentInjection type="paste-embed-bottom" />
  </div>
</template>
