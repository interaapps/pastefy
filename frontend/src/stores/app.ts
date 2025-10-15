import { ref } from 'vue'
import { defineStore } from 'pinia'

export const useAppStore = defineStore('app-store', () => {
  const searchShown = ref(false)
  const codeMirrorAvailable = ref(false)
  const searchShownEndpoints = ref({
    myPastes: true,
    publicPastes: true,
  })

  const settingsModalShown = ref(false)

  return { searchShown, searchShownEndpoints, codeMirrorAvailable, settingsModalShown }
})
