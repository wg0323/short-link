<script setup lang="ts">
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Link, User } from '@element-plus/icons-vue'
import { useAdminStore } from '@/stores/admin'

const route = useRoute()
const router = useRouter()
const adminStore = useAdminStore()

const menus = [
  { name: 'users', path: '/users', label: '注册用户', icon: User },
  { name: 'domains', path: '/domains', label: '短链域名', icon: Link }
]

const title = computed(() => (route.meta.title as string) || '管理后台')
const initial = computed(() => adminStore.nickname.slice(-1) || '管')

function handleLogout() {
  adminStore.logout()
  router.replace('/login')
}
</script>

<template>
  <div class="app">
    <aside class="sidebar">
      <div class="brand">
        <span class="mark">S</span>
        ShortLink 管理后台
      </div>

      <div class="label">运营</div>
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

      <div class="side-user">
        <span class="avatar">{{ initial }}</span>
        <div class="meta">
          <div class="name">{{ adminStore.nickname }}</div>
          <div class="role">管理员</div>
        </div>
      </div>
    </aside>

    <div class="main">
      <header class="topbar">
        <div class="crumb">管理后台 / <b>{{ title }}</b></div>
        <el-button text @click="handleLogout">退出登录</el-button>
      </header>

      <main class="content">
        <router-view />
      </main>
    </div>
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
  width: 232px;
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
  font-size: 14.5px;
  font-weight: 650;
  color: var(--ink-900);
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
  border-radius: 50%;
  background: linear-gradient(140deg, #60a5fa, #2563eb);
  color: #fff;
  font-size: 12px;
  font-weight: 600;
}

.meta .name {
  font-size: 13px;
  font-weight: 550;
  color: var(--ink-900);
}

.meta .role {
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

.content {
  width: 100%;
  padding: 24px 32px 56px;
}

@media (min-width: 2200px) {
  .sidebar {
    width: 260px;
  }

  .content {
    padding: 28px 40px 72px;
  }
}
</style>
