<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Bell, DataLine, Grid, HomeFilled, Link as LinkIcon, User } from '@element-plus/icons-vue'
import { changePassword } from '@/api/auth'
import { useUserStore } from '@/stores/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const menus = [
  { name: 'overview', path: '/console/overview', label: '概览', icon: Grid },
  { name: 'links', path: '/console/links', label: '短链管理', icon: LinkIcon },
  { name: 'stats', path: '/console/stats', label: '数据统计', icon: DataLine }
]

const currentTitle = computed(() => (route.meta.title as string) || '控制台')
const nickname = computed(() => userStore.profile?.nickname || '用户')
const initial = computed(() => nickname.value.slice(-1))
const avatar = computed(() => userStore.profile?.avatar || '')
const phone = computed(() => {
  const value = userStore.profile?.phone || ''
  return value.length === 11 ? `${value.slice(0, 3)}****${value.slice(7)}` : value
})

const passwordVisible = ref(false)
const savingPassword = ref(false)
const passwordForm = reactive({ oldPassword: '', newPassword: '', confirmPassword: '' })

onMounted(() => {
  if (!userStore.profile) {
    userStore.loadProfile().catch(() => undefined)
  }
})

async function handleCommand(command: string) {
  if (command === 'password') {
    passwordForm.oldPassword = ''
    passwordForm.newPassword = ''
    passwordForm.confirmPassword = ''
    passwordVisible.value = true
    return
  }
  await handleLogout()
}

async function savePassword() {
  if (!passwordForm.oldPassword || !passwordForm.newPassword) {
    ElMessage.warning('请填写原密码和新密码')
    return
  }
  if (passwordForm.newPassword.length < 8) {
    ElMessage.warning('新密码至少 8 位')
    return
  }
  if (passwordForm.newPassword !== passwordForm.confirmPassword) {
    ElMessage.warning('两次输入的新密码不一致')
    return
  }
  savingPassword.value = true
  try {
    await changePassword(passwordForm.oldPassword, passwordForm.newPassword)
    ElMessage.success('密码已修改，请重新登录')
    passwordVisible.value = false
    await userStore.logout()
    router.replace('/login')
  } catch {
    // 拦截器已提示
  } finally {
    savingPassword.value = false
  }
}

async function handleLogout() {
  await userStore.logout()
  router.push('/login')
}
</script>

<template>
  <div class="app">
    <aside class="sidebar">
      <router-link to="/" class="brand">
        <span class="mark">S</span>
        ShortLink
      </router-link>

      <div class="label">工作台</div>
      <router-link
        v-for="item in menus"
        :key="item.name"
        :to="item.path"
        class="side-link"
        :class="{ active: route.name === item.name }"
      >
        <el-icon :size="16"><component :is="item.icon" /></el-icon>
        {{ item.label }}
      </router-link>

      <div class="label">账号</div>
      <router-link
        to="/console/profile"
        class="side-link"
        :class="{ active: route.name === 'profile' }"
      >
        <el-icon :size="16"><User /></el-icon>
        个人中心
      </router-link>
      <router-link to="/" class="side-link">
        <el-icon :size="16"><HomeFilled /></el-icon>
        返回首页
      </router-link>

      <div class="side-user">
        <span class="avatar">
          <img v-if="avatar" :src="avatar" alt="" />
          <template v-else>{{ initial }}</template>
        </span>
        <div class="meta">
          <div class="name">{{ nickname }}</div>
          <div class="phone num">{{ phone }}</div>
        </div>
      </div>
    </aside>

    <div class="main">
      <header class="topbar">
        <div class="crumb">控制台 / <b>{{ currentTitle }}</b></div>
        <div class="right">
          <el-icon :size="17" class="bell"><Bell /></el-icon>
          <el-dropdown trigger="click" @command="handleCommand">
            <span class="avatar">
              <img v-if="avatar" :src="avatar" alt="" />
              <template v-else>{{ initial }}</template>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="password">修改密码</el-dropdown-item>
                <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </header>

      <main class="content">
        <router-view />
      </main>
    </div>

    <el-dialog v-model="passwordVisible" title="修改密码" width="440px">
      <el-form label-position="top">
        <el-form-item label="原密码">
          <el-input v-model="passwordForm.oldPassword" type="password" show-password />
        </el-form-item>
        <el-form-item label="新密码">
          <el-input v-model="passwordForm.newPassword" type="password" show-password placeholder="8-32 位" />
        </el-form-item>
        <el-form-item label="确认新密码">
          <el-input v-model="passwordForm.confirmPassword" type="password" show-password />
        </el-form-item>
      </el-form>
      <div class="tip">修改密码后需要重新登录，其它设备的登录状态也会失效。</div>
      <template #footer>
        <el-button @click="passwordVisible = false">取消</el-button>
        <el-button type="primary" :loading="savingPassword" @click="savePassword">确认修改</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.app {
  display: flex;
  min-height: 100vh;
  background: var(--canvas);
}

.sidebar {
  position: sticky;
  top: 0;
  display: flex;
  flex: none;
  flex-direction: column;
  width: 224px;
  height: 100vh;
  padding: 16px 12px;
  border-right: 1px solid var(--line);
  background: var(--surface);
}

.brand {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 6px 8px 18px;
  font-size: 15px;
  font-weight: 650;
  color: var(--ink-900);
  transition: color 0.16s var(--ease);
}

.brand:hover {
  color: var(--brand);
}

.mark {
  display: grid;
  place-items: center;
  width: 24px;
  height: 24px;
  border-radius: 7px;
  background: linear-gradient(140deg, #3b82f6, #1d4ed8);
  color: #fff;
  font-size: 11px;
  font-weight: 700;
}

.label {
  padding: 10px 10px 6px;
  font-size: 11px;
  letter-spacing: 0.08em;
  text-transform: uppercase;
  color: var(--ink-400);
}

.side-link {
  display: flex;
  align-items: center;
  gap: 10px;
  height: 38px;
  padding: 0 10px;
  border-radius: var(--radius-sm);
  color: var(--ink-600);
  font-size: 13.5px;
  transition: background 0.16s var(--ease), color 0.16s var(--ease);
}

.side-link:hover {
  background: var(--sunken);
  color: var(--ink-900);
}

.side-link.active {
  background: var(--brand-weak);
  color: var(--brand);
  font-weight: 600;
}

.side-user {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-top: auto;
  padding: 10px;
  border: 1px solid var(--line);
  border-radius: 10px;
}

.avatar {
  display: grid;
  place-items: center;
  width: 30px;
  height: 30px;
  overflow: hidden;
  border-radius: 50%;
  background: linear-gradient(140deg, #60a5fa, #2563eb);
  color: #fff;
  font-size: 12px;
  font-weight: 600;
  cursor: pointer;
}

.avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.meta .name {
  font-size: 13px;
  font-weight: 550;
  color: var(--ink-900);
}

.meta .phone {
  font-size: 11.5px;
  color: var(--ink-400);
}

.main {
  flex: 1;
  min-width: 0;
}

.topbar {
  position: sticky;
  top: 0;
  z-index: 5;
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 56px;
  padding: 0 28px;
  border-bottom: 1px solid var(--line);
  background: var(--surface);
}

.crumb {
  font-size: 13px;
  color: var(--ink-400);
}

.crumb b {
  font-weight: 550;
  color: var(--ink-700);
}

.right {
  display: flex;
  align-items: center;
  gap: 16px;
}

.bell {
  color: var(--ink-400);
  cursor: pointer;
}

.tip {
  margin: 4px 0 0;
  font-size: 12px;
  color: var(--ink-400);
}

.content {
  width: 100%;
  padding: 24px 32px 56px;
}

@media (min-width: 2200px) {
  .sidebar {
    width: 252px;
    padding: 20px 14px;
  }

  .side-link {
    height: 42px;
    font-size: 14.5px;
  }

  .topbar {
    height: 62px;
    padding: 0 40px;
  }

  .content {
    padding: 28px 40px 72px;
  }
}
</style>
