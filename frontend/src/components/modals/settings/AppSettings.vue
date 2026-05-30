<script lang="ts" setup>
import Divider from 'primevue/divider'
import { useConfig } from '@/composables/config.ts'
import Checkbox from 'primevue/checkbox'
import ThemeSwitcher from '@/components/ThemeSwitcher.vue'
import PasteVisibilityIcon from '@/components/PasteVisibilityIcon.vue'
import Select from 'primevue/select'
import LanguageSwitcher from '@/components/LanguageSwitcher.vue'

const config = useConfig()
</script>
<template>
  <div class="flex flex-col" @click.stop>
    <div class="flex items-center justify-between">
      <label for="animations" class="text-sm">{{ $t('settings.animations') }}</label>
      <Checkbox id="animations" v-model="config.animations" binary size="small" />
    </div>
    <Divider />

    <div class="flex items-center justify-between gap-2">
      <label for="theme" class="text-sm">{{ $t('settings.theme') }}</label>
      <ThemeSwitcher />
    </div>
    <Divider />
    <div class="flex items-center justify-between gap-2">
      <label for="language" class="text-sm">{{ $t('settings.language') }}</label>
      <LanguageSwitcher id="language" />
    </div>
    <Divider />
    <div class="flex items-center justify-between gap-2">
      <label for="default-visibility" class="text-sm">{{ $t('settings.defaultVisibility') }}</label>
      <Select
        :options="['UNLISTED', 'PUBLIC', 'PRIVATE']"
        v-model="config.defaultVisibility"
        size="small"
      >
        <template #value="{ value: option }">
          <div class="flex items-center gap-1">
            <PasteVisibilityIcon :visibility="option" class="text-lg" />
            <span>{{ $t(`paste.visibility.${option.toLowerCase()}`) }}</span>
          </div>
        </template>
        <template #option="{ option }">
          <div class="flex items-center gap-1">
            <PasteVisibilityIcon :visibility="option" class="text-lg" />
            <span>{{ $t(`paste.visibility.${option.toLowerCase()}`) }}</span>
          </div>
        </template>
      </Select>
    </div>
  </div>
</template>
