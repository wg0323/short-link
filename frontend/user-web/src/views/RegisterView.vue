<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { register as registerApi, sendSms } from '@/api/auth'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()

const formRef = ref<FormInstance>()
const loading = ref(false)
const sending = ref(false)
const countdown = ref(0)
const form = reactive({ phone: '', code: '', password: '', nickname: '' })

const rules: FormRules = {
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }
  ],
  code: [
    { required: true, message: '请输入验证码', trigger: 'blur' },
    { pattern: /^\d{6}$/, message: '验证码为 6 位数字', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 8, max: 32, message: '密码长度需为 8-32 位', trigger: 'blur' }
  ]
}

async function handleSendSms() {
  const phoneValid = await formRef.value?.validateField('phone').catch(() => false)
  if (!phoneValid) {
    return
  }
  sending.value = true
  try {
    const result = await sendSms(form.phone)
    if (result.mockCode) {
      form.code = result.mockCode
      ElMessage.success(`开发模式：验证码已自动填入 ${result.mockCode}`)
    } else {
      ElMessage.success('验证码已发送')
    }
    countdown.value = 60
    const timer = window.setInterval(() => {
      countdown.value -= 1
      if (countdown.value <= 0) {
        window.clearInterval(timer)
      }
    }, 1000)
  } catch {
    // 错误提示由拦截器处理
  } finally {
    sending.value = false
  }
}

async function submit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) {
    return
  }
  loading.value = true
  try {
    const result = await registerApi({
      phone: form.phone,
      code: form.code,
      password: form.password,
      nickname: form.nickname || undefined
    })
    userStore.setSession(result.accessToken, result.refreshToken)
    ElMessage.success('注册成功')
    router.replace('/console/overview')
  } catch {
    // 错误提示由拦截器处理
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="auth">
    <div class="panel">
      <router-link to="/" class="brand">
        <span class="mark">S</span>
        ShortLink
      </router-link>

      <div class="card">
        <h1>创建账号</h1>
        <p class="sub">手机号注册，注册后立即可用</p>

        <el-form ref="formRef" :model="form" :rules="rules" label-position="top" @submit.prevent="submit">
          <el-form-item label="手机号" prop="phone">
            <el-input v-model="form.phone" size="large" placeholder="请输入手机号" maxlength="11" />
          </el-form-item>

          <el-form-item label="验证码" prop="code">
            <div class="code-row">
              <el-input v-model="form.code" size="large" placeholder="6 位验证码" maxlength="6" />
              <el-button
                size="large"
                :loading="sending"
                :disabled="countdown > 0"
                @click="handleSendSms"
              >
                {{ countdown > 0 ? `${countdown}s 后重发` : '获取验证码' }}
              </el-button>
            </div>
          </el-form-item>

          <el-form-item label="密码" prop="password">
            <el-input
              v-model="form.password"
              type="password"
              size="large"
              show-password
              placeholder="8-32 位密码"
            />
          </el-form-item>

          <el-form-item label="昵称（选填）">
            <el-input v-model="form.nickname" size="large" placeholder="不填则使用手机号后四位" maxlength="20" />
          </el-form-item>

          <el-button type="primary" size="large" class="submit" :loading="loading" @click="submit">
            注册并登录
          </el-button>
        </el-form>

        <p class="foot">
          已有账号？
          <router-link to="/login" class="link">直接登录</router-link>
        </p>
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
  max-width: 440px;
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
  padding: 32px 28px 28px;
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

.code-row {
  display: flex;
  gap: 10px;
  width: 100%;
}

.code-row .el-input {
  flex: 1;
}

.submit {
  width: 100%;
  margin-top: 8px;
}

.foot {
  margin: 18px 0 0;
  font-size: 13px;
  color: var(--ink-400);
  text-align: center;
}

.link {
  color: var(--brand);
  font-weight: 550;
}
</style>
