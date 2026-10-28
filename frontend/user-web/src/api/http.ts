import axios, { type AxiosRequestConfig } from 'axios'
import { ElMessage } from 'element-plus'
import type { AuthResponse } from './types'

export interface ApiResult<T> {
  code: number
  message: string
  data: T
}

export const TOKEN_KEY = 'sl_token'

export const REFRESH_KEY = 'sl_refresh'

const http = axios.create({
  baseURL: '/api',
  timeout: 15000
})

http.interceptors.request.use((config) => {
  const token = localStorage.getItem(TOKEN_KEY)
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

export function clearSession() {
  localStorage.removeItem(TOKEN_KEY)
  localStorage.removeItem(REFRESH_KEY)
}

function backToLogin() {
  const current = window.location.pathname + window.location.search
  if (!current.startsWith('/login')) {
    window.location.assign(`/login?redirect=${encodeURIComponent(current)}`)
  }
}

/**
 * 并发 401 共用同一个刷新请求，避免一屏请求同时去刷新。
 */
let refreshing: Promise<string> | null = null

/**
 * 拿 refresh token 换新的 access token，同时把两个令牌写回本地。
 *
 * <p>走裸 axios 而非 http 实例：刷新接口自己返回 401 时不能再触发刷新，否则递归。</p>
 */
async function renewToken(): Promise<string> {
  const refreshToken = localStorage.getItem(REFRESH_KEY)
  if (!refreshToken) {
    throw new Error('缺少刷新令牌')
  }
  const response = await axios.post<ApiResult<AuthResponse>>('/api/auth/refresh', { refreshToken })
  const body = response.data
  if (body.code !== 0) {
    throw new Error(body.message)
  }
  localStorage.setItem(TOKEN_KEY, body.data.accessToken)
  localStorage.setItem(REFRESH_KEY, body.data.refreshToken)
  return body.data.accessToken
}

type RetryableConfig = AxiosRequestConfig & { retried?: boolean }

http.interceptors.response.use(
  (response) => response,
  async (error) => {
    const status = error.response?.status
    const config = error.config as RetryableConfig | undefined

    if (status === 401) {
      const refreshItself = Boolean(config?.url?.includes('/auth/refresh'))
      if (config && !config.retried && !refreshItself) {
        config.retried = true
        try {
          if (!refreshing) {
            refreshing = renewToken().finally(() => {
              refreshing = null
            })
          }
          const accessToken = await refreshing
          config.headers = {
            ...config.headers,
            Authorization: `Bearer ${accessToken}`
          } as AxiosRequestConfig['headers']
          return http.request(config)
        } catch {
          // 刷新失败，走下面的清会话逻辑
        }
      }
      clearSession()
      backToLogin()
      return Promise.reject(error)
    }

    const message: string = error.response?.data?.message || '网络异常，请稍后再试'
    ElMessage.error(message)
    return Promise.reject(error)
  }
)

/**
 * 统一解包 {code, message, data}，业务码非 0 时提示并抛出。
 */
export async function request<T>(config: AxiosRequestConfig): Promise<T> {
  const response = await http.request<ApiResult<T>>(config)
  const body = response.data
  if (body.code !== 0) {
    ElMessage.error(body.message)
    throw new Error(body.message)
  }
  return body.data
}
