<template>
  <div class="sms-import-page glass-background">
    <!-- 顶部导航栏 -->
    <van-nav-bar
      title="📥 导入短信"
      left-arrow
      @click-left="onClickLeft"
      fixed
      class="cute-navbar"
    />
    
    <!-- 内容区域 -->
    <div class="content">
      <!-- 输入区域 -->
      <div class="input-section">
        <div class="input-header">
          <span class="icon">✍️</span>
          <span class="title">粘贴短信内容</span>
        </div>
        <van-field
          v-model="smsText"
          rows="12"
          autosize
          type="textarea"
          placeholder="在这里粘贴你的短信内容吧~&#10;&#10;支持一次粘贴多条短信哦 😊"
          class="cute-textarea"
          :border="false"
        />
      </div>
      
      <!-- 导入按钮 -->
      <div class="button-group">
        <van-button
          round
          block
          class="cute-button"
          @click="onImport"
        >
          <span class="button-text">开始导入 🚀</span>
        </van-button>
      </div>
      
      <!-- 提示卡片 -->
      <div class="tips-card">
        <div class="tips-header">
          <span class="icon">💡</span>
          <span class="title">小贴士</span>
        </div>
        <div class="tips-list">
          <div class="tip-item">
            <span class="dot">•</span>
            <span>支持批量粘贴多条短信</span>
          </div>
          <div class="tip-item">
            <span class="dot">•</span>
            <span>系统会自动识别发件人和时间</span>
          </div>
          <div class="tip-item">
            <span class="dot">•</span>
            <span>重复的短信会自动去重</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 底部导航 -->
    <van-tabbar v-model="activeTab" route class="cute-tabbar">
      <van-tabbar-item to="/express-detail" icon="logistics">快递</van-tabbar-item>
      <van-tabbar-item to="/tag-manage" icon="label-o">标签</van-tabbar-item>
      <van-tabbar-item v-if="showImport" to="/sms-import" icon="plus">导入</van-tabbar-item>
      <van-tabbar-item to="/settings" icon="setting-o">设置</van-tabbar-item>
    </van-tabbar>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { showToast, showLoadingToast, showConfirmDialog, closeToast } from 'vant'
import { createSmsBatch } from '@/api/sms'
import { parseSmsText, formatImportPreview, validateSmsData } from '@/utils/smsImportParser'
import { shouldShowImportFeature } from '@/utils/environment'

const router = useRouter()
const smsText = ref('')
const activeTab = ref(2)
const importing = ref(false)
const showImport = ref(shouldShowImportFeature())

const onClickLeft = () => {
  router.back()
}

const onImport = async () => {
  if (!smsText.value.trim()) {
    showToast('请输入短信内容哦 📝')
    return
  }
  
  try {
    importing.value = true
    
    // 1. 解析短信内容
    showLoadingToast({
      message: '正在解析短信...',
      forbidClick: true,
      duration: 0
    })
    
    const parsedSmsList = parseSmsText(smsText.value)
    
    if (parsedSmsList.length === 0) {
      closeToast()
      showToast('未能识别有效的短信内容')
      return
    }
    
    // 2. 验证数据
    let hasError = false
    for (const sms of parsedSmsList) {
      const errors = validateSmsData(sms)
      if (errors.length > 0) {
        closeToast()
        showToast(errors[0])
        hasError = true
        break
      }
    }
    
    if (hasError) {
      return
    }
    
    // 3. 显示预览并确认
    closeToast()
    const preview = formatImportPreview(parsedSmsList)
    
    await showConfirmDialog({
      title: '确认导入',
      message: preview,
      confirmButtonText: '确认导入',
      cancelButtonText: '取消'
    })
    
    // 4. 调用API批量导入
    showLoadingToast({
      message: '正在导入...',
      forbidClick: true,
      duration: 0
    })
    
    const response = await createSmsBatch({
      messages: parsedSmsList
    })
    
    closeToast()
    
    // 5. 显示结果
    const importedCount = response.length
    const skippedCount = parsedSmsList.length - importedCount
    
    let message = `成功导入 ${importedCount} 条短信`
    if (skippedCount > 0) {
      message += `\n${skippedCount} 条重复短信已跳过`
    }
    
    showToast({
      message,
      duration: 2000
    })
    
    // 6. 清空输入框并跳转
    smsText.value = ''
    
    setTimeout(() => {
      router.push('/express-detail')
    }, 2000)
    
  } catch (error) {
    closeToast()
    
    if (error === 'cancel') {
      // 用户取消
      return
    }
    
    console.error('导入失败:', error)
    showToast(error.response?.data?.detail || '导入失败，请重试')
  } finally {
    importing.value = false
  }
}
</script>

<style scoped>
.sms-import-page {
  min-height: 100vh;
}

/* 玻璃导航栏 */
.cute-navbar {
  background: var(--glass-fill);
  backdrop-filter: blur(var(--glass-blur));
  -webkit-backdrop-filter: blur(var(--glass-blur));
  border-bottom: 1px solid var(--glass-border);
  box-shadow: var(--glass-shadow);
}

.cute-navbar :deep(.van-nav-bar__title) {
  font-size: 18px;
  font-weight: 600;
  color: var(--glass-text-primary);
}

.cute-navbar :deep(.van-icon) {
  color: var(--glass-text-primary);
}

/* 内容区域 */
.content {
  padding: 62px 16px 20px;
}

/* 输入区域 - 玻璃面板 */
.input-section {
  background: var(--glass-fill);
  backdrop-filter: blur(var(--glass-blur));
  -webkit-backdrop-filter: blur(var(--glass-blur));
  border: 1px solid var(--glass-border);
  border-radius: var(--glass-radius);
  padding: 20px;
  margin-bottom: 16px;
  box-shadow: var(--glass-shadow);
  position: relative;
  z-index: 1;
}

.input-header {
  display: flex;
  align-items: center;
  margin-bottom: 12px;
}

.input-header .icon {
  font-size: 20px;
  margin-right: 8px;
}

.input-header .title {
  font-size: 16px;
  font-weight: 600;
  color: var(--glass-text-primary);
}

.cute-textarea {
  background: rgba(255, 255, 255, 0.5);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.7);
  border-radius: var(--glass-radius-medium);
  padding: 12px;
}

.cute-textarea :deep(.van-field__control) {
  font-size: 14px;
  line-height: 1.8;
  color: var(--glass-text-primary);
}

.cute-textarea :deep(.van-field__control::placeholder) {
  color: var(--glass-text-secondary);
  line-height: 1.8;
  opacity: 0.7;
}

/* 按钮组 */
.button-group {
  margin-bottom: 16px;
}

.cute-button {
  background: var(--glass-accent-purple);
  border: none;
  height: 50px;
  font-size: 16px;
  font-weight: 600;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
  color: white;
}

.cute-button:active {
  transform: scale(0.98);
}

.button-text {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
}

/* 提示卡片 - 玻璃面板 */
.tips-card {
  background: var(--glass-fill);
  backdrop-filter: blur(var(--glass-blur));
  -webkit-backdrop-filter: blur(var(--glass-blur));
  border: 1px solid var(--glass-border);
  border-radius: var(--glass-radius);
  padding: 20px;
  box-shadow: var(--glass-shadow);
  position: relative;
  z-index: 1;
}

.tips-header {
  display: flex;
  align-items: center;
  margin-bottom: 16px;
}

.tips-header .icon {
  font-size: 20px;
  margin-right: 8px;
}

.tips-header .title {
  font-size: 16px;
  font-weight: 600;
  color: var(--glass-text-primary);
}

.tips-list {
  padding-left: 4px;
}

.tip-item {
  display: flex;
  align-items: flex-start;
  margin-bottom: 12px;
  font-size: 14px;
  color: var(--glass-text-secondary);
  line-height: 1.6;
}

.tip-item:last-child {
  margin-bottom: 0;
}

.tip-item .dot {
  color: var(--glass-text-primary);
  margin-right: 8px;
  font-size: 18px;
}

/* 底部导航 - 玻璃效果 */
.cute-tabbar {
  background: var(--glass-fill);
  backdrop-filter: blur(var(--glass-blur));
  -webkit-backdrop-filter: blur(var(--glass-blur));
  border-top: 1px solid var(--glass-border);
  box-shadow: 0 -8px 32px rgba(0, 0, 0, 0.05);
}

.cute-tabbar :deep(.van-tabbar-item--active) {
  color: var(--glass-text-primary);
}
</style>
