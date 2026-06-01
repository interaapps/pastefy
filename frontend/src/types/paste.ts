import type { PublicUser } from '@/types/user.ts'

export type PasteType = 'PASTE' | 'MULTI_PASTE'
export type PasteVisibility = 'PUBLIC' | 'PRIVATE' | 'UNLISTED'

export type PasteAiInfo = {
  dangerous?: boolean
  suggested_filename: string
  warnings: {
    description: string
    severity: number
  }[]
  tags: string[]
  description: string
}
export type Paste = {
  id?: string
  content: string
  title: string
  encrypted: boolean
  visibility: PasteVisibility
  raw_url?: string
  type: PasteType
  created_at?: string
  expire_at?: string
  tags?: string[]
  forked_from?: string
  user_id?: string
  folder?: string
  user?: PublicUser
  starred?: boolean

  ai?: boolean
  ai_info?: PasteAiInfo
}

export type MultiPastePart = {
  name: string
  contents: string
}
