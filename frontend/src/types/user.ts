export type UserType = 'ADMIN' | 'USER' | 'BLOCKED' | 'AWAITING_ACCESS'
export type User = {
  logged_in: boolean
  id: string
  name: string
  display_name: string
  color: string
  profile_picture: string
  auth_type: string
  auth_types: string[]
  type: UserType
}
export type PublicUser = {
  id: string
  name: string
  display_name: string
  avatar: string
}
