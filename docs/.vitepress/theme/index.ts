// https://vitepress.dev/guide/custom-theme
import { h } from 'vue'
import type { Theme } from 'vitepress'
import DefaultTheme from 'vitepress/theme'
import './style.css'
// @ts-ignore
import {theme, useOpenapi} from 'vitepress-openapi/client'
import 'vitepress-openapi/dist/style.css'

import spec from '../../openapi.json'


export default {
  extends: DefaultTheme,
  Layout: () => {
    return h(DefaultTheme.Layout, null, {
      // https://vitepress.dev/guide/extending-default-theme#layout-slots
    })
  },
  enhanceApp({ app, router, siteData }) {
    useOpenapi({
      spec,
    })

    theme.enhanceApp({ app })

    // ...
  }
} satisfies Theme
