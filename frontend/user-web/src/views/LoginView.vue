<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { useUserStore } from '@/stores/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const formRef = ref<FormInstance>()
const loading = ref(false)
const form = reactive({ phone: '', password: '' })

const rules: FormRules = {
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }
  ],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

async function submit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) {
    return
  }
  loading.value = true
  try {
    await userStore.login(form.phone, form.password)
    ElMessage.success('登录成功')
    const redirect = (route.query.redirect as string) || '/console/overview'
    router.replace(redirect)
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
        <h1>欢迎回来</h1>
        <p class="sub">登录后管理你的短链与访问数据</p>

        <el-form ref="formRef" :model="form" :rules="rules" label-position="top" @submit.prevent="submit">
          <el-form-item label="手机号" prop="phone">
            <el-input v-model="form.phone" size="large" placeholder="请输入手机号" maxlength="11" />
          </el-form-item>
          <el-form-item label="密码" prop="password">
            <el-input
              v-model="form.password"
              type="password"
              size="large"
              show-password
              placeholder="请输入密码"
              @keyup.enter="submit"
            />
          </el-form-item>
          <el-button type="primary" size="large" class="submit" :loading="loading" @click="submit">
            登录
          </el-button>
        </el-form>

        <p class="foot">
          还没有账号？
          <router-link to="/register" class="link">免费注册</router-link>
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
