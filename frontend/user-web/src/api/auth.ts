import { request } from './http'
import type { AuthResponse, UserProfile } from './types'

export function sendSms(phone: string) {
  return request<{ mockCode: string | null }>({
    url: '/auth/sms/send',
    method: 'post',
    data: { phone }
  })
}

export function register(payload: {
  phone: string
  code: string
  password: string
  nickname?: string
}) {
  return request<AuthResponse>({ url: '/auth/register', method: 'post', data: payload })
}

export function login(payload: { phone: string; password: string }) {
  return request<AuthResponse>({ url: '/auth/login', method: 'post', data: payload })
}

export function logout() {
  return request<void>({ url: '/auth/logout', method: 'post' })
}

export function fetchProfile() {
  return request<UserProfile>({ url: '/user/profile' })
}

export function updateNickname(nickname: string) {
  return request<void>({ url: '/user/profile', method: 'put', data: { nickname } })
}

export function uploadAvatar(file: File) {
  const data = new FormData()
  data.append('file', file)
  return request<UserProfile>({ url: '/user/avatar', method: 'post', data })
}

export function changePassword(oldPassword: string, newPassword: string) {
  return request<void>({
    url: '/user/password',
    method: 'put',
    data: { oldPassword, newPassword }
  })
}
