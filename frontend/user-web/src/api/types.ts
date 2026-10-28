export interface UserProfile {
  id: number
  phone: string
  nickname: string
  avatar: string
  createdAt: string
}

export interface AuthResponse {
  accessToken: string
  refreshToken: string
  expiresIn: number
  user: UserProfile
}

export interface DomainItem {
  id: number
  domain: string
  defaultFlag: boolean
}

export type LinkStatus = 'normal' | 'expired' | 'disabled' | 'deleted'

export interface LinkItem {
  id: number
  code: string
  shortUrl: string
  originUrl: string
  remark: string
  domainId: number
  domain: string
  status: LinkStatus
  hasPassword: boolean
  clickCount: number
  startAt?: string
  endAt?: string
  expireAt?: string
  createdAt: string
}

export interface CreateLinkPayload {
  originUrl: string
  domainId?: number
  remark?: string
  code?: string
  password?: string
  startAt?: string
  endAt?: string
  expireAt?: string
}

export interface CreateLinkResult {
  id: number
  code: string
  shortUrl: string
  duplicated: boolean
  createdAt: string
}

export interface PageVO<T> {
  total: number
  page: number
  size: number
  records: T[]
}

export interface StatOverview {
  pv: number
  uv: number
  ipCount: number
  linkCount: number
}

export interface TrendPoint {
  date: string
  pv: number
  uv: number
  ipCount: number
}

export interface RegionItem {
  province: string
  pv: number
}

export interface ClickLogItem {
  clickAt: string
  code: string
  ip: string
  province: string
  city: string
  device: string
  referer: string
}

export type StatRange = 'today' | 'yesterday' | 'last30' | 'custom'
