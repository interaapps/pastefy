<script setup lang="ts">
import Button from 'primevue/button'
import InputText from 'primevue/inputtext'

type Entry = {
  key: string
  value: string
}

const props = withDefaults(
  defineProps<{
    label: string
    keyPlaceholder?: string
    valuePlaceholder?: string
    addLabel?: string
  }>(),
  {
    addLabel: 'add entry',
  },
)

const model = defineModel<Entry[]>({ required: true })

const addItem = () => {
  model.value = [...model.value, { key: '', value: '' }]
}

const removeItem = (index: number) => {
  model.value = model.value.filter((_, currentIndex) => currentIndex !== index)
}

const updateItem = (index: number, next: Partial<Entry>) => {
  model.value = model.value.map((entry, currentIndex) =>
    currentIndex === index ? { ...entry, ...next } : entry,
  )
}
</script>

<template>
  <div class="space-y-3">
    <div class="flex items-center justify-between gap-3">
      <label class="block text-sm text-neutral-500 dark:text-neutral-400">{{ props.label }}</label>
      <Button @click="addItem" :label="props.addLabel" icon="ti ti-plus" severity="contrast" outlined size="small" />
    </div>

    <div class="space-y-2">
      <div v-for="(entry, index) of model" :key="index" class="grid gap-2 md:grid-cols-[0.9fr_1.1fr_auto]">
        <InputText
          :model-value="entry.key"
          @update:model-value="(value) => updateItem(index, { key: String(value ?? '') })"
          :placeholder="props.keyPlaceholder || 'Key'"
          fluid
        />
        <InputText
          :model-value="entry.value"
          @update:model-value="(value) => updateItem(index, { value: String(value ?? '') })"
          :placeholder="props.valuePlaceholder || 'Value'"
          fluid
        />
        <Button
          @click="removeItem(index)"
          icon="ti ti-trash"
          severity="contrast"
          outlined
          aria-label="Remove entry"
        />
      </div>
    </div>

    <div v-if="!model.length" class="rounded-xl border border-dashed border-neutral-200 px-3 py-3 text-sm text-neutral-500 dark:border-neutral-700 dark:text-neutral-400">
      No entries yet.
    </div>
  </div>
</template>
