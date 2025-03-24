<script lang="ts" setup>
import Dialog from 'primevue/dialog'
import { useCurrentUserStore } from '@/stores/current-user.ts'
const currentUserStore = useCurrentUserStore()

const visible = defineModel<boolean>('visible')
</script>
<template>
  <Dialog v-model:visible="visible" modal header="Login" class="w-[30rem] max-w-full">
    <div class="flex flex-wrap justify-center gap-2">
      <a
        v-for="authType of currentUserStore.authTypes"
        :href="`/api/v2/auth/oauth2/${authType}`"
        :key="authType"
        class="flex aspect-square w-[6rem] flex-col items-center justify-center gap-2 rounded-xl border border-neutral-300 p-4 dark:border-neutral-700"
      >
        <svg
          v-if="authType === 'interaapps'"
          width="24"
          height="24"
          viewBox="0 0 24 24"
          fill="none"
          xmlns="http://www.w3.org/2000/svg"
        >
          <path
            d="M1.10297 24L9.38217 0H15.052L23.3312 24H18.5634L12.2171 4.8595L5.83854 24H1.10297Z"
            fill="#4542F4"
          />
          <path d="M0 24V0H4.38121V24H0Z" fill="#4542F4" />
        </svg>

        <i v-else :class="`ti ti-brand-${authType} text-3xl`" />

        <span class="text-sm">{{ authType }}</span>
      </a>
    </div>
  </Dialog>
</template>
