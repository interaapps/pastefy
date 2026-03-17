<script setup lang="ts">
import { computed, defineAsyncComponent } from 'vue'
import Tag from 'primevue/tag'
const Highlighted = defineAsyncComponent(() => import('@/components/Highlighted.vue'))

const props = defineProps<{
  playbook: string
}>()

const hosts = computed(() =>
  Array.from(props.playbook.matchAll(/^\s*hosts\s*:\s*(.+)$/gm)).map((match) => match[1].trim()),
)
const roles = computed(() =>
  Array.from(props.playbook.matchAll(/^\s*-\s*role\s*:\s*(.+)$/gm)).map((match) => match[1].trim()),
)
const tasks = computed(() =>
  Array.from(props.playbook.matchAll(/^\s*-\s*name\s*:\s*(.+)$/gm)).map((match) => match[1].trim()),
)
</script>

<template>
  <div class="overflow-hidden text-sm">
    <div
      class="flex flex-col gap-4 border-b border-neutral-200 bg-neutral-50 px-4 py-4 dark:border-neutral-700 dark:bg-neutral-900/70"
    >
      <div class="flex flex-wrap items-center justify-between gap-3">
        <div>
          <div class="text-xs font-semibold tracking-[0.2em] uppercase opacity-60">Ansible View</div>
          <div class="mt-1 text-xs opacity-60">Hosts, roles, and named tasks for operational playbooks</div>
        </div>
        <div class="flex flex-wrap gap-2">
          <Tag :value="`${hosts.length} hosts`" severity="contrast" />
          <Tag :value="`${roles.length} roles`" severity="contrast" />
          <Tag :value="`${tasks.length} tasks`" severity="contrast" />
        </div>
      </div>

      <div v-if="hosts.length" class="flex flex-wrap gap-2">
        <Tag v-for="host in hosts" :key="host" :value="host" severity="info" />
      </div>
    </div>

    <div class="grid gap-0 border-b border-neutral-200 dark:border-neutral-700 md:grid-cols-2">
      <div class="border-b border-neutral-200 p-4 md:border-r md:border-b-0 dark:border-neutral-700">
        <div class="mb-3 text-xs font-semibold tracking-[0.2em] uppercase opacity-60">Roles</div>
        <div class="flex flex-wrap gap-2">
          <Tag v-for="role in roles" :key="role" :value="role" severity="contrast" />
        </div>
      </div>
      <div class="p-4">
        <div class="mb-3 text-xs font-semibold tracking-[0.2em] uppercase opacity-60">Named Tasks</div>
        <div class="space-y-2 font-[JetBrains_Mono_Variable] text-[0.9rem]">
          <div v-if="tasks.length === 0" class="opacity-50">No named tasks detected.</div>
          <div v-for="task in tasks.slice(0, 18)" :key="task" class="break-all">{{ task }}</div>
        </div>
      </div>
    </div>

    <Highlighted class="max-h-[24rem] overflow-auto" file-name="playbook.yml" :contents="playbook" show-copy-button />
  </div>
</template>
