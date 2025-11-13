/**
 * 提取规则API
 */
import request from './request'

/**
 * 获取提取规则列表
 * @param {string} ruleType - 规则类型: pickup_code, address, sender
 */
export function getRules(ruleType = null) {
  return request({
    url: '/extraction-rules',
    method: 'get',
    params: { rule_type: ruleType }
  })
}

/**
 * 获取单个规则
 * @param {number} ruleId - 规则ID
 */
export function getRule(ruleId) {
  return request({
    url: `/extraction-rules/${ruleId}`,
    method: 'get'
  })
}

/**
 * 创建规则
 * @param {object} data - 规则数据
 */
export function createRule(data) {
  return request({
    url: '/extraction-rules',
    method: 'post',
    data
  })
}

/**
 * 更新规则
 * @param {number} ruleId - 规则ID
 * @param {object} data - 规则数据
 */
export function updateRule(ruleId, data) {
  return request({
    url: `/extraction-rules/${ruleId}`,
    method: 'put',
    data
  })
}

/**
 * 删除规则
 * @param {number} ruleId - 规则ID
 */
export function deleteRule(ruleId) {
  return request({
    url: `/extraction-rules/${ruleId}`,
    method: 'delete'
  })
}

/**
 * 测试规则
 * @param {object} data - 测试数据 { pattern, extract_group, test_text }
 */
export function testRule(data) {
  return request({
    url: '/extraction-rules/test',
    method: 'post',
    data
  })
}

/**
 * 获取规则模板列表
 * @param {string} ruleType - 规则类型
 */
export function getRuleTemplates(ruleType = null) {
  return request({
    url: '/extraction-rules/templates',
    method: 'get',
    params: { rule_type: ruleType }
  })
}

/**
 * 应用规则模板
 * @param {number} templateId - 模板ID
 */
export function applyRuleTemplate(templateId) {
  return request({
    url: `/extraction-rules/templates/${templateId}/apply`,
    method: 'post'
  })
}
