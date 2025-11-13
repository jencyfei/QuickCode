import request from './request'

/**
 * 获取短信列表
 */
export function getSmsList(params) {
  return request({
    url: '/sms',
    method: 'get',
    params
  })
}

/**
 * 获取单条短信详情
 */
export function getSms(id) {
  return request({
    url: `/sms/${id}`,
    method: 'get'
  })
}

/**
 * 创建单条短信
 */
export function createSms(data) {
  return request({
    url: '/sms',
    method: 'post',
    data
  })
}

/**
 * 批量创建短信
 */
export function createSmsBatch(data) {
  return request({
    url: '/sms/batch',
    method: 'post',
    data
  })
}

/**
 * 更新短信
 */
export function updateSms(id, data) {
  return request({
    url: `/sms/${id}`,
    method: 'put',
    data
  })
}

/**
 * 删除单条短信
 */
export function deleteSms(id) {
  return request({
    url: `/sms/${id}`,
    method: 'delete'
  })
}

/**
 * 批量删除短信
 */
export function batchDeleteSms(data) {
  return request({
    url: '/sms/batch-delete',
    method: 'post',
    data
  })
}

/**
 * 为短信添加标签
 */
export function addTagsToSms(smsId, data) {
  return request({
    url: `/sms/${smsId}/tags`,
    method: 'post',
    data
  })
}

/**
 * 批量为短信添加标签
 */
export function batchAddTagsToSms(data) {
  return request({
    url: '/sms/batch-add-tags',
    method: 'post',
    data
  })
}

/**
 * 移除短信的标签
 */
export function removeTagFromSms(smsId, tagId) {
  return request({
    url: `/sms/${smsId}/tags/${tagId}`,
    method: 'delete'
  })
}
