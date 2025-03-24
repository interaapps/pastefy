<script setup lang="ts">
import Toast from 'primevue/toast'
import Button from 'primevue/button'
import ConfirmDialog from 'primevue/confirmdialog'
import Dialog from 'primevue/dialog'
import { useCurrentUserStore } from '@/stores/current-user.ts'

const currentUserStore = useCurrentUserStore()
</script>

<template>
  <router-view />
  <Toast />
  <ConfirmDialog>
    <template #container="{ message, acceptCallback, rejectCallback }">
      <div class="flex flex-col items-center rounded p-8">
        <span class="mb-3 block text-xl font-bold">{{ message.header }}</span>
        <p class="mb-0">{{ message.message }}</p>
        <div class="mt-6 flex items-center gap-2">
          <Button
            severity="contrast"
            label="confirm"
            @click="acceptCallback"
            class="w-32 max-w-full"
            size="small"
          />
          <Button
            label="cancel"
            outlined
            @click="rejectCallback"
            class="w-32 max-w-full"
            severity="contrast"
            size="small"
          />
        </div>
      </div>
    </template>
  </ConfirmDialog>

  <Dialog
    v-if="currentUserStore.user?.type === 'AWAITING_ACCESS'"
    :visible="currentUserStore.user?.type === 'AWAITING_ACCESS'"
    header="Awaiting Access"
    modal
    :closable="false"
    class="w-[20rem] max-w-full"
  >
    <div class="flex flex-col gap-4">
      <p>
        Hello {{ currentUserStore.user.name }}, please wait until an admin activates your account.
      </p>

      <Button @click="currentUserStore.logout()" label="Logout" outlined size="small" />
    </div>
  </Dialog>
  <Dialog
    v-if="currentUserStore.user?.type === 'BLOCKED'"
    :visible="currentUserStore.user?.type === 'BLOCKED'"
    header="Blocked"
    modal
    :closable="false"
    class="w-[20rem] max-w-full"
  >
    Hello {{ currentUserStore.user.name }}, ou have been blocked!
  </Dialog>
</template>
