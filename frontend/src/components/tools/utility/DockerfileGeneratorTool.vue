<script setup lang="ts">
import { useTranslation } from 'i18next-vue'
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

const { t } = useTranslation()
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
  { get label() { return t('utility.dockerfileGeneratorTool.options.nodeJsApp') }, value: 'node' as DockerfilePreset },
  { get label() { return t('utility.dockerfileGeneratorTool.options.bunApp') }, value: 'bun' as DockerfilePreset },
  { get label() { return t('utility.dockerfileGeneratorTool.options.javaJar') }, value: 'java-jar' as DockerfilePreset },
  { get label() { return t('utility.dockerfileGeneratorTool.options.pythonApp') }, value: 'python' as DockerfilePreset },
  { get label() { return t('utility.dockerfileGeneratorTool.options.staticSiteOnNginx') }, value: 'static-nginx' as DockerfilePreset },
  { get label() { return t('utility.dockerfileGeneratorTool.options.goApp') }, value: 'go' as DockerfilePreset },
  { get label() { return t('utility.dockerfileGeneratorTool.options.rustApp') }, value: 'rust' as DockerfilePreset },
  { get label() { return t('utility.dockerfileGeneratorTool.options.phpApache') }, value: 'php-apache' as DockerfilePreset },
  { get label() { return t('utility.dockerfileGeneratorTool.options.phpFpm') }, value: 'php-fpm' as DockerfilePreset },
  { get label() { return t('utility.dockerfileGeneratorTool.options.dotNetApp') }, value: 'dotnet' as DockerfilePreset },
  { get label() { return t('utility.dockerfileGeneratorTool.options.customImage') }, value: 'custom' as DockerfilePreset },
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
          <label class="mb-1 block text-sm text-neutral-500 dark:text-neutral-400">{{ $t('utility.dockerfileGeneratorTool.preset') }}</label>
          <Select v-model="preset" :options="presetOptions" option-label="label" option-value="value" fluid />
        </div>
        <div>
          <label class="mb-1 block text-sm text-neutral-500 dark:text-neutral-400">{{ $t('utility.dockerfileGeneratorTool.baseImage') }}</label>
          <InputText v-model="baseImage" fluid />
        </div>
        <div>
          <label class="mb-1 block text-sm text-neutral-500 dark:text-neutral-400">{{ $t('utility.dockerfileGeneratorTool.runtimeImage') }}</label>
          <InputText v-model="runtimeImage" fluid />
        </div>
        <div>
          <label class="mb-1 block text-sm text-neutral-500 dark:text-neutral-400">{{ $t('utility.dockerfileGeneratorTool.buildWorkdir') }}</label>
          <InputText v-model="buildWorkdir" fluid />
        </div>
        <div>
          <label class="mb-1 block text-sm text-neutral-500 dark:text-neutral-400">{{ $t('utility.dockerfileGeneratorTool.runtimeWorkdir') }}</label>
          <InputText v-model="runtimeWorkdir" fluid />
        </div>
        <div>
          <label class="mb-1 block text-sm text-neutral-500 dark:text-neutral-400">{{ $t('utility.dockerfileGeneratorTool.copySource') }}</label>
          <InputText v-model="copySource" fluid />
        </div>
        <div>
          <label class="mb-1 block text-sm text-neutral-500 dark:text-neutral-400">{{ $t('utility.dockerfileGeneratorTool.copyTarget') }}</label>
          <InputText v-model="copyTarget" fluid />
        </div>
        <div>
          <label class="mb-1 block text-sm text-neutral-500 dark:text-neutral-400">{{ $t('utility.dockerfileGeneratorTool.restartPolicyHint') }}</label>
          <Select v-model="restartPolicy" :options="restartPolicyOptions" option-label="label" option-value="value" fluid />
        </div>
        <div>
          <label class="mb-1 block text-sm text-neutral-500 dark:text-neutral-400">{{ $t('utility.dockerfileGeneratorTool.networkHint') }}</label>
          <InputText v-model="networkName" fluid />
        </div>
      </div>

      <div class="grid gap-3 md:grid-cols-2">
        <GeneratorToggleField
          v-model="multiStage"
          :label="$t('utility.dockerfileGeneratorTool.useMultiStageBuild')"
          help="Split build and runtime images where it is useful."
        />
        <GeneratorToggleField
          v-model="includeHealthcheck"
          :label="$t('utility.dockerfileGeneratorTool.addHealthcheck')"
          help="Useful for container health reporting and orchestration."
        />
      </div>

      <StringListField v-model="runCommands" :label="$t('utility.dockerfileGeneratorTool.runCommands')" :placeholder="$t('utility.dockerfileGeneratorTool.npmCi')" :add-label="$t('utility.dockerfileGeneratorTool.addCommand')" />
      <InputText v-if="includeHealthcheck" v-model="healthcheckCommand" :placeholder="$t('utility.dockerfileGeneratorTool.healthcheckCommand')" fluid />
      <KeyValueListField v-model="environmentEntries" :label="$t('utility.dockerfileGeneratorTool.environmentVariables')" key-placeholder="NODE_ENV" :value-placeholder="$t('utility.dockerfileGeneratorTool.production')" />
      <KeyValueListField v-model="labelEntries" :label="$t('utility.dockerfileGeneratorTool.imageLabels')" key-placeholder="org.opencontainers.image.source" value-placeholder="https://example.com/repo" />
      <StringListField v-model="volumeEntries" :label="$t('utility.dockerfileGeneratorTool.volumes')" placeholder="/app/data" :add-label="$t('utility.dockerfileGeneratorTool.addVolume')" />
      <StringListField v-model="portEntries" :label="$t('utility.dockerfileGeneratorTool.exposedPorts')" placeholder="3000" :add-label="$t('utility.dockerfileGeneratorTool.addPort')" />
      <InputText v-model="entrypoint" :placeholder="$t('utility.dockerfileGeneratorTool.entrypointCommand')" fluid />
      <InputText v-model="command" :placeholder="$t('utility.dockerfileGeneratorTool.defaultCommand')" fluid />
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
