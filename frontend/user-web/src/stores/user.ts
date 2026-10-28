import { defineStore } from 'pinia'
import { ref } from 'vue'
import { fetchProfile, login as loginApi, logout as logoutApi } from '@/api/auth'
import { clearSession, REFRESH_KEY, TOKEN_KEY } from '@/api/http'
import type { UserProfile } from '@/api/types'

export const useUserStore = defineStore('user', () => {
  const profile = ref<UserProfile | null>(null)

  function setSession(accessToken: string, refreshToken: string) {
    localStorage.setItem(TOKEN_KEY, accessToken)
    localStorage.setItem(REFRESH_KEY, refreshToken)
  }

  async function login(phone: string, password: string) {
    const result = await loginApi({ phone, password })
    setSession(result.accessToken, result.refreshToken)
    profile.value = result.user
    return result
  }

  async function loadProfile() {
    // 令牌以 localStorage 为准，401 自动刷新时这里能读到最新的 access token
    if (!localStorage.getItem(TOKEN_KEY)) {
      return null
    }
    profile.value = await fetchProfile()
    return profile.value
  }

  async function logout() {
    try {
      await logoutApi()
    } catch {
      // 登出失败不阻塞前端清理
    }
    profile.value = null
    clearSession()
  }

  return { profile, setSession, login, loadProfile, logout }
})
