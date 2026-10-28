import { defineStore } from 'pinia'
import { ref } from 'vue'
import { TOKEN_KEY, login as loginApi } from '@/api/admin'

export const useAdminStore = defineStore('admin', () => {
  const token = ref<string>(localStorage.getItem(TOKEN_KEY) || '')
  const nickname = ref<string>(localStorage.getItem('sl_admin_name') || '管理员')

  async function login(username: string, password: string, captchaId: string, captchaCode: string) {
    const result = await loginApi({ username, password, captchaId, captchaCode })
    token.value = result.token
    nickname.value = result.nickname || result.username
    localStorage.setItem(TOKEN_KEY, result.token)
    localStorage.setItem('sl_admin_name', nickname.value)
    return result
  }

  function logout() {
    token.value = ''
    localStorage.removeItem(TOKEN_KEY)
    localStorage.removeItem('sl_admin_name')
  }

  return { token, nickname, login, logout }
})
