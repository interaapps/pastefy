import { ref, watch } from 'vue'
import { defineStore } from 'pinia'
import type { MultiPastePart, Paste, PasteType, PasteVisibility } from '@/types/paste.ts'
import CryptoJS from 'crypto-js'
import { useAppInfoStore } from '@/stores/app-info.ts'
import { useConfig } from '@/composables/config.ts'
import router from '@/router'

export const useCurrentPasteStore = defineStore('current-paste', () => {
  const title = ref('')
  const contents = ref('')
  const visibility = ref<PasteVisibility>('UNLISTED')

  const currentId = ref<string | undefined>(undefined)
  const expiresAt = ref<string | undefined>(undefined)
  const expiresAtEnabled = ref(false)
  const type = ref<PasteType>('PASTE')
  const multiPastes = ref<MultiPastePart[]>([])
  const currentMultiPasteIndex = ref<undefined | number>(undefined)
  const encrypted = ref(false)
  const password = ref('')
  const tags = ref<string[]>([])
  const forkedFrom = ref<string | undefined>(undefined)
  const folder = ref<string | undefined>(undefined)

  const appInfo = useAppInfoStore()
  const config = useConfig()

  function forkFrom(paste: Paste) {
    title.value = paste.title
    contents.value = paste.content
    forkedFrom.value = paste.id

    if (paste.type === 'MULTI_PASTE') {
      multiPastes.value = JSON.parse(paste.content)
      type.value = 'MULTI_PASTE'
      selectMultiPart(0)
    }

    if (window.innerWidth < 768) {
      router.push({ name: 'home' })
    } else {
      config.value.sideBarShown = true
    }
  }

  function editFrom(paste: Paste, pw?: string) {
    forkFrom(paste)
    forkedFrom.value = undefined
    password.value = pw || ''

    folder.value = paste.folder
    encrypted.value = paste.encrypted
    visibility.value = paste.visibility
    currentId.value = paste.id
    tags.value = paste.tags || []
    expiresAt.value = paste.expire_at
    if (expiresAt.value) expiresAtEnabled.value = true
  }

  function addMultiPart() {
    const i =
      multiPastes.value.push({
        name: '',
        contents: '',
      }) - 1
    if (i === 0) {
      multiPastes.value[0].name = title.value
      multiPastes.value[0].contents = contents.value
      title.value = ''
    }
    selectMultiPart(i)
    type.value = 'MULTI_PASTE'
  }
  function selectMultiPart(index: number) {
    currentMultiPasteIndex.value = index
    const part = multiPastes.value[index]
    if (part) contents.value = part.contents
  }

  function removeMultiPart(index: number) {
    if (multiPastes.value.length === 1) {
      type.value = 'PASTE'
      currentMultiPasteIndex.value = undefined
      contents.value = multiPastes.value[0].contents
      title.value = multiPastes.value[0].name
    }
    multiPastes.value.splice(index, 1)
    selectMultiPart(0)
  }

  function getCurrentMultiPart() {
    return multiPastes.value[currentMultiPasteIndex.value!]
  }

  function toPaste(): Paste {
    const paste: Paste = {
      content: contents.value,
      title: title.value,
      encrypted: encrypted.value,
      visibility: visibility.value,
      type: type.value,
      tags: tags.value,
      forked_from: forkedFrom.value,
      folder: folder.value,
      expire_at: (expiresAtEnabled.value && expiresAt.value) || undefined,
    }

    if (type.value === 'MULTI_PASTE') {
      paste.content = JSON.stringify(multiPastes.value)
    }

    if (password.value) paste.encrypted = true

    if (!password.value && paste.encrypted) {
      password.value =
        Math.random().toString(36).substring(3) + Math.random().toString(36).substring(3)
    }

    if (paste.encrypted) {
      paste.title = CryptoJS.AES.encrypt(paste.title, password.value).toString()
      paste.content = CryptoJS.AES.encrypt(paste.content, password.value).toString()
    }

    return paste
  }

  watch(contents, (newContents) => {
    if (currentMultiPasteIndex.value !== undefined) {
      multiPastes.value[currentMultiPasteIndex.value].contents = newContents
    }
  })

  function clear() {
    title.value = ''
    contents.value = ''
    multiPastes.value = []
    currentMultiPasteIndex.value = undefined
    encrypted.value = appInfo.appInfo?.encryption_is_default || false
    password.value = ''
    visibility.value = 'UNLISTED'
    currentId.value = undefined
    tags.value = []
    forkedFrom.value = undefined
    folder.value = undefined
    expiresAt.value = undefined
  }

  return {
    title,
    contents,
    forkFrom,
    multiPastes,
    currentMultiPasteIndex,
    addMultiPart,
    selectMultiPart,
    type,
    removeMultiPart,
    getCurrentMultiPart,
    toPaste,
    clear,
    encrypted,
    password,
    visibility,
    editFrom,
    currentId,
    folder,
    forkedFrom,
    expiresAt,
    expiresAtEnabled,
    tags,
  }
})
