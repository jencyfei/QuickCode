/**
 * 环境检测工具
 * 用于判断当前运行环境（Web/App）
 */

/**
 * 检测是否在App环境中
 * @returns {boolean}
 */
export function isInApp() {
  // 方法1: 检测User-Agent
  const ua = navigator.userAgent.toLowerCase()
  if (ua.includes('sms-tagger-app')) {
    return true
  }
  
  // 方法2: 检测App注入的全局对象
  if (window.SmsNativeAPI || window.AndroidNative || window.webkit?.messageHandlers) {
    return true
  }
  
  // 方法3: 检测URL参数（临时测试用）
  const urlParams = new URLSearchParams(window.location.search)
  if (urlParams.get('platform') === 'app') {
    return true
  }
  
  return false
}

/**
 * 获取平台类型
 * @returns {'web'|'android'|'ios'}
 */
export function getPlatform() {
  if (!isInApp()) {
    return 'web'
  }
  
  const ua = navigator.userAgent.toLowerCase()
  if (ua.includes('android') || window.AndroidNative) {
    return 'android'
  }
  if (ua.includes('iphone') || ua.includes('ipad') || window.webkit?.messageHandlers) {
    return 'ios'
  }
  
  return 'web'
}

/**
 * 是否显示导入功能
 * @returns {boolean}
 */
export function shouldShowImportFeature() {
  // 1. 检查用户设置（优先级最高）
  const userSetting = localStorage.getItem('show_import_feature')
  if (userSetting !== null) {
    return userSetting === 'true'
  }
  
  // 2. 根据环境自动判断
  // Web环境显示，App环境隐藏
  return !isInApp()
}

/**
 * 设置是否显示导入功能
 * @param {boolean} show
 */
export function setShowImportFeature(show) {
  localStorage.setItem('show_import_feature', show.toString())
}

/**
 * 重置为默认设置（根据环境自动判断）
 */
export function resetImportFeatureSetting() {
  localStorage.removeItem('show_import_feature')
}

