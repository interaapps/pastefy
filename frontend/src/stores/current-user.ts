import { ref } from 'vue'
import { defineStore } from 'pinia'
import type { User } from '@/types/user.ts'
import { client } from '@/main.ts'
import { useConfig } from '@/composables/config.ts'
import { useUserFoldersStore } from '@/stores/user-folders.ts'

export const useCurrentUserStore = defineStore('current-user', () => {
  const user = ref<undefined | User>(undefined)
  const authTypes = ref<string[]>([])
  const userLoading = ref(false)

  const config = useConfig()

  const userFoldersStore = useUserFoldersStore()

  function logout() {
    user.value = undefined
    config.value.apiKey = undefined
    client.defaults.headers.common.Authorization = undefined
  }

  function setUser(newUser: User) {
    user.value = newUser
  }

  async function fetchUser() {
    userLoading.value = true
    try {
      const user = (await client.get('/api/v2/user')).data
      authTypes.value = user.auth_types
      if (user.logged_in) {
        setUser(user)
      }
    } catch {}
    userLoading.value = false

    userFoldersStore.fetchFolders()
  }

  return { user, logout, setUser, userLoading, fetchUser, authTypes }
})
