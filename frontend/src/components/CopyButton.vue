<script setup lang="ts">
import Button from 'primevue/button'
import { useClipboard } from '@vueuse/core'
import { ref } from 'vue'

const clipboard = useClipboard()
const copied = ref(false)

const props = defineProps<{
  contents: string
  getCopyContents?: () => Promise<string>
}>()

const loading = ref(false)
const copy = async () => {
  if (props?.getCopyContents) {
    loading.value = true
    try {
      clipboard.copy(await props.getCopyContents!())
    } catch {
      console.log('error copying')
    }
  } else {
    clipboard.copy(props.contents)
  }
  copied.value = true
  setTimeout(() => {
    copied.value = false
  }, 2000)
  loading.value = false
}
</script>
<template>
  <Button
    class="bg-neutral-100 dark:bg-neutral-800"
    :icon="`ti ${copied ? 'ti-check text-green-500' : 'ti-clipboard'}`"
    size="small"
    outlined
    severity="secondary"
    @click.stop.prevent="copy"
    :loading="loading"
  />
</template>
