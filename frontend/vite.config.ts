import { fileURLToPath, URL } from 'node:url'

import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import vueDevTools from 'vite-plugin-vue-devtools'
import tailwindcss from '@tailwindcss/vite'

// @ts-ignore
import { sync } from 'glob' // Benannter Import

console.log(
  sync('node_modules/codemirror/mode/*/*.js').map((file) => file.replace('node_modules/', '')),
)

// https://vite.dev/config/
export default defineConfig({
  plugins: [vue(), vueDevTools(), tailwindcss()],
  build: {
    outDir: '../backend/src/main/resources/static',
    emptyOutDir: true,
    minify: true,
  },
  optimizeDeps: {
    esbuildOptions: {
      loader: {
        '.keep': 'empty',
      },
    },
  },
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url)),
      'codemirror/mode': '/node_modules/codemirror/mode', // Alias für CodeMirror-Modi
    },
  },
})
