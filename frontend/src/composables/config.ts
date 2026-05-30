import { useStorage } from '@vueuse/core'
import type { SelectedTheme } from '@/utils/theme-logic.ts'
import type { PasteVisibility } from '@/types/paste.ts'
import type { LanguagePreference } from '@/i18n.ts'
const prefersReducedMotion = window.matchMedia('(prefers-reduced-motion: reduce)')

export function useConfig() {
  return useStorage<{
    apiKey?: string
    sideBarShown?: boolean
    theme?: SelectedTheme
    animations?: boolean
    defaultVisibility?: PasteVisibility
    language?: LanguagePreference
  }>(
    'config',
    // Default values:
    {
      apiKey: localStorage['session'] || undefined,
      sideBarShown: window.innerWidth > 1024,
      theme: 'system',
      animations: !prefersReducedMotion.matches,
      defaultVisibility: 'UNLISTED',
      language: 'browser',
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
