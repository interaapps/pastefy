import { ref } from 'vue'
import { defineStore } from 'pinia'

export const useAppStore = defineStore('app-store', () => {
  const searchShown = ref(false)
  const searchShownEndpoints = ref({
    myPastes: true,
    publicPastes: true,
  })

  return { searchShown, searchShownEndpoints }
})
