<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { fetchCaptcha, type Captcha } from '@/api/admin'
import { useAdminStore } from '@/stores/admin'

const router = useRouter()
const adminStore = useAdminStore()

const loading = ref(false)
const captcha = ref<Captcha>({ captchaId: '', image: '' })
const form = reactive({ username: 'admin', password: '', captchaCode: '' })

onMounted(refreshCaptcha)

async function refreshCaptcha() {
  captcha.value = await fetchCaptcha()
  form.captchaCode = ''
}

async function submit() {
  if (!form.username || !form.password || !form.captchaCode) {
    ElMessage.warning('请填写账号、密码和验证码')
    return
  }
  loading.value = true
  try {
    await adminStore.login(form.username, form.password, captcha.value.captchaId, form.captchaCode)
    ElMessage.success('登录成功')
    router.replace('/users')
  } catch {
    await refreshCaptcha()
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="auth">
    <div class="panel">
      <div class="brand">
        <span class="mark">S</span>
        ShortLink 管理后台
      </div>

      <div class="card">
        <h1>管理员登录</h1>
        <p class="sub">该入口仅供内部使用，请勿对外暴露</p>

        <el-form label-position="top" @submit.prevent="submit">
          <el-form-item label="账号">
            <el-input v-model="form.username" size="large" placeholder="管理员账号" />
          </el-form-item>
          <el-form-item label="密码">
            <el-input
              v-model="form.password"
              type="password"
              size="large"
              show-password
              placeholder="登录密码"
            />
          </el-form-item>
          <el-form-item label="图形验证码">
            <div class="captcha-row">
              <el-input
                v-model="form.captchaCode"
                size="large"
                maxlength="4"
                placeholder="4 位验证码"
                @keyup.enter="submit"
              />
              <img
                v-if="captcha.image"
                :src="captcha.image"
                alt="验证码"
                class="captcha"
                title="点击刷新"
                @click="refreshCaptcha"
              />
            </div>
          </el-form-item>
          <el-button type="primary" size="large" class="submit" :loading="loading" @click="submit">
            登录
          </el-button>
        </el-form>

        <p class="foot">默认账号见 application.yml 的 shortlink.admin 配置</p>
      </div>
    </div>
  </div>
</template>

<style scoped>
.auth {
  display: grid;
  place-items: center;
  min-height: 100vh;
  padding: 24px;
  background: var(--ink-900);
}

.panel {
  width: 100%;
  max-width: 420px;
}

.brand {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  margin-bottom: 28px;
  color: #fff;
  font-size: 16px;
  font-weight: 650;
}

.mark {
  display: grid;
  place-items: center;
  width: 26px;
  height: 26px;
  border-radius: 8px;
  background: linear-gradient(140deg, #3b82f6, #1d4ed8);
  font-size: 12px;
  font-weight: 700;
}

.card {
  padding: 32px 28px 26px;
  border-radius: var(--radius-lg);
  background: #fff;
  box-shadow: var(--shadow-3);
}

h1 {
  margin: 0;
  font-size: 22px;
  font-weight: 650;
  letter-spacing: -0.02em;
  color: var(--ink-900);
}

.sub {
  margin: 8px 0 24px;
  font-size: 13px;
  color: var(--ink-400);
}

.captcha-row {
  display: flex;
  gap: 10px;
  width: 100%;
}

.captcha-row .el-input {
  flex: 1;
}

.captcha {
  width: 120px;
  height: 40px;
  border: 1px solid var(--line);
  border-radius: var(--radius-sm);
  cursor: pointer;
  object-fit: cover;
}

.submit {
  width: 100%;
  margin-top: 8px;
}

.foot {
  margin: 18px 0 0;
  font-size: 12px;
  color: var(--ink-400);
  text-align: center;
}
</style>
