<script setup lang="ts">
import Highlighted from '@/components/Highlighted.vue'
import GeneratorToggleField from '@/components/tools/utility/GeneratorToggleField.vue'
import KeyValueListField from '@/components/tools/utility/KeyValueListField.vue'
import StringListField from '@/components/tools/utility/StringListField.vue'
import UtilityResultActions from '@/components/tools/utility/UtilityResultActions.vue'
import UtilityShell from '@/components/tools/utility/UtilityShell.vue'
import InputText from 'primevue/inputtext'
import Select from 'primevue/select'
import { computed, watch } from 'vue'
import { useStorage } from '@vueuse/core'

import {
  dockerfilePresetDefaults,
  restartPolicyOptions,
  toCommandArray,
  toKeyValueEntries,
  toStringEntries,
  type KeyValueEntry,
  type DockerfilePreset,
  type RestartPolicy,
} from '@/utils/container-generators.ts'

const preset = useStorage<DockerfilePreset>('pastefy-utility-dockerfile-preset', 'node')
const baseImage = useStorage('pastefy-utility-dockerfile-base-image', 'node:20-alpine')
const runtimeImage = useStorage('pastefy-utility-dockerfile-runtime-image', '')
const buildWorkdir = useStorage('pastefy-utility-dockerfile-build-workdir', '/workspace')
const runtimeWorkdir = useStorage('pastefy-utility-dockerfile-runtime-workdir', '/app')
const copySource = useStorage('pastefy-utility-dockerfile-copy-source', '.')
const copyTarget = useStorage('pastefy-utility-dockerfile-copy-target', '/workspace')
const runCommands = useStorage<string[]>('pastefy-utility-dockerfile-run-commands', ['npm ci', 'npm run build'])
const environmentEntries = useStorage<KeyValueEntry[]>('pastefy-utility-dockerfile-env', [{ key: 'NODE_ENV', value: 'production' }])
const volumeEntries = useStorage<string[]>('pastefy-utility-dockerfile-volumes', ['/app/data'])
const portEntries = useStorage<string[]>('pastefy-utility-dockerfile-ports', ['3000'])
const labelEntries = useStorage<KeyValueEntry[]>('pastefy-utility-dockerfile-labels', [{ key: 'org.opencontainers.image.source', value: 'https://example.com/repo' }])
const networkName = useStorage('pastefy-utility-dockerfile-network', 'app-network')
const restartPolicy = useStorage<RestartPolicy>('pastefy-utility-dockerfile-restart', 'unless-stopped')
const entrypoint = useStorage('pastefy-utility-dockerfile-entrypoint', '')
const command = useStorage('pastefy-utility-dockerfile-command', 'npm run start')
const includeHealthcheck = useStorage('pastefy-utility-dockerfile-healthcheck', false)
const healthcheckCommand = useStorage('pastefy-utility-dockerfile-healthcheck-command', 'wget -qO- http://127.0.0.1:3000/health || exit 1')
const multiStage = useStorage('pastefy-utility-dockerfile-multi-stage', true)

const presetOptions = [
  { label: 'Node.js app', value: 'node' as DockerfilePreset },
  { label: 'Bun app', value: 'bun' as DockerfilePreset },
  { label: 'Java JAR', value: 'java-jar' as DockerfilePreset },
  { label: 'Python app', value: 'python' as DockerfilePreset },
  { label: 'Static site on NGINX', value: 'static-nginx' as DockerfilePreset },
  { label: 'Go app', value: 'go' as DockerfilePreset },
  { label: 'Rust app', value: 'rust' as DockerfilePreset },
  { label: 'PHP Apache', value: 'php-apache' as DockerfilePreset },
  { label: 'PHP-FPM', value: 'php-fpm' as DockerfilePreset },
  { label: '.NET app', value: 'dotnet' as DockerfilePreset },
  { label: 'Custom image', value: 'custom' as DockerfilePreset },
]

watch(
  preset,
  (value) => {
    const defaults = dockerfilePresetDefaults[value]
    baseImage.value = defaults.base
    runtimeImage.value = defaults.runtimeBase || defaults.base
    runCommands.value = [...defaults.install, ...defaults.build]
    command.value = defaults.start
    portEntries.value = [String(defaults.port)]
  },
  { immediate: true },
)

const dockerfile = computed(() => {
  const envs = toKeyValueEntries(environmentEntries.value)
  const labels = toKeyValueEntries(labelEntries.value)
  const volumes = toStringEntries(volumeEntries.value)
  const ports = toStringEntries(portEntries.value)
  const runs = toStringEntries(runCommands.value)
  const entrypointArray = toCommandArray(entrypoint.value)
  const commandArray = toCommandArray(command.value)
  const defaults = dockerfilePresetDefaults[preset.value]

  const buildStage = [
    `FROM ${baseImage.value.trim()}${multiStage.value ? ' AS build' : ''}`,
    `WORKDIR ${buildWorkdir.value.trim() || '/workspace'}`,
    `COPY ${copySource.value.trim() || '.'} ${copyTarget.value.trim() || '/workspace'}`,
    ...runs.map((line) => `RUN ${line}`),
  ]

  const runtimeLines = [
    `FROM ${multiStage.value ? runtimeImage.value.trim() || baseImage.value.trim() : baseImage.value.trim()}`,
    `WORKDIR ${runtimeWorkdir.value.trim() || '/app'}`,
    multiStage.value
      ? `COPY --from=build ${defaults.copyOutput || `${copyTarget.value.trim() || '/workspace'} ${runtimeWorkdir.value.trim() || '/app'}`}`
      : `COPY ${copySource.value.trim() || '.'} ${runtimeWorkdir.value.trim() || '/app'}`,
    ...envs.map((entry) => `ENV ${entry.key}=${entry.value}`),
    ...labels.map((entry) => `LABEL ${entry.key}=${JSON.stringify(entry.value)}`),
    ...volumes.map((entry) => `VOLUME ${JSON.stringify([entry])}`),
    ...ports.map((entry) => `EXPOSE ${entry}`),
    includeHealthcheck.value && healthcheckCommand.value.trim()
      ? `HEALTHCHECK CMD ${healthcheckCommand.value.trim()}`
      : '',
    entrypointArray.length ? `ENTRYPOINT ${JSON.stringify(entrypointArray)}` : '',
    commandArray.length ? `CMD ${JSON.stringify(commandArray)}` : '',
    '',
    '# Runtime hints',
    `# suggested restart policy: ${restartPolicy.value}`,
    `# suggested network: ${networkName.value || 'app-network'}`,
  ].filter(Boolean)

  return [...buildStage, '', ...runtimeLines].join('\n')
})
</script>

<template>
  <UtilityShell
    control-title="Dockerfile"
    control-description="Choose a preset or custom image and shape build, runtime, ports, envs, volumes, and container runtime hints visually."
    result-title="Generated Dockerfile"
    result-description="A flexible Dockerfile baseline for build and deployment flows."
  >
    <template #controls>
      <div class="grid gap-3 md:grid-cols-2">
        <div>
          <label class="mb-1 block text-sm text-neutral-500 dark:text-neutral-400">Preset</label>
          <Select v-model="preset" :options="presetOptions" option-label="label" option-value="value" fluid />
        </div>
        <div>
          <label class="mb-1 block text-sm text-neutral-500 dark:text-neutral-400">Base image</label>
          <InputText v-model="baseImage" fluid />
        </div>
        <div>
          <label class="mb-1 block text-sm text-neutral-500 dark:text-neutral-400">Runtime image</label>
          <InputText v-model="runtimeImage" fluid />
        </div>
        <div>
          <label class="mb-1 block text-sm text-neutral-500 dark:text-neutral-400">Build workdir</label>
          <InputText v-model="buildWorkdir" fluid />
        </div>
        <div>
          <label class="mb-1 block text-sm text-neutral-500 dark:text-neutral-400">Runtime workdir</label>
          <InputText v-model="runtimeWorkdir" fluid />
        </div>
        <div>
          <label class="mb-1 block text-sm text-neutral-500 dark:text-neutral-400">Copy source</label>
          <InputText v-model="copySource" fluid />
        </div>
        <div>
          <label class="mb-1 block text-sm text-neutral-500 dark:text-neutral-400">Copy target</label>
          <InputText v-model="copyTarget" fluid />
        </div>
        <div>
          <label class="mb-1 block text-sm text-neutral-500 dark:text-neutral-400">Restart policy hint</label>
          <Select v-model="restartPolicy" :options="restartPolicyOptions" option-label="label" option-value="value" fluid />
        </div>
        <div>
          <label class="mb-1 block text-sm text-neutral-500 dark:text-neutral-400">Network hint</label>
          <InputText v-model="networkName" fluid />
        </div>
      </div>

      <div class="grid gap-3 md:grid-cols-2">
        <GeneratorToggleField
          v-model="multiStage"
          label="Use multi-stage build"
          help="Split build and runtime images where it is useful."
        />
        <GeneratorToggleField
          v-model="includeHealthcheck"
          label="Add healthcheck"
          help="Useful for container health reporting and orchestration."
        />
      </div>

      <StringListField v-model="runCommands" label="RUN commands" placeholder="npm ci" add-label="add command" />
      <InputText v-if="includeHealthcheck" v-model="healthcheckCommand" placeholder="Healthcheck command" fluid />
      <KeyValueListField v-model="environmentEntries" label="Environment variables" key-placeholder="NODE_ENV" value-placeholder="production" />
      <KeyValueListField v-model="labelEntries" label="Image labels" key-placeholder="org.opencontainers.image.source" value-placeholder="https://example.com/repo" />
      <StringListField v-model="volumeEntries" label="Volumes" placeholder="/app/data" add-label="add volume" />
      <StringListField v-model="portEntries" label="Exposed ports" placeholder="3000" add-label="add port" />
      <InputText v-model="entrypoint" placeholder="Entrypoint command" fluid />
      <InputText v-model="command" placeholder="Default command" fluid />
    </template>

    <template #result>
      <div class="overflow-hidden rounded-xl border border-neutral-200 bg-white dark:border-neutral-700 dark:bg-neutral-900">
        <Highlighted :contents="dockerfile" file-name="Dockerfile" />
      </div>
    </template>

    <template #footer>
      <UtilityResultActions :result="dockerfile" file-name="Dockerfile" />
    </template>
  </UtilityShell>
</template>
