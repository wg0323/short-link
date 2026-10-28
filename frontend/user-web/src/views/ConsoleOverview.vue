<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { createLink, pageLinks } from '@/api/link'
import { fetchOverview } from '@/api/stat'
import type { CreateLinkResult, LinkItem, StatOverview } from '@/api/types'

const router = useRouter()
const overview = ref<StatOverview>({ pv: 0, uv: 0, ipCount: 0, linkCount: 0 })
const recent = ref<LinkItem[]>([])
const originUrl = ref('')
const creating = ref(false)
const created = ref<CreateLinkResult | null>(null)
const loading = ref(true)

const cards = [
  { key: 'pv', label: '今日访问量 PV' },
  { key: 'uv', label: '今日独立访客 UV' },
  { key: 'ipCount', label: '今日独立 IP' },
  { key: 'linkCount', label: '短链总数' }
] as const

onMounted(load)

async function load() {
  loading.value = true
  try {
    const [stat, page] = await Promise.all([
      fetchOverview({ range: 'today' }),
      pageLinks({ page: 1, size: 5 })
    ])
    overview.value = stat
    recent.value = page.records
  } finally {
    loading.value = false
  }
}

async function handleCreate() {
  const url = originUrl.value.trim()
  if (!url) {
    ElMessage.warning('请先粘贴需要缩短的链接')
    return
  }
  creating.value = true
  try {
    created.value = await createLink({ originUrl: url })
    originUrl.value = ''
    ElMessage.success(created.value.duplicated ? '该链接已存在，已返回原短链' : '生成成功')
    await load()
  } catch {
    // 拦截器已提示
  } finally {
    creating.value = false
  }
}

async function copy(text: string) {
  try {
    await navigator.clipboard.writeText(text)
    ElMessage.success('已复制')
  } catch {
    ElMessage.warning('复制失败，请手动选择')
  }
}
</script>

<template>
  <div v-loading="loading">
    <div class="sl-page-head">
      <div>
        <h1>概览</h1>
        <p class="sub">今天的访问情况与最近的短链</p>
      </div>
      <el-button type="primary" @click="router.push('/console/links')">管理短链</el-button>
    </div>

    <div class="kpi-grid">
      <div v-for="card in cards" :key="card.key" class="kpi sl-card">
        <div class="label">{{ card.label }}</div>
        <div class="value num">{{ overview[card.key].toLocaleString() }}</div>
      </div>
    </div>

    <section class="sl-card quick">
      <h2>快速生成</h2>
      <div class="row">
        <el-input
          v-model="originUrl"
          size="large"
          placeholder="粘贴长链接，例如 https://example.com/campaign"
          @keyup.enter="handleCreate"
        />
        <el-button type="primary" size="large" :loading="creating" @click="handleCreate">
          生成短链
        </el-button>
      </div>
      <div v-if="created" class="created">
        <span class="muted">短链</span>
        <a :href="created.shortUrl" target="_blank" rel="noopener">{{ created.shortUrl }}</a>
        <el-button text type="primary" @click="copy(created.shortUrl)">复制</el-button>
      </div>
    </section>

    <section class="sl-card recent">
      <div class="head">
        <h2>最近短链</h2>
        <el-button text type="primary" @click="router.push('/console/links')">查看全部</el-button>
      </div>
      <el-table v-if="recent.length" :data="recent" style="width: 100%">
        <el-table-column prop="shortUrl" label="短链" min-width="240">
          <template #default="{ row }">
            <span class="mono">{{ row.shortUrl }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="备注名" min-width="160">
          <template #default="{ row }">{{ row.remark || '—' }}</template>
        </el-table-column>
        <el-table-column prop="clickCount" label="访问量" width="120" align="right">
          <template #default="{ row }">
            <span class="num">{{ row.clickCount.toLocaleString() }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="180" />
      </el-table>
      <el-empty v-else description="还没有短链，先在上方生成一个" />
    </section>
  </div>
</template>

<style scoped>
.kpi-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 16px;
}

.kpi {
  padding: 16px 18px;
}

.kpi .label {
  font-size: 12px;
  color: var(--ink-400);
}

.kpi .value {
  margin-top: 8px;
  font-size: 28px;
  font-weight: 650;
  letter-spacing: -0.02em;
  color: var(--ink-900);
}

.quick {
  margin-top: 16px;
  padding: 18px;
}

.quick h2,
.recent h2 {
  margin: 0;
  font-size: 14px;
  font-weight: 600;
  color: var(--ink-900);
}

.row {
  display: flex;
  gap: 12px;
  margin-top: 14px;
}

.created {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-top: 12px;
  font-size: 13px;
}

.created a {
  color: var(--brand);
  font-family: 'JetBrains Mono', ui-monospace, monospace;
}

.muted {
  color: var(--ink-400);
}

.recent {
  margin-top: 16px;
  padding: 18px 18px 6px;
}

.head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 6px;
}

.mono {
  font-family: 'JetBrains Mono', ui-monospace, monospace;
  font-size: 12.5px;
}

@media (max-width: 1200px) {
  .kpi-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}
</style>
