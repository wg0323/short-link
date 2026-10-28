<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  createDomain,
  deleteDomain,
  listDomains,
  updateDomainStatus,
  type DomainItem
} from '@/api/admin'

const loading = ref(false)
const rows = ref<DomainItem[]>([])

const createVisible = ref(false)
const creating = ref(false)
const form = reactive({ domain: '', defaultFlag: false })

onMounted(load)

async function load() {
  loading.value = true
  try {
    rows.value = await listDomains()
  } finally {
    loading.value = false
  }
}

function openCreate() {
  form.domain = ''
  form.defaultFlag = false
  createVisible.value = true
}

async function submitCreate() {
  if (!form.domain.trim()) {
    ElMessage.warning('请输入域名')
    return
  }
  creating.value = true
  try {
    await createDomain(form.domain.trim(), form.defaultFlag)
    createVisible.value = false
    ElMessage.success('域名已添加')
    await load()
  } catch {
    // 拦截器已提示
  } finally {
    creating.value = false
  }
}

async function toggleStatus(row: DomainItem) {
  const next = row.status === 1 ? 0 : 1
  await updateDomainStatus(row.id, next)
  ElMessage.success(next === 1 ? '已启用' : '已停用')
  await load()
}

async function remove(row: DomainItem) {
  await ElMessageBox.confirm(
    `确定删除域名「${row.domain}」吗？该域名下不能存在未删除的短链。`,
    '删除域名',
    { type: 'warning', confirmButtonText: '删除', cancelButtonText: '取消' }
  )
  await deleteDomain(row.id)
  ElMessage.success('已删除')
  await load()
}
</script>

<template>
  <div>
    <div class="sl-page-head">
      <div>
        <h1>短链域名</h1>
        <p class="sub">平台维护多个域名，用户在生成短链时选择，默认域名不可停用或删除</p>
      </div>
      <el-button type="primary" @click="openCreate">新增域名</el-button>
    </div>

    <section class="sl-card table-card">
      <el-table v-loading="loading" :data="rows">
        <el-table-column prop="domain" label="域名" min-width="240">
          <template #default="{ row }">
            <span class="mono">{{ row.domain }}</span>
            <el-tag v-if="row.defaultFlag" class="tag" size="small" effect="light">默认</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="110">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small" effect="light">
              {{ row.status === 1 ? '启用' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="短链数" width="110" align="right">
          <template #default="{ row }">
            <span class="num">{{ row.linkCount }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="190" />
        <el-table-column label="操作" width="200" align="right">
          <template #default="{ row }">
            <el-button
              text
              :type="row.status === 1 ? 'warning' : 'success'"
              :disabled="row.defaultFlag && row.status === 1"
              @click="toggleStatus(row)"
            >
              {{ row.status === 1 ? '停用' : '启用' }}
            </el-button>
            <el-button text type="danger" :disabled="row.defaultFlag" @click="remove(row)">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </section>

    <el-dialog v-model="createVisible" title="新增短链域名" width="480px">
      <el-form label-position="top">
        <el-form-item label="域名">
          <el-input v-model="form.domain" placeholder="例如 s.example.com，不要带 http://" />
          <div class="tip">需要先把该域名的 DNS 解析指向跳转服务，否则生成的短链无法访问</div>
        </el-form-item>
        <el-form-item label="设为默认域名">
          <el-switch v-model="form.defaultFlag" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="createVisible = false">取消</el-button>
        <el-button type="primary" :loading="creating" @click="submitCreate">添加</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.table-card {
  padding: 12px 16px 0;
}

.mono {
  font-family: 'JetBrains Mono', ui-monospace, monospace;
  font-size: 13px;
  color: var(--ink-900);
}

.tag {
  margin-left: 8px;
}

.tip {
  margin-top: 4px;
  font-size: 11.5px;
  line-height: 1.6;
  color: var(--ink-400);
}
</style>
