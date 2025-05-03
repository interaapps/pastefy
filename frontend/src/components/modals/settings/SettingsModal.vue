<script lang="ts" setup>
import Dialog from 'primevue/dialog'
import Button from 'primevue/button'
import AppSettings from '@/components/modals/settings/AppSettings.vue'
import { ref } from 'vue'

const visible = defineModel<boolean>('visible')
const tab = ref('app-settings')
</script>
<template>
  <Dialog
    v-model:visible="visible"
    modal
    header="Settings"
    class="h-[20rem] w-[46rem] max-w-full overflow-hidden"
  >
    <template #container="{ closeCallback }">
      <div class="relative h-full md:grid md:grid-cols-[14rem_1fr]" @click.stop>
        <Button
          text
          rounded
          icon="ti ti-x text-xl"
          class="absolute top-3 right-3 z-10"
          severity="contrast"
          @click="closeCallback"
        />
        <div
          class="flex flex-col gap-1 border-r border-neutral-200 bg-neutral-100 md:h-full dark:border-neutral-700 dark:bg-neutral-800"
        >
          <span class="block p-3 pb-1 text-sm">Settings</span>
          <div class="flex flex-col p-1">
            <Button
              :text="tab !== 'app-settings'"
              icon="ti ti-settings text-lg"
              severity="contrast"
              fluid
              label="App Settings"
              class="justify-start"
              size="small"
              @click="tab = 'app-settings'"
            />
          </div>
        </div>
        <div class="h-full overflow-auto p-6 pt-16">
          <AppSettings v-if="tab === 'app-settings'" />
        </div>
      </div>
    </template>
  </Dialog>
</template>
