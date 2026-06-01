<script setup lang="ts">
import Button from 'primevue/button'
import { computed, ref } from 'vue'
import type { PasteComment } from '@/types/paste-comment.ts'
import PasteCommentForm from '@/components/paste/PasteCommentForm.vue'

const props = withDefaults(
  defineProps<{
    comment: PasteComment
    canDelete?: boolean
    canReply?: boolean
    submitting?: boolean
    currentUserId?: string
  }>(),
  { canReply: true },
)

const emit = defineEmits<{
  reply: [commentId: number, content: string]
  delete: [commentId: number]
}>()

const replying = ref(false)
const showDelete = computed(
  () =>
    props.canDelete || (!!props.currentUserId && props.comment.user?.id === props.currentUserId),
)

const reply = (content: string) => {
  emit('reply', props.comment.id, content)
  replying.value = false
}

const formatDate = (value: string) => new Date(value).toLocaleString()
</script>

<template>
  <article class="flex gap-2">
    <span
      class="flex h-8 w-8 shrink-0 items-center justify-center overflow-hidden rounded-full bg-neutral-200 text-xs font-bold text-neutral-600 dark:bg-neutral-700 dark:text-neutral-200"
    >
      <img
        v-if="comment.user?.avatar"
        :src="comment.user.avatar"
        alt=""
        class="h-full w-full object-cover"
      />
      <span v-else>{{
        (comment.user?.display_name || comment.user?.name || '?').slice(0, 1)
      }}</span>
    </span>
    <div class="min-w-0 flex-1">
      <div class="flex flex-wrap items-center gap-x-2 gap-y-1">
        <strong class="text-sm">{{
          comment.user?.display_name || comment.user?.name || $t('comments.deletedUser')
        }}</strong>
        <span class="text-xs text-neutral-500">{{ formatDate(comment.created_at) }}</span>
        <span
          v-if="comment.line_from"
          class="rounded bg-neutral-100 px-1.5 py-0.5 text-xs text-neutral-500 dark:bg-neutral-800"
        >
          {{
            comment.line_to
              ? $t('comments.lines', { from: comment.line_from, to: comment.line_to })
              : $t('comments.line', { line: comment.line_from })
          }}
        </span>

        <Button
          v-if="canReply"
          :label="$t('comments.reply')"
          size="small"
          severity="contrast"
          text
          class="px-0 py-0 text-xs"
          @click="replying = !replying"
        />
        <Button
          v-if="showDelete"
          :label="$t('common.delete')"
          size="small"
          severity="contrast"
          text
          class="px-0 py-0 text-xs"
          @click="emit('delete', comment.id)"
        />
      </div>
      <p class="mt-1 text-sm break-words whitespace-pre-wrap">{{ comment.content }}</p>
      <PasteCommentForm
        v-if="replying"
        class="mt-2"
        compact
        :loading="submitting"
        @submit="reply"
        @cancel="replying = false"
      />
      <div
        v-if="comment.replies?.length"
        class="mt-3 flex flex-col gap-3 border-l border-neutral-200 pl-3 dark:border-neutral-700"
      >
        <PasteCommentItem
          v-for="replyComment of comment.replies"
          :key="replyComment.id"
          :comment="replyComment"
          :can-delete
          :can-reply
          :submitting
          :current-user-id
          @reply="(commentId, content) => emit('reply', commentId, content)"
          @delete="(commentId) => emit('delete', commentId)"
        />
      </div>
    </div>
  </article>
</template>
