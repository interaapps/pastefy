import { useConfig } from '@/composables/config.ts'
import { watch } from 'vue'
import { useFavicon } from '@vueuse/core'

export type SelectedTheme = 'light' | 'dark' | 'system'

const config = useConfig()

export function toggleTheme() {
  let darkClass = false
  if (config.value.theme === 'system') {
    darkClass = window.matchMedia('(prefers-color-scheme: dark)').matches
  } else if (config.value.theme === 'dark') {
    darkClass = true
  } else {
    darkClass = false
  }
  document.documentElement.classList.toggle('dark', darkClass)

  useFavicon(
    window.matchMedia('(prefers-color-scheme: dark)').matches
      ? '/favicon-light.ico'
      : '/favicon.ico',
  )
}

export function registerEventHandlers() {
  toggleTheme()
  watch(config, toggleTheme)
  window.matchMedia('(prefers-color-scheme: dark)').addEventListener('change', () => {
    toggleTheme()
  })
}
