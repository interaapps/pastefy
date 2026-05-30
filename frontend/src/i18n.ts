import i18next from 'i18next'
import en from '@/locales/en.json'

import deSVG from 'ia-flag-icons/states/DE.svg'
import ukSVG from 'ia-flag-icons/states/UK.svg'
import ptSVG from 'ia-flag-icons/states/PT.svg'
import frSVG from 'ia-flag-icons/states/FR.svg'
import idSVG from 'ia-flag-icons/states/ID.svg'
import uaSVG from 'ia-flag-icons/states/UA.svg'
import ruSVG from 'ia-flag-icons/states/RU.svg'
import esSVG from 'ia-flag-icons/states/ES.svg'
import vnSVG from 'ia-flag-icons/states/VN.svg'
import thSVG from 'ia-flag-icons/states/TH.svg'
import cnSVG from 'ia-flag-icons/states/CN.svg'
import twSVG from 'ia-flag-icons/states/TW.svg'
import jpSVG from 'ia-flag-icons/states/JP.svg'
import saSVG from 'ia-flag-icons/states/SA.svg'
import ilSVG from 'ia-flag-icons/states/IL.svg'
import hrSVG from 'ia-flag-icons/states/HR.svg'
import krdSVG from 'ia-flag-icons/states/KRD.svg'

export const supportedLocales = [
  'en',
  'de',
  'pt',
  'es',
  'fr',
  'vi',
  'id',
  'th',
  'zh-CN',
  'zh-TW',
  'ru',
  'uk',
  'ar',
  'he',
  'ja',
  'kmr',
  'hr',
] as const

export type SupportedLocale = (typeof supportedLocales)[number]
export type LanguagePreference = SupportedLocale | 'browser'

export const languageOptions: Array<{ value: LanguagePreference; label: string; icon: string }> = [
  { value: 'browser', label: 'Browser language', icon: ukSVG },
  { value: 'en', label: 'English', icon: ukSVG },
  { value: 'de', label: 'Deutsch', icon: deSVG },
  { value: 'pt', label: 'Português', icon: ptSVG },
  { value: 'es', label: 'Español', icon: esSVG },
  { value: 'fr', label: 'Français', icon: frSVG },
  { value: 'vi', label: 'Tiếng Việt', icon: vnSVG },
  { value: 'id', label: 'Bahasa Indonesia', icon: idSVG },
  { value: 'th', label: 'ไทย', icon: thSVG },
  { value: 'zh-CN', label: '简体中文', icon: cnSVG },
  { value: 'zh-TW', label: '繁體中文', icon: twSVG },
  { value: 'ru', label: 'Русский', icon: ruSVG },
  { value: 'uk', label: 'Українська', icon: uaSVG },
  { value: 'ar', label: 'العربية', icon: saSVG },
  { value: 'kmr', label: 'Kurmancî', icon: krdSVG },
  { value: 'he', label: 'עברית', icon: ilSVG },
  { value: 'ja', label: '日本語', icon: jpSVG },
  { value: 'hr', label: 'Hrvatski', icon: hrSVG },
]

const localeAliases: Record<string, SupportedLocale> = {
  en: 'en',
  de: 'de',
  pt: 'pt',
  es: 'es',
  fr: 'fr',
  vi: 'vi',
  id: 'id',
  th: 'th',
  zh: 'zh-CN',
  'zh-cn': 'zh-CN',
  'zh-hans': 'zh-CN',
  'zh-sg': 'zh-CN',
  'zh-tw': 'zh-TW',
  'zh-hant': 'zh-TW',
  'zh-hk': 'zh-TW',
  ru: 'ru',
  uk: 'uk',
  ar: 'ar',
  he: 'he',
  iw: 'he',
  ja: 'ja',
  hr: 'hr',
  kmr: 'kmr',
}

const localeLoaders: Partial<Record<SupportedLocale, () => Promise<{ default: any }>>> = {
  de: () => import('@/locales/de.json'),
  pt: () => import('@/locales/pt.json'),
  es: () => import('@/locales/es.json'),
  fr: () => import('@/locales/fr.json'),
  vi: () => import('@/locales/vi.json'),
  id: () => import('@/locales/id.json'),
  th: () => import('@/locales/th.json'),
  'zh-CN': () => import('@/locales/zh-CN.json'),
  'zh-TW': () => import('@/locales/zh-TW.json'),
  ru: () => import('@/locales/ru.json'),
  uk: () => import('@/locales/uk.json'),
  ar: () => import('@/locales/ar.json'),
  he: () => import('@/locales/he.json'),
  ja: () => import('@/locales/ja.json'),
  kmr: () => import('@/locales/kmr.json'),
  hr: () => import('@/locales/hr.json'),
}

const loadedLocales = new Set<SupportedLocale>(['en'])

const loadLocale = async (locale: SupportedLocale) => {
  if (loadedLocales.has(locale)) {
    return
  }

  const loader = localeLoaders[locale]

  if (!loader) {
    return
  }

  const module = await loader()

  i18next.addResourceBundle(locale, 'translation', module.default, true, true)

  loadedLocales.add(locale)
}

export const resolveBrowserLocale = (): SupportedLocale => {
  const browserLanguages = typeof navigator === 'undefined' ? [] : navigator.languages

  for (const browserLanguage of browserLanguages) {
    const normalized = browserLanguage.toLowerCase()

    if (localeAliases[normalized]) {
      return localeAliases[normalized]
    }

    const baseLanguage = normalized.split('-')[0]

    if (localeAliases[baseLanguage]) {
      return localeAliases[baseLanguage]
    }
  }

  return 'en'
}

export const resolveLanguagePreference = (preference?: LanguagePreference): SupportedLocale =>
  !preference || preference === 'browser' ? resolveBrowserLocale() : preference

const initialPreference = (() => {
  if (typeof localStorage === 'undefined') {
    return 'browser'
  }

  try {
    const parsed = JSON.parse(localStorage.getItem('config') || '{}') as {
      language?: LanguagePreference
    }

    return parsed.language || 'browser'
  } catch {
    return 'browser'
  }
})()

export const applyDocumentLanguage = (locale = i18next.resolvedLanguage || i18next.language) => {
  const resolvedLocale = localeAliases[locale.toLowerCase()] || 'en'

  document.documentElement.lang = resolvedLocale
  document.documentElement.dir = resolvedLocale === 'ar' || resolvedLocale === 'he' ? 'rtl' : 'ltr'
}

const initialLocale = resolveLanguagePreference(initialPreference)

export const i18nReady = i18next
  .init({
    lng: 'en',
    fallbackLng: 'en',
    resources: {
      en: {
        translation: en,
      },
    },
    interpolation: {
      escapeValue: false,
    },
  })
  .then(async () => {
    if (initialLocale !== 'en') {
      await loadLocale(initialLocale)
      await i18next.changeLanguage(initialLocale)
    }

    applyDocumentLanguage()
  })

export const changeLanguage = async (preference: LanguagePreference) => {
  const locale = resolveLanguagePreference(preference)

  if (locale !== 'en') {
    await loadLocale(locale)
  }

  await i18next.changeLanguage(locale)

  applyDocumentLanguage(locale)
}

i18next.on('languageChanged', () => applyDocumentLanguage())

export default i18next
