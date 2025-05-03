import { useStorage } from '@vueuse/core'
import type { SelectedTheme } from '@/utils/theme-logic.ts'
import type { PasteVisibility } from '@/types/paste.ts'
const prefersReducedMotion = window.matchMedia('(prefers-reduced-motion: reduce)')

export function useConfig() {
  return useStorage<{
    apiKey?: string
    sideBarShown?: boolean
    theme?: SelectedTheme
    animations?: boolean
    defaultVisibility?: PasteVisibility
  }>(
    'config',
    // Default values:
    {
      apiKey: localStorage['session'] || undefined,
      sideBarShown: window.innerWidth > 1024,
      theme: 'system',
      animations: prefersReducedMotion.matches,
      defaultVisibility: 'UNLISTED',
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
