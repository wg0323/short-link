<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { createLink, deleteLink, deleteLinks, listDomains, pageLinks, updateLink } from '@/api/link'
import type { DomainItem, LinkItem } from '@/api/types'

const router = useRouter()

const loading = ref(false)
const rows = ref<LinkItem[]>([])
const selection = ref<LinkItem[]>([])
const domains = ref<DomainItem[]>([])

const filters = reactive({
  remark: '',
  domainId: undefined as number | undefined,
  status: '',
  dateRange: [] as string[]
})

const pager = reactive({ page: 1, size: 10, total: 0 })

const createVisible = ref(false)
const creating = ref(false)
const createForm = reactive({
  originUrl: '',
  remark: '',
  domainId: undefined as number | undefined,
  code: '',
  password: '',
  startAt: '',
  endAt: '',
  expireAt: ''
})

const editVisible = ref(false)
const editing = ref(false)
const editForm = reactive({
  code: '',
  remark: '',
  password: '',
  status: 1
})

const statusMeta: Record<string, { text: string; type: 'success' | 'info' | 'danger' }> = {
  normal: { text: '正常', type: 'success' },
  expired: { text: '已过期', type: 'info' },
  disabled: { text: '已禁用', type: 'info' },
  deleted: { text: '已删除', type: 'danger' }
}

onMounted(async () => {
  domains.value = await listDomains()
  await load()
})

async function load() {
  loading.value = true
  try {
    const result = await pageLinks({
      page: pager.page,
      size: pager.size,
      remark: filters.remark || undefined,
      domainId: filters.domainId,
      status: filters.status || undefined,
      startDate: filters.dateRange?.[0] || undefined,
      endDate: filters.dateRange?.[1] || undefined
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
  filters.remark = ''
  filters.domainId = undefined
  filters.status = ''
  filters.dateRange = []
  pager.page = 1
  load()
}

function openCreate() {
  createForm.originUrl = ''
  createForm.remark = ''
  createForm.domainId = domains.value.find((item) => item.defaultFlag)?.id
  createForm.code = ''
  createForm.password = ''
  createForm.startAt = ''
  createForm.endAt = ''
  createForm.expireAt = ''
  createVisible.value = true
}

async function submitCreate() {
  if (!createForm.originUrl.trim()) {
    ElMessage.warning('请填写长链接')
    return
  }
  creating.value = true
  try {
    const result = await createLink({
      originUrl: createForm.originUrl.trim(),
      remark: createForm.remark || undefined,
      domainId: createForm.domainId,
      code: createForm.code || undefined,
      password: createForm.password || undefined,
      startAt: createForm.startAt || undefined,
      endAt: createForm.endAt || undefined,
      expireAt: createForm.expireAt || undefined
    })
    createVisible.value = false
    ElMessage.success(result.duplicated ? '该链接已存在，已返回原短链' : '生成成功')
    await load()
  } catch {
    // 拦截器已提示
  } finally {
    creating.value = false
  }
}

function openEdit(row: LinkItem) {
  editForm.code = row.code
  editForm.remark = row.remark
  editForm.password = ''
  editForm.status = row.status === 'disabled' ? 0 : 1
  editVisible.value = true
}

async function submitEdit() {
  editing.value = true
  try {
    await updateLink(editForm.code, {
      remark: editForm.remark,
      password: editForm.password || undefined,
      status: editForm.status
    })
    editVisible.value = false
    ElMessage.success('已保存')
    await load()
  } catch {
    // 拦截器已提示
  } finally {
    editing.value = false
  }
}

async function remove(row: LinkItem) {
  await ElMessageBox.confirm(`确定删除「${row.remark || row.code}」吗？删除后跳转立即失效。`, '删除短链', {
    type: 'warning',
    confirmButtonText: '删除',
    cancelButtonText: '取消'
  })
  await deleteLink(row.code)
  ElMessage.success('已删除')
  await load()
}

async function removeBatch() {
  if (!selection.value.length) {
    return
  }
  await ElMessageBox.confirm(`确定删除选中的 ${selection.value.length} 条短链吗？`, '批量删除', {
    type: 'warning',
    confirmButtonText: '删除',
    cancelButtonText: '取消'
  })
  const count = await deleteLinks(selection.value.map((item) => item.id))
  ElMessage.success(`已删除 ${count} 条`)
  await load()
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
  <div>
    <div class="sl-page-head">
      <div>
        <h1>短链管理</h1>
        <p class="sub">共 {{ pager.total }} 条短链</p>
      </div>
      <el-button type="primary" @click="openCreate">新建短链</el-button>
    </div>

    <section class="sl-card filters">
      <div class="field">
        <label>备注名</label>
        <el-input v-model="filters.remark" placeholder="输入备注名搜索" clearable style="width: 220px" />
      </div>
      <div class="field">
        <label>短链域名</label>
        <el-select v-model="filters.domainId" placeholder="全部域名" clearable style="width: 170px">
          <el-option v-for="item in domains" :key="item.id" :label="item.domain" :value="item.id" />
        </el-select>
      </div>
      <div class="field">
        <label>状态</label>
        <el-select v-model="filters.status" placeholder="全部状态" clearable style="width: 150px">
          <el-option label="正常" value="normal" />
          <el-option label="已过期" value="expired" />
          <el-option label="已禁用" value="disabled" />
          <el-option label="已删除" value="deleted" />
        </el-select>
      </div>
      <div class="field">
        <label>创建时间</label>
        <el-date-picker
          v-model="filters.dateRange"
          type="daterange"
          value-format="YYYY-MM-DD"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          style="width: 250px"
        />
      </div>
      <div class="actions">
        <el-button type="primary" @click="search">查询</el-button>
        <el-button @click="reset">重置</el-button>
      </div>
    </section>

    <section class="sl-card table-card">
      <el-table v-loading="loading" :data="rows" @selection-change="(val: LinkItem[]) => (selection = val)">
        <el-table-column type="selection" width="46" />
        <el-table-column label="短链" min-width="300">
          <template #default="{ row }">
            <span class="mono">{{ row.shortUrl }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="备注名" min-width="200">
          <template #default="{ row }">{{ row.remark || '—' }}</template>
        </el-table-column>
        <el-table-column prop="originUrl" label="原始链接" min-width="260" show-overflow-tooltip />
        <el-table-column prop="domain" label="域名" width="150" />
        <el-table-column label="访问量" width="110" align="right">
          <template #default="{ row }">
            <span class="num">{{ row.clickCount.toLocaleString() }}</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="110">
          <template #default="{ row }">
            <el-tag :type="statusMeta[row.status]?.type" size="small" effect="light">
              {{ statusMeta[row.status]?.text }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="180" />
        <el-table-column label="操作" width="320" align="center">
          <template #default="{ row }">
            <el-button text type="primary" @click="copy(row.shortUrl)">复制</el-button>
            <el-button text @click="router.push('/console/stats?code=' + row.code)">统计</el-button>
            <el-button text @click="openEdit(row)">编辑</el-button>
            <el-button text type="danger" @click="remove(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="footer">
        <el-button :disabled="!selection.length" @click="removeBatch">
          批量删除{{ selection.length ? `（${selection.length}）` : '' }}
        </el-button>
        <el-pagination
          v-model:current-page="pager.page"
          v-model:page-size="pager.size"
          :total="pager.total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next"
          @current-change="load"
          @size-change="search"
        />
      </div>
    </section>

    <el-dialog v-model="createVisible" title="新建短链" width="620px">
      <el-form label-position="top">
        <el-form-item label="长链接" required>
          <el-input v-model="createForm.originUrl" placeholder="https://example.com/very/long/link" />
          <div class="tip">仅支持 http / https，内网地址会被拒绝</div>
        </el-form-item>
        <div class="grid-2">
          <el-form-item label="备注名">
            <el-input v-model="createForm.remark" placeholder="例如：双十一主会场" maxlength="100" />
          </el-form-item>
          <el-form-item label="短链域名">
            <el-select v-model="createForm.domainId" placeholder="使用默认域名">
              <el-option
                v-for="item in domains"
                :key="item.id"
                :label="item.defaultFlag ? `${item.domain}（默认）` : item.domain"
                :value="item.id"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="自定义短码">
            <el-input v-model="createForm.code" placeholder="留空自动生成 7 位" maxlength="8" />
          </el-form-item>
          <el-form-item label="访问密码">
            <el-input v-model="createForm.password" placeholder="留空表示无密码" maxlength="32" />
          </el-form-item>
          <el-form-item label="到期时间">
            <el-date-picker
              v-model="createForm.expireAt"
              type="datetime"
              value-format="YYYY-MM-DD HH:mm:ss"
              placeholder="不限"
              style="width: 100%"
            />
          </el-form-item>
          <el-form-item label="生效开始">
            <el-date-picker
              v-model="createForm.startAt"
              type="datetime"
              value-format="YYYY-MM-DD HH:mm:ss"
              placeholder="立即生效"
              style="width: 100%"
            />
          </el-form-item>
          <el-form-item label="生效结束">
            <el-date-picker
              v-model="createForm.endAt"
              type="datetime"
              value-format="YYYY-MM-DD HH:mm:ss"
              placeholder="不限"
              style="width: 100%"
            />
          </el-form-item>
        </div>
      </el-form>
      <template #footer>
        <el-button @click="createVisible = false">取消</el-button>
        <el-button type="primary" :loading="creating" @click="submitCreate">生成短链</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="editVisible" title="编辑短链" width="520px">
      <el-form label-position="top">
        <el-form-item label="短码">
          <el-input :model-value="editForm.code" disabled />
        </el-form-item>
        <el-form-item label="备注名">
          <el-input v-model="editForm.remark" maxlength="100" />
        </el-form-item>
        <el-form-item label="访问密码">
          <el-input v-model="editForm.password" placeholder="留空表示不修改密码" maxlength="32" />
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="editForm.status">
            <el-radio :value="1">启用</el-radio>
            <el-radio :value="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editVisible = false">取消</el-button>
        <el-button type="primary" :loading="editing" @click="submitEdit">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.filters {
  display: flex;
  flex-wrap: nowrap;
  align-items: center;
  gap: 24px;
  padding: 16px 18px;
  overflow-x: auto;
}

.field {
  display: flex;
  flex: none;
  align-items: center;
  gap: 8px;
}

.field label {
  flex: none;
  font-size: 12px;
  color: var(--ink-400);
  white-space: nowrap;
}

.actions {
  display: flex;
  flex: none;
  gap: 10px;
  margin-left: auto;
}

.table-card {
  margin-top: 16px;
  padding: 12px 16px 0;
}

.mono {
  font-family: 'JetBrains Mono', ui-monospace, monospace;
  font-size: 12.5px;
  color: var(--ink-900);
}

.footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 0 16px;
}

.tip {
  margin-top: 4px;
  font-size: 11.5px;
  color: var(--ink-400);
}

.grid-2 {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 0 16px;
}
</style>
