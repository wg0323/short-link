import axios, { type AxiosRequestConfig } from 'axios'
import { ElMessage } from 'element-plus'

export const TOKEN_KEY = 'sl_admin_token'

interface ApiResult<T> {
  code: number
  message: string
  data: T
}

export interface Captcha {
  captchaId: string
  image: string
}

export interface AdminAuthResult {
  token: string
  expiresIn: number
  username: string
  nickname: string
}

export interface PlatformUser {
  id: number
  phone: string
  nickname: string
  status: number
  linkCount: number
  lastLoginAt?: string
  createdAt: string
}

export interface DomainItem {
  id: number
  domain: string
  defaultFlag: boolean
  status: number
  linkCount: number
  createdAt: string
}

export interface PageVO<T> {
  total: number
  page: number
  size: number
  records: T[]
}

const http = axios.create({ baseURL: '', timeout: 15000 })

http.interceptors.request.use((config) => {
  const token = localStorage.getItem(TOKEN_KEY)
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

http.interceptors.response.use(
  (response) => response,
  (error) => {
    const status = error.response?.status
    const message: string = error.response?.data?.message || '网络异常，请稍后再试'
    if (status === 401) {
      localStorage.removeItem(TOKEN_KEY)
      if (!window.location.pathname.startsWith('/login')) {
        window.location.assign('/login')
      }
    } else {
      ElMessage.error(message)
    }
    return Promise.reject(error)
  }
)

async function request<T>(config: AxiosRequestConfig): Promise<T> {
  const response = await http.request<ApiResult<T>>(config)
  const body = response.data
  if (body.code !== 0) {
    ElMessage.error(body.message)
    throw new Error(body.message)
  }
  return body.data
}

export function fetchCaptcha() {
  return request<Captcha>({ url: '/admin/auth/captcha' })
}

export function login(payload: {
  username: string
  password: string
  captchaId: string
  captchaCode: string
}) {
  return request<AdminAuthResult>({ url: '/admin/auth/login', method: 'post', data: payload })
}

export function pageUsers(params: {
  keyword?: string
  status?: number
  page: number
  size: number
}) {
  return request<PageVO<PlatformUser>>({ url: '/admin/user/page', params })
}

export function updateUserStatus(id: number, status: number) {
  return request<void>({ url: `/admin/user/${id}/status`, method: 'put', params: { status } })
}

export function resetUserPassword(id: number, password: string) {
  return request<void>({ url: `/admin/user/${id}/password`, method: 'put', data: { password } })
}

export function listDomains() {
  return request<DomainItem[]>({ url: '/admin/domain' })
}

export function createDomain(domain: string, defaultFlag: boolean) {
  return request<void>({ url: '/admin/domain', method: 'post', data: { domain, defaultFlag } })
}

export function updateDomainStatus(id: number, status: number) {
  return request<void>({ url: `/admin/domain/${id}/status`, method: 'put', params: { status } })
}

export function deleteDomain(id: number) {
  return request<void>({ url: `/admin/domain/${id}`, method: 'delete' })
}
