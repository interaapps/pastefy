import { type Component, shallowRef } from 'vue'
import { defineStore } from 'pinia'

export const useComponentInjectionStore = defineStore('component-injections', () => {
  const injections = shallowRef<Record<string, Component[]>>({})

  function registerInjection(name: string, component: Component) {
    if (!injections.value[name]) {
      injections.value[name] = []
    }
    injections.value[name] = [...injections.value[name], component]
  }

  return { injections, registerInjection }
})
