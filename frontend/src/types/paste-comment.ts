import type { PublicUser } from '@/types/user.ts'

export type PasteComment = {
  id: number
  content: string
  parent_id?: number
  line_from?: number
  line_to?: number
  created_at: string
  user?: PublicUser
  replies: PasteComment[]
}

export type PasteCommentMarker = {
  line: number
  profiles: PublicUser[]
  additional_profiles: number
}

export type CreatePasteComment = {
  content: string
  parent_id?: number
  line_from?: number
  line_to?: number
}
