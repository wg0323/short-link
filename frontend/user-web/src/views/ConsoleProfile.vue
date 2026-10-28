<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { fetchQuota, updateNickname, uploadAvatar } from '@/api/auth'
import type { QuotaOverview } from '@/api/types'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()

const uploading = ref(false)
const nicknameVisible = ref(false)
const savingName = ref(false)
const nicknameForm = reactive({ nickname: '' })
const fileInput = ref<HTMLInputElement>()
const quota = ref<QuotaOverview | null>(null)

const profile = computed(() => userStore.profile)
const initial = computed(() => (profile.value?.nickname || '用').slice(-1))
const dailyPercent = computed(() => {
  const limit = quota.value?.dailyGenerate
  if (!limit) {
    return null
  }
  return Math.min(100, Math.round(((quota.value?.dailyGenerated ?? 0) / limit) * 100))
})

onMounted(async () => {
  userStore.loadProfile().catch(() => undefined)
  quota.value = (await fetchQuota().catch(() => null)) ?? null
})

function limitText(value?: number | null) {
  return value == null ? '不限' : value.toLocaleString()
}

function openNicknameDialog() {
  nicknameForm.nickname = profile.value?.nickname || ''
  nicknameVisible.value = true
}

async function saveNickname() {
  const value = nicknameForm.nickname.trim()
  if (!value) {
    ElMessage.warning('昵称不能为空')
    return
  }
  savingName.value = true
  try {
    await updateNickname(value)
    await userStore.loadProfile()
    nicknameVisible.value = false
    ElMessage.success('昵称已更新')
  } catch {
    // 拦截器已提示
  } finally {
    savingName.value = false
  }
}

function pickAvatar() {
  fileInput.value?.click()
}

async function onAvatarChange(event: Event) {
  const input = event.target as HTMLInputElement
  const file = input.files?.[0]
  // 清空以允许重复选择同一张图片
  input.value = ''
  if (!file) {
    return
  }
  if (!file.type.startsWith('image/')) {
    ElMessage.warning('请选择图片文件')
    return
  }
  if (file.size > 2 * 1024 * 1024) {
    ElMessage.warning('头像不能超过 2MB')
    return
  }
  uploading.value = true
  try {
    userStore.profile = await uploadAvatar(file)
    ElMessage.success('头像已更新')
  } catch {
    // 拦截器已提示
  } finally {
    uploading.value = false
  }
}
</script>

<template>
  <div>
    <div class="sl-page-head">
      <div>
        <h1>个人中心</h1>
        <p class="sub">账号基础信息与头像设置</p>
      </div>
    </div>

    <section class="sl-card block">
      <h2>基本信息</h2>

      <div class="row avatar-row">
        <span class="label">头像</span>
        <div class="avatar-cell">
          <span class="avatar">
            <img v-if="profile?.avatar" :src="profile.avatar" alt="" />
            <template v-else>{{ initial }}</template>
          </span>
          <el-button text type="primary" :loading="uploading" @click="pickAvatar">更换头像</el-button>
          <input ref="fileInput" type="file" accept="image/*" hidden @change="onAvatarChange" />
        </div>
      </div>

      <div class="row">
        <span class="label">用户名称</span>
        <div class="value">
          {{ profile?.nickname || '—' }}
          <el-button text type="primary" @click="openNicknameDialog">修改</el-button>
        </div>
      </div>
      <div class="row">
        <span class="label">注册手机号</span>
        <span class="value num">{{ profile?.phone || '—' }}</span>
      </div>
      <div class="row">
        <span class="label">注册时间</span>
        <span class="value num">{{ profile?.createdAt || '—' }}</span>
      </div>
    </section>

    <section v-if="quota" class="sl-card block quota">
      <h2>套餐与用量</h2>
      <div class="row">
        <span class="label">当前套餐</span>
        <span class="value">{{ quota.packageName || quota.packageCode }}</span>
      </div>
      <div class="row">
        <span class="label">今日新建</span>
        <span class="value num">{{ quota.dailyGenerated }} / {{ limitText(quota.dailyGenerate) }}</span>
      </div>
      <el-progress
        v-if="dailyPercent !== null"
        class="bar"
        :percentage="dailyPercent"
        :show-text="false"
        :stroke-width="6"
      />
      <div class="row">
        <span class="label">累计新建</span>
        <span class="value num">{{ quota.usedGenerate }} / {{ limitText(quota.totalGenerate) }}</span>
      </div>
      <div class="row">
        <span class="label">累计访问</span>
        <span class="value num">{{ quota.usedClick.toLocaleString() }} / {{ limitText(quota.totalClick) }}</span>
      </div>
    </section>

    <el-dialog v-model="nicknameVisible" title="修改昵称" width="420px">
      <el-form label-position="top">
        <el-form-item label="昵称">
          <el-input v-model="nicknameForm.nickname" maxlength="20" show-word-limit placeholder="请输入昵称" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="nicknameVisible = false">取消</el-button>
        <el-button type="primary" :loading="savingName" @click="saveNickname">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.block {
  max-width: 860px;
  padding: 20px 22px;
}

.quota {
  margin-top: 16px;
}

.bar {
  margin: 2px 0 6px;
}

h2 {
  margin: 0 0 12px;
  font-size: 14px;
  font-weight: 600;
  color: var(--ink-900);
}

.row {
  display: flex;
  justify-content: space-between;
  padding: 10px 0;
  border-bottom: 1px solid var(--line-soft);
  font-size: 13.5px;
}

.row:last-of-type {
  border-bottom: none;
}

.avatar-row {
  align-items: center;
}

.label {
  color: var(--ink-400);
}

.value {
  display: flex;
  align-items: center;
  gap: 2px;
  color: var(--ink-900);
}

.avatar-cell {
  display: flex;
  align-items: center;
  gap: 10px;
}

.avatar {
  display: grid;
  place-items: center;
  width: 44px;
  height: 44px;
  overflow: hidden;
  border-radius: 50%;
  background: linear-gradient(140deg, #60a5fa, #2563eb);
  color: #fff;
  font-size: 15px;
  font-weight: 600;
}

.avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
</style>
