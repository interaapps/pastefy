<script setup lang="ts">
import InputText from 'primevue/inputtext'
import TreeSelect from 'primevue/treeselect'
import Checkbox from 'primevue/checkbox'
import DatePicker from 'primevue/datepicker'
import Select from 'primevue/select'
import Button from 'primevue/button'
import ButtonGroup from 'primevue/buttongroup'
import { computed, ref, watch } from 'vue'

import Codemirror from 'codemirror-editor-vue3'

import 'codemirror/addon/display/placeholder.js'
import 'codemirror/mode/javascript/javascript.js'
import 'codemirror/mode/markdown/markdown.js'
import 'codemirror/addon/display/placeholder.js'
import 'codemirror/addon/edit/closebrackets.js'
import 'codemirror/addon/edit/closetag.js'
import 'codemirror/addon/edit/continuelist.js'
import 'codemirror/addon/edit/matchbrackets.js'
import 'codemirror/addon/edit/matchtags.js'
import 'codemirror/mode/meta.js'
import 'codemirror/addon/mode/loadmode.js'

import 'codemirror/addon/hint/show-hint.js'
import 'codemirror/addon/hint/javascript-hint.js'
import 'codemirror/addon/hint/html-hint.js'
import 'codemirror/addon/hint/sql-hint.js'
import 'codemirror/addon/hint/xml-hint.js'
import 'codemirror/addon/hint/css-hint.js'

import CodeMirror from 'codemirror'
import emmet from '@emmetio/codemirror-plugin'

import codemirrorLanguageImports from '@/utils/codemirror-language-imports.ts'
import { useCurrentPasteStore } from '@/stores/current-paste.ts'
import { useAsyncState, useDebounceFn } from '@vueuse/core'
import { client } from '@/main.ts'
import type { Paste } from '@/types/paste.ts'
import router from '@/router'
import PasteVisibilityIcon from '@/components/PasteVisibilityIcon.vue'
import { getIconByFileName } from '@/utils/lang-information.ts'
import PastePreview from '@/components/PastePreview.vue'
import { useAppInfoStore } from '@/stores/app-info.ts'
import { useUserFoldersStore } from '@/stores/user-folders.ts'
import type { Folder } from '@/types/folder.ts'
import { useCurrentUserStore } from '@/stores/current-user.ts'
import { estimateTitle } from '@/utils/estimate-title.ts'
import { useConfig } from '@/composables/config.ts'

const currentPaste = useCurrentPasteStore()
const currentUserStore = useCurrentUserStore()

const config = useConfig()

emmet(CodeMirror)

const cmOptions = ref({
  mode: 'javascript',
  theme: 'pastefy',
  autofocus: true,
  autoCloseBrackets: true,
  autoCloseTags: true,
  matchBrackets: true,
  extraKeys: {
    Tab: 'emmetExpandAbbreviation',
    Esc: 'emmetResetAbbreviation',
    Enter: 'emmetInsertLineBreak',
  },
} as CodeMirror.EditorConfiguration)
const isFullscreen = ref(false)

const currentTitle = computed(() =>
  currentPaste.type === 'MULTI_PASTE'
    ? currentPaste.getCurrentMultiPart().name
    : currentPaste.title,
)

watch(
  currentTitle,
  useDebounceFn(async () => {
    const ext = currentTitle.value.split('.').pop()
    if (ext) {
      const cmExt = CodeMirror.findModeByExtension(ext)
      if (cmExt) {
        await codemirrorLanguageImports[cmExt.mode]?.()
      }
      cmOptions.value.mode = cmExt?.mode
    }
  }, 200),
)

const showSettings = ref(false)

const { execute: submitPaste, isLoading: isPasting } = useAsyncState(
  async () => {
    if (currentPaste.currentId) {
      await client.put(`/api/v2/paste/${currentPaste.currentId}`, currentPaste.toPaste())
      currentPaste.clear()
      router.go(0)
      return
    }

    const passwordBefore = currentPaste.password
    const paste = (await client.post('/api/v2/paste', currentPaste.toPaste())).data.paste as Paste

    router.push({
      name: 'paste',
      params: {
        paste: paste.id,
      },
      hash: passwordBefore
        ? undefined
        : currentPaste.password
          ? `#${currentPaste.password}`
          : undefined,
    })
    currentPaste.clear()
  },
  undefined,
  {
    immediate: false,
  },
)
const appInfo = useAppInfoStore()
const userFoldersStore = useUserFoldersStore()

type FolderEntry = {
  key: string
  label: string
  data: string
  icon: string
  children: FolderEntry[]
}
const folderToEntry = (folder: Folder): FolderEntry => ({
  key: folder.id,
  label: folder.name,
  data: folder.id,
  icon: 'ti ti-folder',
  children: folder.children.map(folderToEntry) as FolderEntry[],
})

const userFolders = computed(() => userFoldersStore.folders?.map(folderToEntry) || [])

const pasteEvent = async (e: unknown) => {
  if (e instanceof ClipboardEvent) {
    const data = e.clipboardData?.getData('text')
    if (data && !(e.target as HTMLTextAreaElement)?.value && currentPaste.title === '') {
      const title = estimateTitle(data.trim())

      if (!title) return

      if (currentPaste.type === 'MULTI_PASTE') {
        currentPaste.getCurrentMultiPart().name = title
        return
      }
      currentPaste.title = title
    }
  }
}
</script>
<template>
  <div v-if="appInfo.appInfo?.login_required_for_create">
    <span class="block text-center text-sm text-neutral-500">
      Login is required to create pastes
    </span>
  </div>
  <form @submit.prevent="() => submitPaste()" v-else class="flex w-full flex-col gap-2">
    <InputText
      size="small"
      v-model="currentPaste.title"
      fluid
      :placeholder="currentPaste.type === 'MULTI_PASTE' ? 'title' : 'file.js'"
      variant="outlined"
    />

    <div class="mt-4 mb-3" v-if="currentPaste.type === 'MULTI_PASTE'">
      <div class="mb-2 flex w-full items-center justify-between gap-4">
        <label>FILES</label>
        <Button
          @click="currentPaste.addMultiPart()"
          size="small"
          icon="ti ti-plus text-lg"
          text
          severity="contrast"
          class="p-0"
        />
      </div>

      <div class="flex gap-2" :class="isFullscreen ? 'flex-col' : 'overflow-auto'">
        <div
          v-for="(part, index) of currentPaste.multiPastes"
          class="flex min-w-[6rem] items-center justify-between gap-1 rounded-md border border-neutral-400 p-2 py-1.5 text-sm"
          :class="index === currentPaste.currentMultiPasteIndex ? 'border-primary-500' : ''"
          @click="currentPaste.selectMultiPart(index)"
          :key="index"
        >
          <i :class="`ti ti-${getIconByFileName(part.name)}`" />
          <input
            :readonly="index !== currentPaste.currentMultiPasteIndex"
            v-model="part.name"
            placeholder="filename"
            class="h-full w-full outline-none"
          />
          <Button
            @click="currentPaste.removeMultiPart(index)"
            size="small"
            icon="ti ti-x text-lg"
            severity="contrast"
            text
            class="p-0"
          />
        </div>
      </div>
    </div>

    <Teleport :disabled="!isFullscreen" to="body">
      <div
        :class="`relative ${isFullscreen ? `!fixed top-0 ${config.sideBarShown ? 'left-[340px] w-[calc(100%-340px)]' : 'left-[0px] w-full'} h-full` : ''} ${isFullscreen && (cmOptions.mode === 'markdown' || currentTitle?.endsWith('.csv')) ? 'grid grid-cols-2' : ''}`"
      >
        <Codemirror
          class="w-full"
          :class="isFullscreen ? 'rounded-none' : 'rounded-md border border-neutral-400'"
          v-model:value="currentPaste.contents"
          :options="cmOptions"
          placeholder="Paste here"
          :height="isFullscreen ? '100%' : '200px'"
          @paste="pasteEvent"
        />
        <PastePreview
          v-if="isFullscreen && cmOptions.mode === 'markdown'"
          class="w-full overflow-auto border-l border-neutral-300 bg-neutral-100 dark:border-neutral-700 dark:bg-neutral-800"
          :contents="currentPaste.contents"
          file-name="test.md"
          type="markdown"
        />
        <PastePreview
          v-if="isFullscreen && currentTitle?.endsWith('.csv')"
          class="w-full overflow-auto border-l border-neutral-300 bg-neutral-100 dark:border-neutral-700 dark:bg-neutral-800"
          :contents="currentPaste.contents"
          file-name="test.csv"
          type="csv"
        />
        <ButtonGroup
          class="absolute"
          :class="isFullscreen ? 'top-[1rem] right-[1rem]' : 'top-[0.1rem] right-[0.1rem]'"
        >
          <Button
            icon="ti ti-plus"
            v-if="currentPaste.type !== 'MULTI_PASTE'"
            class="bg-neutral-300 p-0 dark:bg-neutral-800"
            severity="contrast"
            text
            @click="currentPaste.addMultiPart()"
          />
          <Button
            :icon="`ti ${isFullscreen ? 'ti-minimize' : 'ti-maximize'}`"
            class="hidden bg-neutral-300 p-0 md:flex dark:bg-neutral-800"
            severity="contrast"
            text
            :size="isFullscreen ? 'large' : ''"
            @click="isFullscreen = !isFullscreen"
          />
        </ButtonGroup>
      </div>
    </Teleport>

    <div class="flex flex-col gap-3" v-if="showSettings">
      <Select
        :options="[
          'UNLISTED',
          ...(currentPaste.password || currentPaste.encrypted ? [] : ['PUBLIC']),
          ...(currentUserStore.user ? ['PRIVATE'] : []),
        ]"
        v-model="currentPaste.visibility"
        size="small"
      >
        <template #value="{ value: option }">
          <div class="flex items-center gap-1">
            <PasteVisibilityIcon :visibility="option" class="text-lg" />
            <span>{{ option }}</span>
          </div>
        </template>
        <template #option="{ option }">
          <div class="flex items-center gap-1">
            <PasteVisibilityIcon :visibility="option" class="text-lg" />
            <span>{{ option }}</span>
          </div>
        </template>
      </Select>

      <InputText
        placeholder="password (optional)"
        fluid
        size="small"
        v-model="currentPaste.password"
      />
      <div class="flex items-center gap-2">
        <Checkbox
          v-if="currentPaste.password"
          id="is-encrypted"
          :model-value="true"
          binary
          disabled
          size="small"
        />
        <Checkbox v-else id="is-encrypted" v-model="currentPaste.encrypted" binary size="small" />
        <label for="is-encrypted" class="text-sm"> Encrypted </label>
      </div>
      <div class="flex items-center gap-2">
        <Checkbox
          id="expires-checkbox"
          v-model="currentPaste.expiresAtEnabled"
          binary
          size="small"
        />
        <label for="expires-checkbox" class="text-sm"> Expires </label>
      </div>
      <div v-if="currentPaste.expiresAtEnabled">
        <DatePicker
          size="small"
          fluid
          :model-value="currentPaste.expiresAt ? new Date(currentPaste.expiresAt) : undefined"
          :min-date="new Date()"
          show-icon
          show-time
          @update:model-value="
            (date) =>
              (currentPaste.expiresAt = (date as Date)
                ?.toISOString()
                .replace('T', ' ')
                .replace('Z', ''))
          "
        />
      </div>

      <TreeSelect
        v-if="currentUserStore.user"
        :loading="userFoldersStore.isLoading"
        :model-value="currentPaste.folder ? { [currentPaste.folder]: true } : undefined"
        :options="userFolders"
        placeholder="folder"
        filter
        filter-by="label"
        fluid
        size="small"
        show-clear
        selection-mode="single"
        @update:model-value="
          (value) => (currentPaste.folder = (value && Object.keys(value)[0]) || undefined)
        "
      />
    </div>

    <div
      class="flex w-full items-center justify-between rounded-md border border-neutral-200 p-1 dark:border-neutral-700"
      v-if="currentPaste.currentId"
    >
      <span class="pl-1"> Editing {{ currentPaste.currentId }} </span>
      <Button
        @click="currentPaste.clear"
        size="small"
        icon="ti ti-x text-lg"
        severity="contrast"
        class="p-0"
        text
      />
    </div>
    <div
      class="flex w-full items-center justify-between rounded-md border border-neutral-200 p-1 dark:border-neutral-700"
      v-if="currentPaste.forkedFrom"
    >
      <span class="pl-1"> Forking {{ currentPaste.forkedFrom }} </span>
      <div class="flex items-center gap-0.5">
        <Button
          @click="currentPaste.forkedFrom = undefined"
          size="small"
          icon="ti ti-unlink text-lg"
          severity="contrast"
          class="p-0"
          text
        />
        <Button
          @click="currentPaste.clear"
          size="small"
          icon="ti ti-x text-lg"
          severity="contrast"
          class="p-0"
          text
        />
      </div>
    </div>

    <div class="flex w-full gap-1">
      <Button
        :label="currentPaste.currentId ? 'edit' : 'submit'"
        fluid
        class="text-white"
        :disabled="!currentPaste.contents"
        size="small"
        type="submit"
        :loading="isPasting"
        v-tooltip.bottom="!currentPaste.contents ? 'enter contents first' : undefined"
      />
      <div>
        <Button
          size="small"
          class="border-neutral-400"
          icon="ti ti-settings text-lg"
          severity="contrast"
          outlined
          @click="showSettings = !showSettings"
        />
      </div>
    </div>
  </form>
</template>
