<script setup lang="ts">
import Highlighted from '@/components/Highlighted.vue'
import GeneratorToggleField from '@/components/tools/utility/GeneratorToggleField.vue'
import UtilityResultActions from '@/components/tools/utility/UtilityResultActions.vue'
import UtilityShell from '@/components/tools/utility/UtilityShell.vue'
import InputText from 'primevue/inputtext'
import Select from 'primevue/select'
import { computed } from 'vue'
import { useStorage } from '@vueuse/core'

type ScriptType = 'sh' | 'bat'

const scriptType = useStorage<ScriptType>('pastefy-utility-jar-script-type', 'sh')
const appName = useStorage('pastefy-utility-jar-script-name', 'my-app')
const jarPath = useStorage('pastefy-utility-jar-script-jar', 'app.jar')
const workingDir = useStorage('pastefy-utility-jar-script-workdir', '/opt/my-app')
const xms = useStorage('pastefy-utility-jar-script-xms', '256m')
const xmx = useStorage('pastefy-utility-jar-script-xmx', '512m')
const profile = useStorage('pastefy-utility-jar-script-profile', 'prod')
const port = useStorage('pastefy-utility-jar-script-port', '8080')
const extraJvmArgs = useStorage('pastefy-utility-jar-script-jvm', '-Dfile.encoding=UTF-8')
const extraAppArgs = useStorage('pastefy-utility-jar-script-app-args', '--server.shutdown=graceful')
const logFile = useStorage('pastefy-utility-jar-script-log', 'app.log')
const useNohup = useStorage('pastefy-utility-jar-script-nohup', true)

const scriptTypeOptions = [
  { label: 'start.sh', value: 'sh' as ScriptType },
  { label: 'start.bat', value: 'bat' as ScriptType },
]

const script = computed(() => {
  if (scriptType.value === 'bat') {
    return `@echo off
set APP_NAME=${appName.value}
set JAR_FILE=${jarPath.value}
set WORKDIR=${workingDir.value}
set JAVA_OPTS=-Xms${xms.value} -Xmx${xmx.value} ${extraJvmArgs.value}
set APP_ARGS=--spring.profiles.active=${profile.value} --server.port=${port.value} ${extraAppArgs.value}

cd /d %WORKDIR%
start /b java %JAVA_OPTS% -jar %JAR_FILE% %APP_ARGS% >> ${logFile.value} 2>&1`
  }

  return `#!/usr/bin/env bash
set -euo pipefail

APP_NAME="${appName.value}"
JAR_FILE="${jarPath.value}"
WORKDIR="${workingDir.value}"
JAVA_OPTS="-Xms${xms.value} -Xmx${xmx.value} ${extraJvmArgs.value}"
APP_ARGS="--spring.profiles.active=${profile.value} --server.port=${port.value} ${extraAppArgs.value}"

cd "$WORKDIR"
${useNohup.value ? `nohup java $JAVA_OPTS -jar "$JAR_FILE" $APP_ARGS >> "${logFile.value}" 2>&1 &` : `java $JAVA_OPTS -jar "$JAR_FILE" $APP_ARGS`}`
})

const fileName = computed(() => (scriptType.value === 'bat' ? 'start.bat' : 'start.sh'))
</script>

<template>
  <UtilityShell
    control-title="JAR Startup Script"
    control-description="Generate a start script for Java JAR deployments with JVM options, profiles, logs, and app args."
    result-title="Generated Script"
    result-description="Use this as a starting point for manual or scripted JAR launches."
  >
    <template #controls>
      <div class="grid gap-3 md:grid-cols-2">
        <div>
          <label class="mb-1 block text-sm text-neutral-500 dark:text-neutral-400">Script type</label>
          <Select v-model="scriptType" :options="scriptTypeOptions" option-label="label" option-value="value" fluid />
        </div>
        <div>
          <label class="mb-1 block text-sm text-neutral-500 dark:text-neutral-400">App name</label>
          <InputText v-model="appName" fluid />
        </div>
        <div>
          <label class="mb-1 block text-sm text-neutral-500 dark:text-neutral-400">JAR file</label>
          <InputText v-model="jarPath" fluid />
        </div>
        <div>
          <label class="mb-1 block text-sm text-neutral-500 dark:text-neutral-400">Working directory</label>
          <InputText v-model="workingDir" fluid />
        </div>
        <div>
          <label class="mb-1 block text-sm text-neutral-500 dark:text-neutral-400">Xms</label>
          <InputText v-model="xms" fluid />
        </div>
        <div>
          <label class="mb-1 block text-sm text-neutral-500 dark:text-neutral-400">Xmx</label>
          <InputText v-model="xmx" fluid />
        </div>
        <div>
          <label class="mb-1 block text-sm text-neutral-500 dark:text-neutral-400">Spring profile</label>
          <InputText v-model="profile" fluid />
        </div>
        <div>
          <label class="mb-1 block text-sm text-neutral-500 dark:text-neutral-400">Port</label>
          <InputText v-model="port" fluid />
        </div>
        <div class="md:col-span-2">
          <label class="mb-1 block text-sm text-neutral-500 dark:text-neutral-400">Extra JVM args</label>
          <InputText v-model="extraJvmArgs" fluid />
        </div>
        <div class="md:col-span-2">
          <label class="mb-1 block text-sm text-neutral-500 dark:text-neutral-400">Extra app args</label>
          <InputText v-model="extraAppArgs" fluid />
        </div>
        <div class="md:col-span-2">
          <label class="mb-1 block text-sm text-neutral-500 dark:text-neutral-400">Log file</label>
          <InputText v-model="logFile" fluid />
        </div>
      </div>
      <GeneratorToggleField
        v-if="scriptType === 'sh'"
        v-model="useNohup"
        label="Use nohup background launch"
        help="Disable this when you want the script to run in the foreground."
      />
    </template>

    <template #result>
      <div class="overflow-hidden rounded-xl border border-neutral-200 bg-white dark:border-neutral-700 dark:bg-neutral-900">
        <Highlighted :contents="script" :file-name="fileName" />
      </div>
    </template>

    <template #footer>
      <UtilityResultActions :result="script" :file-name="fileName" />
    </template>
  </UtilityShell>
</template>
