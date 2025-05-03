import './assets/main.css'

import {
  createApp,
  defineCustomElement,
  type App as VueApp,
  h,
  ref,
  shallowRef,
  defineComponent,
} from 'vue'
import * as vueFunctions from 'vue'
import { createPinia } from 'pinia'
import theme from '@/theme.ts'

import App from './App.vue'
import router from './router'

import mitt, { type Emitter } from 'mitt'

import PrimeVue from 'primevue/config'
import Aura from '@primeuix/themes/aura'
import { definePreset } from '@primeuix/themes'
import axios from 'axios'
import ToastService from 'primevue/toastservice'
import Tooltip from 'primevue/tooltip'
import ConfirmationService from 'primevue/confirmationservice'

// eslint-disable-next-line
// @ts-ignore
import vue3Shortkey from 'vue3-shortkey'

import { InstallCodeMirror } from 'codemirror-editor-vue3'
import { useAppInfoStore } from '@/stores/app-info.ts'
import { useCurrentUserStore } from '@/stores/current-user.ts'
import { useConfig } from '@/composables/config.ts'
import Highlighted from '@/components/Highlighted.vue'
import { registerEventHandlers } from '@/utils/theme-logic.ts'
import type { Router } from 'vue-router'
import { createPlugin, plugins } from '@/plugins.ts'
import { useComponentInjectionStore } from '@/stores/component-injections.ts'

declare global {
  interface Window {
    pastefy: {
      app: VueApp
      router: Router
      appInfoStore: typeof appInfoStore
      config: typeof config
      eventBus: Emitter<Events>
      createPlugin: typeof createPlugin
      client: typeof client
      componentInjections: typeof componentInjections
      vueFunctions: typeof vueFunctions
    }
    registerPastefyPlugin: { config: unknown; entrypoint: string }[]
  }
}

export const client = axios.create({
  baseURL: (import.meta.env.VITE_APP_BASE_URL as string) || undefined,
})
export const app = createApp(App)

app.use(createPinia())
app.use(router)
app.use(InstallCodeMirror)

const preset = definePreset(Aura, theme)

app.use(PrimeVue, {
  theme: {
    preset,
    options: {
      darkModeSelector: '.dark',
      cssLayer: {
        name: 'primevue',
        order: 'theme, base, primevue',
      },
    },
  },
})
app.use(ToastService)
app.directive('tooltip', Tooltip)
app.use(ConfirmationService)
app.use(vue3Shortkey, { prevent: ['input', 'textarea'] })

customElements.define(
  'pastefy-highlighted',
  defineCustomElement(Highlighted, {
    shadowRoot: false,
  }),
)

app.mount('#app')

const currentUserStore = useCurrentUserStore()
const config = useConfig()

client.interceptors.response.use((response) => {
  if (typeof response.data === 'string') {
    try {
      response.data = JSON.parse(response.data)
    } catch (e) {
      console.log(e)
    }
  }
  return response
})

client.defaults.headers.common = { Accept: `application/json` }
if (config.value?.apiKey) {
  client.defaults.headers.common.Authorization = `Bearer ${config.value.apiKey}`
}
currentUserStore.fetchUser()

const appInfoStore = useAppInfoStore()
const componentInjections = useComponentInjectionStore()
appInfoStore.fetchAppInfo()

registerEventHandlers()

export type Events = {
  pasteCreate: string
}

export const eventBus = mitt<Events>()

window.pastefy = {
  app,
  router,
  appInfoStore,
  config,
  eventBus,
  createPlugin,
  client,
  componentInjections,
  vueFunctions,
}
;(async () => {
  if ('registerPastefyPlugin' in window && Array.isArray(window.registerPastefyPlugin)) {
    for (const { entrypoint } of window.registerPastefyPlugin) {
      await import(entrypoint)
    }
  }

  plugins.forEach((plugin) => {
    plugin.init?.()
  })
})()
