import { request } from './http'
import type {
  CreateLinkPayload,
  CreateLinkResult,
  DomainItem,
  LinkItem,
  PageVO
} from './types'

export function listDomains() {
  return request<DomainItem[]>({ url: '/domain/list' })
}

export function createLink(payload: CreateLinkPayload) {
  return request<CreateLinkResult>({ url: '/link/generate', method: 'post', data: payload })
}

export function pageLinks(params: {
  page: number
  size: number
  remark?: string
  domainId?: number
  status?: string
  startDate?: string
  endDate?: string
}) {
  return request<PageVO<LinkItem>>({ url: '/link/page', params })
}

export function getLink(code: string) {
  return request<LinkItem>({ url: `/link/${code}` })
}

export function updateLink(
  code: string,
  payload: {
    remark?: string
    password?: string
    startAt?: string
    endAt?: string
    expireAt?: string
    status?: number
  }
) {
  return request<void>({ url: `/link/${code}`, method: 'put', data: payload })
}

export function deleteLink(code: string) {
  return request<void>({ url: `/link/${code}`, method: 'delete' })
}

export function deleteLinks(ids: number[]) {
  return request<number>({ url: '/link/batch', method: 'delete', data: ids })
}
