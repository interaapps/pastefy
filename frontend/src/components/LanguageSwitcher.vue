<script setup lang="ts">
import Select from 'primevue/select'
import { computed } from 'vue'
import { useConfig } from '@/composables/config.ts'
import {
  changeLanguage,
  languageOptions,
  type LanguagePreference,
  resolveLanguagePreference,
} from '@/i18n.ts'
import { useTranslation } from 'i18next-vue'

const config = useConfig()
const { t } = useTranslation()
const localizedLanguageOptions = computed(() =>
  languageOptions.map((option) =>
    option.value === 'browser' ? { ...option, label: t('settings.browserLanguage') } : option,
  ),
)
const language = computed({
  get: () => config.value.language || 'browser',
  set: (value: LanguagePreference) => {
    config.value.language = value
    void changeLanguage(value)
  },
})

const getCurrentLanguageIcon = (l: string) => {
  if (l === 'browser') l = resolveLanguagePreference()

  return languageOptions.find((a) => a.value === l)!.icon
}
</script>

<template>
  <Select
    v-model="language"
    :options="localizedLanguageOptions"
    option-label="label"
    option-value="value"
    size="small"
    :aria-label="$t('settings.language')"
    :pt="{ root: 'flex border-0 px-0', label: 'px-1.5 py-1.5', dropdown: 'hidden' }"
  >
    <template #value="{ value: option }">
      <div class="flex items-center gap-1">
        <img
          :src="getCurrentLanguageIcon(option)"
          class="block size-4.5 rounded-full border border-neutral-200 dark:border-neutral-700"
        />
      </div>
    </template>
    <template #option="{ option }">
      <span class="flex items-center gap-1">
        <img
          :src="
            option.value === 'browser'
              ? languageOptions.find((a) => a.value === resolveLanguagePreference())!.icon ||
                option.icon
              : option.icon
          "
          class="size-4 rounded-full border border-neutral-200 dark:border-neutral-700"
        />
        <span>
          {{ option.label }}
        </span>
      </span>
    </template>
  </Select>
</template>
