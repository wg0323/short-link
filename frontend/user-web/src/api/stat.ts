import { request } from './http'
import type {
  ClickLogItem,
  PageVO,
  RegionItem,
  StatOverview,
  StatRange,
  TrendPoint
} from './types'

interface RangeParams {
  code?: string
  range: StatRange
  start?: string
  end?: string
}

export function fetchOverview(params: RangeParams) {
  return request<StatOverview>({ url: '/stat/overview', params })
}

export function fetchTrend(params: RangeParams) {
  return request<TrendPoint[]>({ url: '/stat/trend', params })
}

export function fetchRegion(params: RangeParams) {
  return request<RegionItem[]>({ url: '/stat/region', params })
}

export function fetchClicks(params: RangeParams & { page: number; size: number }) {
  return request<PageVO<ClickLogItem>>({ url: '/stat/click/page', params })
}
