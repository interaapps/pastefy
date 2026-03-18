<script setup lang="ts">
import Highlighted from '@/components/Highlighted.vue'
import UtilityResultActions from '@/components/tools/utility/UtilityResultActions.vue'
import UtilityShell from '@/components/tools/utility/UtilityShell.vue'
import InputText from 'primevue/inputtext'
import Textarea from 'primevue/textarea'
import { computed } from 'vue'
import { useStorage } from '@vueuse/core'

const serviceName = useStorage('pastefy-utility-systemd-name', 'my-app')
const description = useStorage('pastefy-utility-systemd-description', 'My application service')
const user = useStorage('pastefy-utility-systemd-user', 'www-data')
const group = useStorage('pastefy-utility-systemd-group', 'www-data')
const workingDir = useStorage('pastefy-utility-systemd-workdir', '/opt/my-app')
const execStart = useStorage('pastefy-utility-systemd-exec-start', '/usr/bin/java -jar /opt/my-app/app.jar')
const restartPolicy = useStorage('pastefy-utility-systemd-restart', 'on-failure')
const environment = useStorage('pastefy-utility-systemd-env', 'SPRING_PROFILES_ACTIVE=prod\nSERVER_PORT=8080')

const serviceFile = computed(() => `[Unit]
Description=${description.value}
After=network.target

[Service]
Type=simple
User=${user.value}
Group=${group.value}
WorkingDirectory=${workingDir.value}
${environment.value
  .split(/\r?\n/)
  .map((line) => line.trim())
  .filter(Boolean)
  .map((line) => `Environment="${line}"`)
  .join('\n')}
ExecStart=${execStart.value}
Restart=${restartPolicy.value}
RestartSec=5

[Install]
WantedBy=multi-user.target`)
</script>

<template>
  <UtilityShell
    control-title="systemd Service"
    control-description="Generate a Linux systemd unit file for app and service deployment."
    result-title="Generated Unit"
    result-description="Use this as a starting point for production or self-hosted service management."
  >
    <template #controls>
      <div class="grid gap-3 md:grid-cols-2">
        <InputText v-model="serviceName" placeholder="Service name" fluid />
        <InputText v-model="description" placeholder="Description" fluid />
        <InputText v-model="user" placeholder="User" fluid />
        <InputText v-model="group" placeholder="Group" fluid />
        <InputText v-model="workingDir" placeholder="Working directory" fluid />
        <InputText v-model="restartPolicy" placeholder="Restart policy" fluid />
        <div class="md:col-span-2">
          <InputText v-model="execStart" placeholder="ExecStart command" fluid />
        </div>
        <div class="md:col-span-2">
          <label class="mb-1 block text-sm text-neutral-500 dark:text-neutral-400">Environment variables</label>
          <Textarea v-model="environment" auto-resize rows="8" fluid />
        </div>
      </div>
    </template>

    <template #result>
      <div class="overflow-hidden rounded-xl border border-neutral-200 bg-white dark:border-neutral-700 dark:bg-neutral-900">
        <Highlighted :contents="serviceFile" :file-name="`${serviceName}.service`" />
      </div>
    </template>

    <template #footer>
      <UtilityResultActions :result="serviceFile" :file-name="`${serviceName}.service`" />
    </template>
  </UtilityShell>
</template>
