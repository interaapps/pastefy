<script setup lang="ts">
import Button from 'primevue/button'
import InputNumber from 'primevue/inputnumber'
import Textarea from 'primevue/textarea'
import { ref } from 'vue'

const props = defineProps<{
  lineFrom?: number
  loading?: boolean
  compact?: boolean
}>()

const emit = defineEmits<{
  submit: [content: string, lineTo?: number]
  cancel: []
}>()

const content = ref('')
const lineTo = ref<number>()

const submit = () => {
  const trimmedContent = content.value.trim()
  if (!trimmedContent) return

  emit(
    'submit',
    trimmedContent,
    props.lineFrom && lineTo.value && lineTo.value > props.lineFrom ? lineTo.value : undefined,
  )
}
</script>

<template>
  <form class="flex flex-col gap-2" @submit.prevent="submit">
    <Textarea
      v-model="content"
      :rows="1"
      maxlength="2000"
      class="rounded-none border-0 border-b-1 bg-transparent px-0"
      auto-resize
      fluid
      :placeholder="$t('comments.placeholder')"
    />
    <div v-if="lineFrom" class="flex items-center gap-2 text-sm">
      <span class="text-neutral-500">{{ $t('comments.fromLine', { line: lineFrom }) }}</span>
      <InputNumber
        v-model="lineTo"
        :min="lineFrom"
        :max="lineFrom + 999"
        size="small"
        :placeholder="$t('comments.optionalEndLine')"
        input-class="w-36"
      />
    </div>
    <div class="flex items-center justify-between gap-2">
      <span class="text-xs text-neutral-500">{{ content.length }} / 2000</span>
      <div class="flex gap-2">
        <Button
          v-if="compact"
          type="button"
          :label="$t('common.cancel')"
          size="small"
          severity="contrast"
          text
          @click="emit('cancel')"
        />
        <Button
          type="submit"
          :label="$t('comments.submit')"
          size="small"
          severity="contrast"
          outlined
          :loading
          :disabled="!content.trim()"
        />
      </div>
    </div>
  </form>
</template>
