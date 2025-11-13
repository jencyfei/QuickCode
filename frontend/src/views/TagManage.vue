<template>
  <div class="tag-manage-page glass-background">
    <!-- 顶部导航栏 -->
    <van-nav-bar
      title="🏷️ 标签管理"
      left-arrow
      @click-left="onClickLeft"
      fixed
      class="cute-navbar"
    >
      <template #right>
        <van-button
          size="small"
          round
          class="add-button"
        >
          ➕ 新建
        </van-button>
      </template>
    </van-nav-bar>
    
    <!-- 内容区域 -->
    <div class="content">
      <!-- 空状态 -->
      <div class="empty-state">
        <div class="empty-icon">🎨</div>
        <p class="empty-title">还没有标签哦</p>
        <p class="empty-desc">创建标签，让短信管理更有条理</p>
        <van-button
          round
          type="primary"
          class="cute-button"
        >
          创建第一个标签 ✨
        </van-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'

const router = useRouter()

const onClickLeft = () => {
  router.back()
}
</script>

<style scoped>
.tag-manage-page {
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

.add-button {
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
  animation: rotate 4s ease-in-out infinite;
}

@keyframes rotate {
  0%, 100% {
    transform: rotate(0deg);
  }
  25% {
    transform: rotate(-10deg);
  }
  75% {
    transform: rotate(10deg);
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
