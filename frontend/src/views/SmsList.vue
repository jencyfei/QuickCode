<template>
  <div class="sms-list-page glass-background">
    <!-- 顶部导航栏 -->
    <van-nav-bar
      title="📱 我的短信"
      fixed
      class="cute-navbar"
    >
      <template #right>
        <van-button
          size="small"
          round
          class="import-button"
          @click="goToImport"
        >
          ➕ 导入
        </van-button>
      </template>
    </van-nav-bar>
    
    <!-- 内容区域 -->
    <div class="content">
      <!-- 空状态 -->
      <div class="empty-state">
        <div class="empty-icon">📭</div>
        <p class="empty-title">还没有短信哦</p>
        <p class="empty-desc">点击右上角"导入"按钮开始添加短信吧</p>
        <van-button
          round
          type="primary"
          class="cute-button"
          @click="goToImport"
        >
          立即导入 ✨
        </van-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'

const router = useRouter()

const goToImport = () => {
  router.push('/sms-import')
}
</script>

<style scoped>
.sms-list-page {
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

.import-button {
  background: var(--glass-accent-purple);
  color: white;
  border: none;
  padding: 0 16px;
  font-weight: 600;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
}

/* 内容区域 */
.content {
  padding: 70px 20px 20px;
  min-height: calc(100vh - 46px);
  display: flex;
  align-items: center;
  justify-content: center;
}

/* 空状态 */
.empty-state {
  text-align: center;
  padding: 40px 20px;
}

.empty-icon {
  font-size: 80px;
  margin-bottom: 20px;
  animation: float 3s ease-in-out infinite;
}

@keyframes float {
  0%, 100% {
    transform: translateY(0);
  }
  50% {
    transform: translateY(-10px);
  }
}

.empty-title {
  font-size: 20px;
  font-weight: 600;
  color: var(--glass-text-primary);
  margin-bottom: 8px;
}

.empty-desc {
  font-size: 14px;
  color: var(--glass-text-secondary);
  margin-bottom: 24px;
  line-height: 1.6;
}

.cute-button {
  background: var(--glass-accent-purple);
  border: none;
  padding: 0 32px;
  height: 44px;
  font-size: 15px;
  font-weight: 600;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
  color: white;
}

.cute-button:active {
  transform: scale(0.98);
}
</style>
