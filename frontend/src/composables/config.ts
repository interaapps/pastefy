import { useStorage } from '@vueuse/core'
import type { SelectedTheme } from '@/utils/theme-logic.ts'

export function useConfig() {
  return useStorage<{
    apiKey?: string
    sideBarShown?: boolean
    theme?: SelectedTheme
  }>(
    'config',
    // Default values:
    {
      apiKey: localStorage['session'] || undefined,
      sideBarShown: window.innerWidth > 1024,
      theme: 'system',
    },
    localStorage,
    {
      mergeDefaults: true,
    },
  )
}

if (localStorage['session']) localStorage['session'] = undefined

window.onresize = () => {
  const config = useConfig()
  config.value.sideBarShown = window.innerWidth > 1024
}
