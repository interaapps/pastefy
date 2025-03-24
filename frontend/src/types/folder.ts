import type { Paste } from '@/types/paste.ts'

export type Folder = {
  exists?: boolean
  id: string
  name: string
  user_id?: string
  children: Folder[]
  pastes: Paste[]
  created: string
}
