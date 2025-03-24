<script lang="ts" setup>
import Button from 'primevue/button'
import { useConfig } from '@/composables/config.ts'

const config = useConfig()

const toggleTheme = () => {
  if (config.value.theme === 'light') {
    config.value.theme = 'system'
  } else if (config.value.theme === 'dark') {
    config.value.theme = 'light'
  } else {
    config.value.theme = 'dark'
  }
}
</script>
<template>
  <div>
    <Button
      :aria-label="`Toggle theme (current: ${config.theme})`"
      @click="toggleTheme"
      :icon="`ti ti-${
        config.theme === 'light' ? 'sun' : config.theme === 'dark' ? 'moon-stars' : 'sunset'
      } text-lg`"
      rounded
      size="small"
      severity="contrast"
      text
      v-tooltip="`Theme (${config.theme})`"
    />
  </div>
</template>
