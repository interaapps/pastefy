<script setup lang="ts">
import Dialog from 'primevue/dialog'
import Popover from 'primevue/popover'
import { useAsyncState } from '@vueuse/core'
import { client } from '@/main.ts'
import { computed, ref, useTemplateRef, watch } from 'vue'
import type { PopoverMethods } from 'primevue'
import type { CreatePasteComment, PasteComment, PasteCommentMarker } from '@/types/paste-comment.ts'
import { useCurrentUserStore } from '@/stores/current-user.ts'
import ErrorContainer from '@/components/ErrorContainer.vue'
import LoadingContainer from '@/components/LoadingContainer.vue'
import Pagination from '@/components/Pagination.vue'
import PasteCommentForm from '@/components/paste/PasteCommentForm.vue'
import PasteCommentItem from '@/components/paste/PasteCommentItem.vue'

const props = defineProps<{
  pasteId: string
  pasteUserId?: string
  pasteContents?: string
  pasteFileName?: string
  enableLineComments?: boolean
}>()

const emit = defineEmits<{
  markersUpdated: [markers: PasteCommentMarker[]]
}>()

const page = ref(1)
const pageLimit = 10
const formKey = ref(0)
const selectedLine = ref<number>()
const dialogVisible = ref(false)
const currentUser = useCurrentUserStore()
const lineCommentPopover = useTemplateRef<PopoverMethods>('lineCommentPopover')

const canDelete = computed(
  () =>
    currentUser.user?.type === 'ADMIN' ||
    (!!props.pasteUserId && currentUser.user?.id === props.pasteUserId),
)

const {
  state: comments,
  isLoading,
  error,
  execute: loadComments,
} = useAsyncState(async () => {
  return (
    await client.get(`/api/v2/paste/${props.pasteId}/comments`, {
      params: { page: page.value, page_limit: pageLimit },
    })
  ).data as PasteComment[]
}, undefined)

const { execute: loadMarkers } = useAsyncState(async () => {
  if (!props.enableLineComments) return []
  const markers = (await client.get(`/api/v2/paste/${props.pasteId}/comments/markers`))
    .data as PasteCommentMarker[]
  emit('markersUpdated', markers)
  return markers
}, [])

const {
  state: lineComments,
  isLoading: lineCommentsLoading,
  error: lineCommentsError,
  execute: loadLineComments,
} = useAsyncState(
  async () => {
    if (!selectedLine.value) return []

    return (
      await client.get(`/api/v2/paste/${props.pasteId}/comments`, {
        params: { line: selectedLine.value, page_limit: 30 },
      })
    ).data as PasteComment[]
  },
  [],
  { immediate: false },
)

const reloadComments = async () => {
  await Promise.all([
    loadComments(),
    loadMarkers(),
    ...(selectedLine.value ? [loadLineComments()] : []),
  ])
}

const {
  isLoading: isSubmitting,
  error: submitError,
  execute: createComment,
} = useAsyncState(
  async (request: CreatePasteComment) => {
    await client.post(`/api/v2/paste/${props.pasteId}/comments`, request)
    formKey.value++
    await reloadComments()
  },
  undefined,
  { immediate: false },
)

const { execute: deleteComment } = useAsyncState(
  async (commentId: number) => {
    await client.delete(`/api/v2/paste/${props.pasteId}/comments/${commentId}`)
    await reloadComments()
  },
  undefined,
  { immediate: false },
)

const submit = (request: CreatePasteComment) => createComment(0, request)

const submitRoot = (content: string) => {
  selectedLine.value = undefined
  page.value = 1
  return submit({ content })
}

const submitLine = (content: string, lineTo?: number) => {
  page.value = 1
  return submit({
    content,
    line_from: selectedLine.value,
    line_to: lineTo,
  })
}

const submitReply = (parentId: number, content: string) => submit({ content, parent_id: parentId })

const openLineComment = async (event: Event, line: number, target: HTMLElement) => {
  if (!props.enableLineComments) return
  selectedLine.value = line
  lineCommentPopover.value?.show(event, target)
  await loadLineComments()
}

const openDialog = () => {
  dialogVisible.value = true
  loadComments()
}

watch(page, () => loadComments())
defineExpose({ openDialog, openLineComment })
</script>

<template>
  <Dialog
    v-model:visible="dialogVisible"
    modal
    :header="$t('comments.title')"
    class="w-[42rem] max-w-full"
  >
    <p v-if="!currentUser.user" class="text-sm text-neutral-500">
      {{ $t('comments.loginRequired') }}
    </p>
    <PasteCommentForm
      v-else
      :key="`root-${formKey}`"
      :loading="isSubmitting"
      @submit="submitRoot"
    />

    <ErrorContainer v-if="submitError" class="mt-3" :error="submitError as any" />
    <LoadingContainer v-if="isLoading" class="mt-4" />
    <div v-else-if="comments?.length" class="mt-4 flex max-h-[60vh] flex-col gap-4 overflow-auto">
      <PasteCommentItem
        v-for="comment of comments"
        :key="comment.id"
        :comment
        :can-delete
        :can-reply="!!currentUser.user"
        :submitting="isSubmitting"
        :current-user-id="currentUser.user?.id"
        :paste-contents
        :paste-file-name
        @reply="submitReply"
        @delete="(commentId) => deleteComment(0, commentId)"
      />
      <Pagination
        v-model:page="page"
        :loading="isLoading"
        :next-disabled="comments.length < pageLimit"
      />
    </div>
    <ErrorContainer v-else-if="error" class="mt-3" :error="error as any" />
    <p v-else class="mt-3 text-sm text-neutral-500">{{ $t('comments.empty') }}</p>
  </Dialog>

  <Popover ref="lineCommentPopover" @hide="selectedLine = undefined" class="max-w-full">
    <div class="w-[32rem] max-w-full">
      <h3 class="mb-2 text-sm font-bold">
        {{ $t('comments.commentLine', { line: selectedLine }) }}
      </h3>
      <PasteCommentForm
        v-if="currentUser.user"
        :key="`line-${formKey}-${selectedLine}`"
        :line-from="selectedLine"
        :loading="isSubmitting"
        @submit="submitLine"
      />
      <p v-else class="text-sm text-neutral-500">{{ $t('comments.loginRequired') }}</p>
      <ErrorContainer v-if="lineCommentsError" class="mt-3" :error="lineCommentsError as any" />
      <LoadingContainer v-else-if="lineCommentsLoading" class="mt-3" />
      <div
        v-else-if="lineComments.length"
        class="mt-4 flex max-h-[24rem] flex-col gap-4 overflow-auto"
      >
        <PasteCommentItem
          v-for="comment of lineComments"
          :key="comment.id"
          :comment
          :can-delete
          :can-reply="!!currentUser.user"
          :submitting="isSubmitting"
          :current-user-id="currentUser.user?.id"
          :paste-contents
          :paste-file-name
          @reply="submitReply"
          @delete="(commentId) => deleteComment(0, commentId)"
        />
      </div>
      <p v-else class="mt-3 text-sm text-neutral-500">{{ $t('comments.emptyForLine') }}</p>
    </div>
  </Popover>
</template>
