<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { pageUsers, resetUserPassword, updateUserStatus, type PlatformUser } from '@/api/admin'

const loading = ref(false)
const rows = ref<PlatformUser[]>([])
const pager = reactive({ page: 1, size: 20, total: 0 })
const filters = reactive({ keyword: '', status: undefined as number | undefined })

const resetVisible = ref(false)
const resetting = ref(false)
const resetTarget = ref<PlatformUser | null>(null)
const newPassword = ref('')

onMounted(load)

async function load() {
  loading.value = true
  try {
    const result = await pageUsers({
      keyword: filters.keyword || undefined,
      status: filters.status,
      page: pager.page,
      size: pager.size
    })
    rows.value = result.records
    pager.total = result.total
  } finally {
    loading.value = false
  }
}

function search() {
  pager.page = 1
  load()
}

function reset() {
  filters.keyword = ''
  filters.status = undefined
  pager.page = 1
  load()
}

async function toggleStatus(row: PlatformUser) {
  const next = row.status === 1 ? 0 : 1
  const action = next === 0 ? '禁用' : '启用'
  await ElMessageBox.confirm(
    next === 0
      ? `禁用后「${row.nickname || row.phone}」的所有请求会立即被拒绝，确定吗？`
      : `确定启用「${row.nickname || row.phone}」吗？`,
    `${action}用户`,
    { type: 'warning', confirmButtonText: action, cancelButtonText: '取消' }
  )
  await updateUserStatus(row.id, next)
  ElMessage.success(`已${action}`)
  await load()
}

function openReset(row: PlatformUser) {
  resetTarget.value = row
  newPassword.value = ''
  resetVisible.value = true
}

async function submitReset() {
  if (!resetTarget.value) {
    return
  }
  if (newPassword.value.length < 8) {
    ElMessage.warning('密码至少 8 位')
    return
  }
  resetting.value = true
  try {
    await resetUserPassword(resetTarget.value.id, newPassword.value)
    resetVisible.value = false
    ElMessage.success('密码已重置，该用户需用新密码重新登录')
  } finally {
    resetting.value = false
  }
}
</script>

<template>
  <div>
    <div class="sl-page-head">
      <div>
        <h1>注册用户</h1>
        <p class="sub">共 {{ pager.total }} 位注册用户</p>
      </div>
    </div>

    <section class="sl-card filters">
      <el-input
        v-model="filters.keyword"
        placeholder="手机号或昵称"
        clearable
        style="width: 240px"
        @keyup.enter="search"
      />
      <el-select v-model="filters.status" placeholder="全部状态" clearable style="width: 150px">
        <el-option label="正常" :value="1" />
        <el-option label="已禁用" :value="0" />
      </el-select>
      <el-button type="primary" @click="search">查询</el-button>
      <el-button @click="reset">重置</el-button>
    </section>

    <section class="sl-card table-card">
      <el-table v-loading="loading" :data="rows">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="phone" label="手机号" width="150" />
        <el-table-column prop="nickname" label="昵称" min-width="140">
          <template #default="{ row }">{{ row.nickname || '—' }}</template>
        </el-table-column>
        <el-table-column label="状态" width="110">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small" effect="light">
              {{ row.status === 1 ? '正常' : '已禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="短链数" width="110" align="right">
          <template #default="{ row }">
            <span class="num">{{ row.linkCount }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="注册时间" width="180" />
        <el-table-column label="最后登录" width="180">
          <template #default="{ row }">{{ row.lastLoginAt || '—' }}</template>
        </el-table-column>
        <el-table-column label="操作" width="200" align="right">
          <template #default="{ row }">
            <el-button text type="primary" @click="openReset(row)">重置密码</el-button>
            <el-button text :type="row.status === 1 ? 'danger' : 'success'" @click="toggleStatus(row)">
              {{ row.status === 1 ? '禁用' : '启用' }}
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="footer">
        <el-pagination
          v-model:current-page="pager.page"
          v-model:page-size="pager.size"
          :total="pager.total"
          :page-sizes="[20, 50, 100]"
          layout="total, sizes, prev, pager, next"
          @current-change="load"
          @size-change="search"
        />
      </div>
    </section>

    <el-dialog v-model="resetVisible" title="重置用户密码" width="460px">
      <p class="dialog-tip">
        将为「{{ resetTarget?.nickname || resetTarget?.phone }}」设置新密码，其已登录的会话会失效。
      </p>
      <el-input v-model="newPassword" type="password" show-password placeholder="8-32 位新密码" />
      <template #footer>
        <el-button @click="resetVisible = false">取消</el-button>
        <el-button type="primary" :loading="resetting" @click="submitReset">确认重置</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.filters {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px 18px;
}

.table-card {
  margin-top: 16px;
  padding: 12px 16px 0;
}

.footer {
  display: flex;
  justify-content: flex-end;
  padding: 12px 0 16px;
}

.dialog-tip {
  margin: 0 0 14px;
  font-size: 13px;
  line-height: 1.7;
  color: var(--ink-600);
}
</style>
