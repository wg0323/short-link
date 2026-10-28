<script setup lang="ts">
import { computed, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Link as LinkIcon } from '@element-plus/icons-vue'
import { createLink } from '@/api/link'
import type { CreateLinkResult } from '@/api/types'
import { TOKEN_KEY } from '@/api/http'

const router = useRouter()
const originUrl = ref('')
const loading = ref(false)
const result = ref<CreateLinkResult | null>(null)

const loggedIn = computed(() => Boolean(localStorage.getItem(TOKEN_KEY)))

const features = [
  {
    title: '实时统计',
    desc: '每一条短链的 PV、UV、独立 IP 与地域分布，点击后立刻可见，支持自定义时间区间。'
  },
  {
    title: '访问控制',
    desc: '访问密码、到期时间、生效时间窗三重约束，过期或未生效的链接立即失效。'
  },
  {
    title: '高并发跳转',
    desc: '本地缓存 + Redis 两级缓存与布隆过滤器，跳转链路无状态，可水平扩容到多实例。'
  }
]

async function handleGenerate() {
  if (!loggedIn.value) {
    router.push({ name: 'login', query: { redirect: '/' } })
    return
  }
  const url = originUrl.value.trim()
  if (!url) {
    ElMessage.warning('请先粘贴需要缩短的链接')
    return
  }
  loading.value = true
  try {
    result.value = await createLink({ originUrl: url })
    ElMessage.success(result.value.duplicated ? '该链接已存在，已为你返回原短链' : '短链生成成功')
  } catch {
    // 错误提示由 http 拦截器统一处理
  } finally {
    loading.value = false
  }
}

async function copyShortUrl() {
  if (!result.value) {
    return
  }
  try {
    await navigator.clipboard.writeText(result.value.shortUrl)
    ElMessage.success('已复制到剪贴板')
  } catch {
    ElMessage.warning('复制失败，请手动选择文本')
  }
}
</script>

<template>
  <div class="home">
    <div class="hero-wrap">
      <header class="nav">
        <div class="brand">
          <span class="mark">S</span>
          ShortLink
        </div>
        <div class="actions">
          <router-link v-if="!loggedIn" to="/login" class="ghost">登录</router-link>
          <router-link :to="loggedIn ? '/console' : '/register'" class="primary">
            {{ loggedIn ? '进入控制台' : '免费注册' }}
          </router-link>
        </div>
      </header>

      <section class="hero">
        <span class="eyebrow"><span class="dot"></span>支持两级缓存与实时统计的短链服务</span>
        <h1>把长链接，<em>变短一点</em>。</h1>
        <p class="sub">粘贴链接即可生成短链。每一次点击的来源、地域与设备，都会实时进入你的数据面板。</p>

        <div class="generator">
          <div class="field">
            <el-icon :size="18" class="link-icon"><LinkIcon /></el-icon>
            <input
              v-model="originUrl"
              placeholder="https://example.com/path/to/a/very/long/link?utm_source=..."
              @keyup.enter="handleGenerate"
            />
            <el-button type="primary" size="large" :loading="loading" @click="handleGenerate">
              生成短链
            </el-button>
          </div>
          <p class="hint">
            {{
              loggedIn
                ? '生成后可保存、管理与统计，全部在控制台完成'
                : '登录后可保存、管理与统计短链 · 还没有账号？'
            }}
            <router-link v-if="!loggedIn" to="/register" class="hint-link">免费注册</router-link>
          </p>
        </div>

        <div v-if="result" class="result">
          <span class="result-label">短链</span>
          <a class="result-url" :href="result.shortUrl" target="_blank" rel="noopener">
            {{ result.shortUrl }}
          </a>
          <el-button text type="primary" @click="copyShortUrl">复制</el-button>
          <el-button text @click="router.push('/console/links')">去管理</el-button>
        </div>

        <div class="stats">
          <div class="item"><span class="k num">7 位</span><span class="v">定长短码</span></div>
          <div class="item"><span class="k num">两级</span><span class="v">缓存架构</span></div>
          <div class="item"><span class="k num">302</span><span class="v">可统计跳转</span></div>
        </div>
      </section>
    </div>

    <section id="features" class="features">
      <div class="grid">
        <article v-for="item in features" :key="item.title" class="card">
          <h3>{{ item.title }}</h3>
          <p>{{ item.desc }}</p>
        </article>
      </div>
      <footer class="footer">
        <span>© 2026 ShortLink</span>
        <span class="muted">短链与点击数据分析</span>
      </footer>
    </section>
  </div>
</template>

<style scoped>
.home {
  min-height: 100vh;
  background: var(--canvas);
}

.hero-wrap {
  position: relative;
  overflow: hidden;
  padding-bottom: 96px;
  background: var(--ink-900);
}

.hero-wrap::before {
  content: '';
  position: absolute;
  top: -340px;
  left: 50%;
  width: 1200px;
  height: 900px;
  transform: translateX(-50%);
  background: radial-gradient(closest-side, rgba(37, 99, 235, 0.34), transparent 72%);
  pointer-events: none;
}

.nav {
  position: relative;
  z-index: 2;
  display: flex;
  align-items: center;
  justify-content: space-between;
  max-width: 1240px;
  height: 68px;
  margin: 0 auto;
  padding: 0 40px;
}

.brand {
  display: flex;
  align-items: center;
  gap: 10px;
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
  color: #fff;
  font-size: 12px;
  font-weight: 700;
}

.actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.ghost {
  color: #e2e8f0;
  font-size: 13.5px;
}

.primary {
  display: inline-flex;
  align-items: center;
  height: 34px;
  padding: 0 16px;
  border-radius: var(--radius-sm);
  background: var(--brand);
  color: #fff;
  font-size: 13.5px;
  font-weight: 550;
  transition: background 0.18s var(--ease);
}

.primary:hover {
  background: var(--brand-hover);
}

.hero {
  position: relative;
  z-index: 1;
  max-width: 900px;
  margin: 0 auto;
  padding: 72px 24px 0;
  text-align: center;
}

.eyebrow {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  height: 28px;
  padding: 0 12px;
  border: 1px solid rgba(255, 255, 255, 0.12);
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.04);
  color: var(--ink-300);
  font-size: 12px;
}

.dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: #22c55e;
}

h1 {
  margin: 26px 0 0;
  color: #fff;
  font-size: 58px;
  font-weight: 700;
  line-height: 1.14;
  letter-spacing: -0.035em;
}

h1 em {
  font-style: normal;
  color: #93b4ff;
}

.sub {
  max-width: 620px;
  margin: 20px auto 0;
  color: var(--ink-400);
  font-size: 15.5px;
  line-height: 1.72;
}

.generator {
  max-width: 720px;
  margin: 40px auto 0;
  padding: 10px;
  border-radius: var(--radius-lg);
  background: #fff;
  box-shadow: var(--shadow-3);
  text-align: left;
}

.field {
  display: flex;
  align-items: center;
  gap: 8px;
  height: 56px;
  padding: 0 6px 0 16px;
}

.link-icon {
  color: var(--ink-400);
}

.field input {
  flex: 1;
  min-width: 0;
  height: 100%;
  border: none;
  outline: none;
  background: transparent;
  font-size: 15px;
  color: var(--ink-900);
}

.field input::placeholder {
  color: var(--ink-300);
}

.hint {
  padding: 8px 16px 6px;
  margin: 0;
  font-size: 12.5px;
  color: var(--ink-400);
}

.hint-link {
  color: var(--brand);
  font-weight: 600;
}

.result {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  max-width: 720px;
  margin: 16px auto 0;
  padding: 12px 16px;
  border: 1px solid rgba(255, 255, 255, 0.12);
  border-radius: var(--radius-md);
  background: rgba(255, 255, 255, 0.06);
  color: #fff;
  font-size: 13.5px;
}

.result-label {
  color: var(--ink-400);
}

.result-url {
  color: #93b4ff;
  font-family: 'JetBrains Mono', ui-monospace, monospace;
}

.stats {
  display: flex;
  max-width: 760px;
  margin: 56px auto 0;
}

.stats .item {
  flex: 1;
  padding: 0 20px;
  text-align: center;
  border-right: 1px solid rgba(255, 255, 255, 0.08);
}

.stats .item:last-child {
  border-right: none;
}

.stats .k {
  display: block;
  color: #fff;
  font-size: 26px;
  font-weight: 650;
}

.stats .v {
  display: block;
  margin-top: 6px;
  color: #64748b;
  font-size: 11.5px;
  letter-spacing: 0.1em;
  text-transform: uppercase;
}

.features {
  padding: 64px 24px 40px;
}

.grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
  max-width: 1000px;
  margin: 0 auto;
}

.card {
  padding: 22px;
  border: 1px solid var(--line);
  border-radius: var(--radius-md);
  background: var(--surface);
}

.card h3 {
  margin: 0;
  font-size: 14.5px;
  font-weight: 600;
  color: var(--ink-900);
}

.card p {
  margin: 8px 0 0;
  font-size: 13px;
  line-height: 1.68;
  color: var(--ink-600);
}

.footer {
  display: flex;
  justify-content: space-between;
  max-width: 1000px;
  margin: 56px auto 0;
  padding-top: 24px;
  border-top: 1px solid var(--line);
  font-size: 12.5px;
  color: var(--ink-400);
}

@media (max-width: 900px) {
  .grid {
    grid-template-columns: 1fr;
  }

  h1 {
    font-size: 40px;
  }

  .nav {
    padding: 0 20px;
  }
}
</style>
