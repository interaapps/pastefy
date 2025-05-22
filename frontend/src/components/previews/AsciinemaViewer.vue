<script setup lang="ts">
import { computed, onMounted } from 'vue'

import 'asciinema-player/dist/bundle/asciinema-player.css'

// @ts-ignore
import * as AsciinemaPlayer from 'asciinema-player'
const props = defineProps<{
  cast: string
}>()

onMounted(() => {
  const text = props.cast
  const base64 = btoa(
    encodeURIComponent(text).replace(/%([0-9A-F]{2})/g, (_, p1) =>
      String.fromCharCode(parseInt(p1, 16)),
    ),
  )
  const dataUrl = `data:text/plain;base64,${base64}`

  AsciinemaPlayer.create(dataUrl, document.getElementById('demo'))
  setTimeout(() => {
    document.querySelector<HTMLButtonElement>('.ap-play-button')?.click()
  }, 50)
  console.log('AsciiCinema')
})
</script>
<template>
  <div class="overflow-hidden rounded-xl text-sm">
    <div>
      <div id="demo" />
    </div>
  </div>
</template>

<style>
.ap-play-button .ap-icon {
  margin: auto;
}
</style>
