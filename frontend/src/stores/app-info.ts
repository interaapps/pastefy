import { ref } from 'vue'
import { defineStore } from 'pinia'
import { client } from '@/main.ts'

export type Config = {
  custom_logo?: string
  custom_footer?: Record<string, string>
  login_required_for_read?: boolean
  login_required_for_create?: boolean
  encryption_is_default?: boolean
  public_pastes_enabled?: boolean
  custom_name?: string
}

export const useAppInfoStore = defineStore('app-info', () => {
  const appInfo = ref<undefined | Config>(undefined)

  async function fetchAppInfo() {
    appInfo.value = (await client.get('/api/v2/app/info')).data
  }

  return { appInfo, fetchAppInfo }
})
