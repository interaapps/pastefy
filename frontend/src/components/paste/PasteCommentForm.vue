<script setup lang="ts">
import Button from 'primevue/button'
import InputNumber from 'primevue/inputnumber'
import Textarea from 'primevue/textarea'
import { ref } from 'vue'
import { useCurrentUserStore } from '@/stores/current-user.ts'

const props = defineProps<{
  lineFrom?: number
  loading?: boolean
  compact?: boolean
}>()

const currentUser = useCurrentUserStore()

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
  <div class="flex w-full gap-3">
    <div v-if="currentUser?.user" class="py-2">
      <img
        class="h-[24px] w-[24px] rounded-full border-1 border-neutral-200 object-cover dark:border-neutral-700"
        :src="currentUser.user.profile_picture"
        alt="Profile Picture"
      />
    </div>
    <form class="flex flex-1 flex-col gap-2" @submit.prevent="submit">
      <Textarea
        v-model="content"
        :rows="1"
        maxlength="2000"
        class="rounded-none border-0 border-b-1 bg-transparent px-0"
        auto-resize
        fluid
        :placeholder="$t('comments.placeholder')"
      />
      <div class="flex items-center justify-between gap-2">
        <div v-if="lineFrom" class="flex items-center gap-2 text-sm">
          <InputNumber :model-value="lineFrom" disabled size="small" input-class="w-20" />
          <span> - </span>
          <InputNumber
            v-model="lineTo"
            :min="lineFrom"
            :max="lineFrom + 999"
            size="small"
            :placeholder="$t('comments.optionalEndLine')"
            input-class="w-20"
          />
        </div>
        <span v-if="!lineFrom" class="text-xs text-neutral-500">{{ content.length }} / 2000</span>
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
            :loading
            :disabled="!content.trim()"
          />
        </div>
      </div>
    </form>
  </div>
</template>
