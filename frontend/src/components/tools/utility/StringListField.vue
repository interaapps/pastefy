<script setup lang="ts">
import Button from 'primevue/button'
import InputText from 'primevue/inputtext'

const props = withDefaults(
  defineProps<{
    label: string
    placeholder?: string
    addLabel?: string
  }>(),
  {
    addLabel: 'add entry',
  },
)

const model = defineModel<string[]>({ required: true })

const addItem = () => {
  model.value = [...model.value, '']
}

const removeItem = (index: number) => {
  model.value = model.value.filter((_, currentIndex) => currentIndex !== index)
}

const updateItem = (index: number, value: string) => {
  model.value = model.value.map((entry, currentIndex) => (currentIndex === index ? value : entry))
}
</script>

<template>
  <div class="space-y-3">
    <div class="flex items-center justify-between gap-3">
      <label class="block text-sm text-neutral-500 dark:text-neutral-400">{{ props.label }}</label>
      <Button @click="addItem" :label="props.addLabel" icon="ti ti-plus" severity="contrast" outlined size="small" />
    </div>

    <div class="space-y-2">
      <div v-for="(entry, index) of model" :key="index" class="flex items-center gap-2">
        <InputText
          :model-value="entry"
          @update:model-value="(value) => updateItem(index, String(value ?? ''))"
          :placeholder="props.placeholder"
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
