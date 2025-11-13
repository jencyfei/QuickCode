<template>
  <div class="settings-page glass-background">
    <!-- 顶部导航栏 -->
    <van-nav-bar
      title="设置"
      fixed
      class="cute-navbar"
    >
      <template #left>
        <span class="nav-icon">⚙️</span>
      </template>
    </van-nav-bar>

    <!-- 用户信息 -->
    <div class="user-section">
      <div class="user-avatar">👤</div>
      <div class="user-info">
        <div class="user-email">{{ userEmail || '未登录' }}</div>
        <div class="user-id">ID: {{ userId || '-' }}</div>
      </div>
    </div>

    <!-- 设置列表 -->
    <van-cell-group title="主题设置" class="setting-group">
      <van-cell title="主题颜色" is-link @click="showColorPicker = true">
        <template #value>
          <div class="color-preview" :style="{ background: themeColor }"></div>
        </template>
      </van-cell>
    </van-cell-group>

    <van-cell-group title="高级功能" class="setting-group">
      <van-cell title="规则配置" is-link @click="$router.push('/rule-config')">
        <template #icon>
          <span style="margin-right: 8px;">⚙️</span>
        </template>
      </van-cell>
    </van-cell-group>

    <van-cell-group title="数据管理" class="setting-group">
      <van-cell title="导出数据" is-link @click="exportData" />
      <van-cell title="清空缓存" is-link @click="clearCache" />
    </van-cell-group>

    <van-cell-group title="关于" class="setting-group">
      <van-cell title="版本号" value="1.0.0" />
      <van-cell title="隐私政策" is-link @click="showPrivacy" />
      <van-cell title="用户协议" is-link @click="showTerms" />
    </van-cell-group>

    <!-- 登出按钮 -->
    <div class="logout-section">
      <van-button
        type="danger"
        block
        round
        @click="handleLogout"
        class="logout-btn"
      >
        退出登录
      </van-button>
    </div>

    <!-- 颜色选择弹窗 -->
    <van-popup v-model:show="showColorPicker" position="bottom" round>
      <div class="color-picker-popup">
        <div class="popup-header">
          <h3>选择主题颜色</h3>
        </div>
        <div class="color-list">
          <div
            v-for="color in colorList"
            :key="color.value"
            class="color-option"
            :class="{ active: themeColor === color.value }"
            @click="selectColor(color.value)"
          >
            <div class="color-circle" :style="{ background: color.value }"></div>
            <div class="color-name">{{ color.name }}</div>
          </div>
        </div>
      </div>
    </van-popup>

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
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { showToast, showConfirmDialog } from 'vant'
import { useUserStore } from '@/store/user'
import { shouldShowImportFeature } from '@/utils/environment'

const router = useRouter()
const userStore = useUserStore()

// 数据
const activeTab = ref(3)
const showColorPicker = ref(false)
const themeColor = ref('#FF6B9D')

// 是否显示导入功能
const showImport = ref(shouldShowImportFeature())

// 颜色列表
const colorList = [
  { name: '粉色', value: '#FF6B9D' },
  { name: '蓝色', value: '#4A90E2' },
  { name: '紫色', value: '#9B59B6' },
  { name: '绿色', value: '#2ECC71' },
  { name: '橙色', value: '#E67E22' },
  { name: '红色', value: '#E74C3C' }
]

// 用户信息
const userEmail = computed(() => userStore.userEmail)
const userId = computed(() => userStore.userId)

// 选择颜色
const selectColor = (color) => {
  themeColor.value = color
  // 保存到本地存储
  localStorage.setItem('themeColor', color)
  // 应用主题色
  document.documentElement.style.setProperty('--primary-color', color)
  showColorPicker.value = false
  showToast('主题已更新')
}

// 导出数据
const exportData = () => {
  showToast('导出功能开发中...')
}

// 清空缓存
const clearCache = async () => {
  try {
    await showConfirmDialog({
      title: '确认清空',
      message: '确定要清空本地缓存吗？'
    })
    
    localStorage.clear()
    showToast('缓存已清空')
  } catch (error) {
    // 用户取消
  }
}

// 显示隐私政策
const showPrivacy = () => {
  showToast('隐私政策页面开发中...')
}

// 显示用户协议
const showTerms = () => {
  showToast('用户协议页面开发中...')
}

// 退出登录
const handleLogout = async () => {
  try {
    await showConfirmDialog({
      title: '确认退出',
      message: '确定要退出登录吗？'
    })
    
    userStore.logout()
    showToast('已退出登录')
    router.push('/login')
  } catch (error) {
    // 用户取消
  }
}

// 初始化
onMounted(() => {
  // 加载保存的主题色
  const savedColor = localStorage.getItem('themeColor')
  if (savedColor) {
    themeColor.value = savedColor
    document.documentElement.style.setProperty('--primary-color', savedColor)
  }
})
</script>

<style scoped>
.settings-page {
  min-height: 100vh;
  padding-top: 46px;
  padding-bottom: 60px;
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
  color: var(--glass-text-primary);
  font-weight: 600;
}

.nav-icon {
  font-size: 24px;
}

/* 用户信息 - 玻璃面板 */
.user-section {
  background: var(--glass-fill);
  backdrop-filter: blur(var(--glass-blur));
  -webkit-backdrop-filter: blur(var(--glass-blur));
  border: 1px solid var(--glass-border);
  border-radius: var(--glass-radius);
  padding: 32px 24px;
  display: flex;
  align-items: center;
  gap: 16px;
  margin: 0 16px 16px;
  box-shadow: var(--glass-shadow);
  position: relative;
  z-index: 1;
}

.user-avatar {
  width: 64px;
  height: 64px;
  border-radius: 50%;
  background: var(--glass-accent-purple);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 32px;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
}

.user-info {
  flex: 1;
}

.user-email {
  font-size: 18px;
  font-weight: 600;
  color: var(--glass-text-primary);
  margin-bottom: 4px;
}

.user-id {
  font-size: 13px;
  color: var(--glass-text-secondary);
}

/* 设置组 - 玻璃面板 */
.setting-group {
  margin: 0 16px 16px;
  background: var(--glass-fill);
  backdrop-filter: blur(var(--glass-blur));
  -webkit-backdrop-filter: blur(var(--glass-blur));
  border: 1px solid var(--glass-border);
  border-radius: var(--glass-radius);
  box-shadow: var(--glass-shadow);
  overflow: hidden;
  position: relative;
  z-index: 1;
}

.setting-group :deep(.van-cell-group__title) {
  padding-left: 24px;
  color: var(--glass-text-secondary);
  font-weight: 600;
  background: transparent;
}

.setting-group :deep(.van-cell) {
  background: transparent;
  color: var(--glass-text-primary);
}

.color-preview {
  width: 24px;
  height: 24px;
  border-radius: 50%;
}

/* 登出按钮 */
.logout-section {
  padding: 24px;
}

.logout-btn {
  background: var(--glass-fill);
  backdrop-filter: blur(var(--glass-blur-light));
  -webkit-backdrop-filter: blur(var(--glass-blur-light));
  border: 1px solid var(--glass-border);
  color: #E74C3C;
  box-shadow: var(--glass-shadow);
}

/* 颜色选择弹窗 */
.color-picker-popup {
  padding: 20px;
}

.popup-header {
  margin-bottom: 20px;
}

.popup-header h3 {
  font-size: 18px;
  font-weight: 600;
  color: #333;
}

.color-list {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
}

.color-option {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  padding: 16px;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s;
}

.color-option:active {
  transform: scale(0.95);
}

.color-option.active {
  background: rgba(102, 126, 234, 0.1);
  box-shadow: 0 0 0 2px rgba(102, 126, 234, 0.5);
}

.color-circle {
  width: 48px;
  height: 48px;
  border-radius: 50%;
}

.color-name {
  font-size: 13px;
  color: var(--glass-text-secondary);
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
