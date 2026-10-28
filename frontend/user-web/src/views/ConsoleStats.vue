<script setup lang="ts">
import { nextTick, onBeforeUnmount, onMounted, reactive, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import * as echarts from 'echarts'
import { fetchClicks, fetchOverview, fetchRegion, fetchTrend } from '@/api/stat'
import type { ClickLogItem, RegionItem, StatOverview, StatRange, TrendPoint } from '@/api/types'

const route = useRoute()

const range = ref<StatRange>('today')
const customRange = ref<string[]>([])
const code = ref((route.query.code as string) || '')
const overview = ref<StatOverview>({ pv: 0, uv: 0, ipCount: 0, linkCount: 0 })
const trend = ref<TrendPoint[]>([])
const regions = ref<RegionItem[]>([])
const clicks = ref<ClickLogItem[]>([])
const loading = ref(false)
const pager = reactive({ page: 1, size: 10, total: 0 })

const chartRef = ref<HTMLElement>()
let chart: echarts.ECharts | null = null

const maxRegionPv = ref(1)

onMounted(async () => {
  await load()
  await nextTick()
  if (chartRef.value) {
    chart = echarts.init(chartRef.value)
    renderChart()
    window.addEventListener('resize', handleResize)
  }
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  chart?.dispose()
  chart = null
})

watch([range, code], () => {
  pager.page = 1
  load()
})

function handleResize() {
  chart?.resize()
}

async function load() {
  if (range.value === 'custom' && customRange.value.length !== 2) {
    return
  }
  loading.value = true
  const params = {
    code: code.value || undefined,
    range: range.value,
    start: range.value === 'custom' ? customRange.value[0] : undefined,
    end: range.value === 'custom' ? customRange.value[1] : undefined
  }
  try {
    const [stat, points, regionList, page] = await Promise.all([
      fetchOverview(params),
      fetchTrend(params),
      fetchRegion(params),
      fetchClicks({ ...params, page: pager.page, size: pager.size })
    ])
    overview.value = stat
    trend.value = points
    regions.value = regionList
    maxRegionPv.value = Math.max(1, ...regionList.map((item) => item.pv))
    clicks.value = page.records
    pager.total = page.total
    renderChart()
  } finally {
    loading.value = false
  }
}

async function loadClicks() {
  const params = {
    code: code.value || undefined,
    range: range.value,
    start: range.value === 'custom' ? customRange.value[0] : undefined,
    end: range.value === 'custom' ? customRange.value[1] : undefined,
    page: pager.page,
    size: pager.size
  }
  const page = await fetchClicks(params)
  clicks.value = page.records
  pager.total = page.total
}

function renderChart() {
  if (!chart) {
    return
  }
  const days = trend.value.map((item) => item.date)
  const pv = trend.value.map((item) => item.pv)
  const uv = trend.value.map((item) => item.uv)
  const ip = trend.value.map((item) => item.ipCount)

  chart.setOption({
    grid: { left: 48, right: 16, top: 16, bottom: 32 },
    tooltip: { trigger: 'axis' },
    legend: {
      right: 0,
      top: 0,
      icon: 'roundRect',
      itemWidth: 8,
      itemHeight: 8,
      textStyle: { color: '#94a3b8', fontSize: 12 },
      data: ['PV', 'UV', '独立 IP']
    },
    xAxis: {
      type: 'category',
      data: days,
      boundaryGap: false,
      axisLine: { lineStyle: { color: '#e2e8f0' } },
      axisLabel: { color: '#94a3b8', fontSize: 11 },
      axisTick: { show: false }
    },
    yAxis: {
      type: 'value',
      splitLine: { lineStyle: { color: '#eff2f6' } },
      axisLabel: { color: '#94a3b8', fontSize: 11 }
    },
    series: [
      {
        name: 'PV',
        type: 'line',
        smooth: true,
        symbol: 'circle',
        symbolSize: 5,
        showSymbol: days.length <= 2,
        data: pv,
        lineStyle: { width: 2.2, color: '#2563eb' },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(37, 99, 235, 0.18)' },
            { offset: 1, color: 'rgba(37, 99, 235, 0)' }
          ])
        }
      },
      {
        name: 'UV',
        type: 'line',
        smooth: true,
        symbol: 'circle',
        symbolSize: 5,
        showSymbol: days.length <= 2,
        data: uv,
        lineStyle: { width: 1.8, color: '#0d9488' }
      },
      {
        name: '独立 IP',
        type: 'line',
        smooth: true,
        symbol: 'circle',
        symbolSize: 5,
        showSymbol: days.length <= 2,
        data: ip,
        lineStyle: { width: 1.8, color: '#cbd5e1' }
      }
    ]
  })
}

function regionWidth(pv: number) {
  return `${Math.max(4, Math.round((pv / maxRegionPv.value) * 100))}%`
}
</script>

<template>
  <div v-loading="loading">
    <div class="sl-page-head">
      <div>
        <h1>数据统计</h1>
        <p class="sub">实时访问趋势、地域分布与访问明细</p>
      </div>
      <div class="head-actions">
        <el-input v-model="code" placeholder="全部短链（输入短码筛选）" clearable style="width: 220px" />
        <el-radio-group v-model="range">
          <el-radio-button value="today">今天</el-radio-button>
          <el-radio-button value="yesterday">昨天</el-radio-button>
          <el-radio-button value="last30">最近 30 天</el-radio-button>
          <el-radio-button value="custom">自定义</el-radio-button>
        </el-radio-group>
        <el-date-picker
          v-if="range === 'custom'"
          v-model="customRange"
          type="daterange"
          value-format="YYYY-MM-DD"
          start-placeholder="开始"
          end-placeholder="结束"
          style="width: 250px"
          @change="load"
        />
      </div>
    </div>

    <div class="kpi-grid">
      <div class="kpi sl-card">
        <div class="label">访问量 PV</div>
        <div class="value num">{{ overview.pv.toLocaleString() }}</div>
      </div>
      <div class="kpi sl-card">
        <div class="label">独立访客 UV</div>
        <div class="value num">{{ overview.uv.toLocaleString() }}</div>
      </div>
      <div class="kpi sl-card">
        <div class="label">独立 IP</div>
        <div class="value num">{{ overview.ipCount.toLocaleString() }}</div>
      </div>
      <div class="kpi sl-card">
        <div class="label">短链总数</div>
        <div class="value num">{{ overview.linkCount.toLocaleString() }}</div>
      </div>
    </div>

    <div class="chart-grid">
      <section class="sl-card chart-card">
        <h2>访问趋势</h2>
        <div ref="chartRef" class="chart"></div>
        <el-empty v-if="!trend.length" description="所选区间还没有访问数据" :image-size="80" />
      </section>

      <section class="sl-card region-card">
        <h2>地域分布</h2>
        <div v-if="regions.length" class="region-list">
          <div v-for="item in regions" :key="item.province" class="bar-row">
            <span class="name">{{ item.province }}</span>
            <span class="track"><span class="fill" :style="{ width: regionWidth(item.pv) }" /></span>
            <span class="val num">{{ item.pv.toLocaleString() }}</span>
          </div>
        </div>
        <el-empty v-else description="暂无地域数据" :image-size="70" />
      </section>
    </div>

    <section class="sl-card detail">
      <h2>访问明细</h2>
      <el-table :data="clicks" style="width: 100%">
        <el-table-column prop="clickAt" label="访问时间" width="190" />
        <el-table-column prop="code" label="短链" width="140" />
        <el-table-column prop="ip" label="IP" width="150" />
        <el-table-column label="地域" width="190">
          <template #default="{ row }">
            {{ row.province }}{{ row.city && row.city !== row.province ? ' · ' + row.city : '' }}
          </template>
        </el-table-column>
        <el-table-column prop="device" label="设备" width="180" />
        <el-table-column prop="referer" label="来源" show-overflow-tooltip>
          <template #default="{ row }">{{ row.referer || '直接访问' }}</template>
        </el-table-column>
      </el-table>
      <div class="pager">
        <el-pagination
          v-model:current-page="pager.page"
          v-model:page-size="pager.size"
          :total="pager.total"
          layout="total, prev, pager, next"
          @current-change="loadClicks"
        />
      </div>
    </section>
  </div>
</template>

<style scoped>
.head-actions {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

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

.chart-grid {
  display: grid;
  grid-template-columns: minmax(0, 1.9fr) minmax(0, 1fr);
  gap: 16px;
  margin-top: 16px;
}

.chart-card,
.region-card {
  display: flex;
  flex-direction: column;
  padding: 16px 18px 12px;
}

h2 {
  margin: 0 0 8px;
  font-size: 14px;
  font-weight: 600;
  color: var(--ink-900);
}

.chart {
  width: 100%;
  height: 320px;
}

.region-list {
  display: flex;
  flex: 1;
  flex-direction: column;
  justify-content: space-between;
  gap: 8px;
  padding: 8px 0 12px;
}

.bar-row {
  display: grid;
  grid-template-columns: 62px minmax(0, 1fr) 68px;
  align-items: center;
  gap: 10px;
  font-size: 12.5px;
}

.bar-row .name {
  color: var(--ink-600);
}

.track {
  display: block;
  height: 6px;
  border-radius: 3px;
  background: var(--sunken);
  overflow: hidden;
}

.fill {
  display: block;
  height: 100%;
  border-radius: 3px;
  background: linear-gradient(90deg, #60a5fa, #2563eb);
}

.bar-row .val {
  text-align: right;
  color: var(--ink-700);
}

.detail {
  margin-top: 16px;
  padding: 16px 18px 8px;
}

.pager {
  display: flex;
  justify-content: flex-end;
  padding: 12px 0;
}

@media (max-width: 1200px) {
  .kpi-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .chart-grid {
    grid-template-columns: 1fr;
  }
}
</style>
