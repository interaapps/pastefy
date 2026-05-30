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
            :label="$t('common.confirm')"
            @click="acceptCallback"
            class="w-32 max-w-full"
            size="small"
          />
          <Button
            :label="$t('common.cancel')"
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
    :header="$t('auth.awaitingAccess')"
    modal
    :closable="false"
    class="w-[20rem] max-w-full"
  >
    <div class="flex flex-col gap-4">
      <p>{{ $t('auth.awaitingMessage', { name: currentUserStore.user.name }) }}</p>

      <Button @click="currentUserStore.logout()" :label="$t('auth.logout')" outlined size="small" />
    </div>
  </Dialog>
  <Dialog
    v-if="currentUserStore.user?.type === 'BLOCKED'"
    :visible="currentUserStore.user?.type === 'BLOCKED'"
    :header="$t('auth.blocked')"
    modal
    :closable="false"
    class="w-[20rem] max-w-full"
  >
    {{ $t('auth.blockedMessage', { name: currentUserStore.user.name }) }}
  </Dialog>
</template>
