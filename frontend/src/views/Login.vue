<template>
  <div class="login-page">
    <!-- 顶部装饰 -->
    <div class="top-decoration">
      <div class="circle circle-1"></div>
      <div class="circle circle-2"></div>
      <div class="circle circle-3"></div>
    </div>
    
    <!-- Logo区域 -->
    <div class="logo-section">
      <div class="logo-icon">📱</div>
      <h1>短信标签助手</h1>
      <p class="subtitle">让短信管理变得简单有趣</p>
    </div>
    
    <!-- 表单区域 -->
    <div class="form-section">
      <van-form @submit="onSubmit">
        <div class="input-group">
          <van-field
            v-model="formData.username"
            name="username"
            placeholder="邮箱或手机号"
            :border="false"
            class="cute-input"
          >
            <template #left-icon>
              <span class="input-icon">👤</span>
            </template>
          </van-field>
        </div>
        
        <div class="input-group">
          <van-field
            v-model="formData.password"
            type="password"
            name="password"
            placeholder="密码"
            :border="false"
            class="cute-input"
          >
            <template #left-icon>
              <span class="input-icon">🔒</span>
            </template>
          </van-field>
        </div>
        
        <div class="button-group">
          <van-button
            round
            block
            type="primary"
            native-type="submit"
            class="cute-button primary-button"
          >
            <span class="button-text">登录 ✨</span>
          </van-button>
          
          <van-button
            round
            block
            class="cute-button secondary-button"
            @click="goToRegister"
          >
            <span class="button-text">注册账号 🎉</span>
          </van-button>
        </div>
      </van-form>
    </div>
    
    <!-- 底部装饰 -->
    <div class="bottom-decoration">
      <p class="tip">💡 小提示：支持邮箱和手机号登录哦</p>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { showToast } from 'vant'
import { login } from '@/api/auth'
import { useUserStore } from '@/store/user'

const router = useRouter()
const userStore = useUserStore()

const formData = ref({
  username: '',
  password: ''
})

const onSubmit = async (values) => {
  try {
    const response = await login(values)
    userStore.setToken(response.access_token)
    showToast('登录成功 🎉')
    router.push('/express-detail')
  } catch (error) {
    console.error('登录失败:', error)
  }
}

const goToRegister = () => {
  showToast('注册功能开发中... 🚀')
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  background: linear-gradient(180deg, #FFF5F5 0%, #FFE5E5 50%, #FFF0F0 100%);
  display: flex;
  flex-direction: column;
  justify-content: center;
  padding: 32px 24px;
  position: relative;
  overflow: hidden;
}

/* 顶部装饰圆圈 */
.top-decoration {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 200px;
  overflow: hidden;
}

.circle {
  position: absolute;
  border-radius: 50%;
  opacity: 0.1;
}

.circle-1 {
  width: 150px;
  height: 150px;
  background: #FF6B9D;
  top: -50px;
  left: -30px;
}

.circle-2 {
  width: 100px;
  height: 100px;
  background: #FFA07A;
  top: 20px;
  right: 40px;
}

.circle-3 {
  width: 80px;
  height: 80px;
  background: #FFB6C1;
  top: 100px;
  left: 50%;
}

/* Logo区域 */
.logo-section {
  text-align: center;
  margin-bottom: 40px;
  z-index: 1;
}

.logo-icon {
  font-size: 64px;
  margin-bottom: 16px;
  animation: bounce 2s infinite;
}

@keyframes bounce {
  0%, 100% {
    transform: translateY(0);
  }
  50% {
    transform: translateY(-10px);
  }
}

.logo-section h1 {
  font-size: 28px;
  font-weight: 700;
  color: #FF6B9D;
  margin-bottom: 8px;
  letter-spacing: 1px;
}

.subtitle {
  font-size: 14px;
  color: #999;
  font-weight: 400;
}

/* 表单区域 */
.form-section {
  background: white;
  border-radius: 24px;
  padding: 32px 24px;
  box-shadow: 0 8px 32px rgba(255, 107, 157, 0.1);
  z-index: 1;
}

/* 输入框组 */
.input-group {
  margin-bottom: 16px;
  background: #F8F9FA;
  border-radius: 16px;
  overflow: hidden;
  transition: all 0.3s;
}

.input-group:focus-within {
  background: #FFF5F5;
  box-shadow: 0 0 0 2px rgba(255, 107, 157, 0.2);
}

.cute-input {
  background: transparent;
}

.cute-input :deep(.van-field__control) {
  font-size: 15px;
  color: #333;
}

.cute-input :deep(.van-field__control::placeholder) {
  color: #BBB;
}

.input-icon {
  font-size: 20px;
  margin-right: 8px;
}

/* 按钮组 */
.button-group {
  margin-top: 32px;
}

.cute-button {
  height: 50px;
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 12px;
  border: none;
  transition: all 0.3s;
}

.primary-button {
  background: linear-gradient(135deg, #FF6B9D 0%, #FF8FAB 100%);
  box-shadow: 0 4px 16px rgba(255, 107, 157, 0.3);
}

.primary-button:active {
  transform: scale(0.98);
  box-shadow: 0 2px 8px rgba(255, 107, 157, 0.3);
}

.secondary-button {
  background: white;
  border: 2px solid #FFE5E5;
  color: #FF6B9D;
}

.secondary-button:active {
  transform: scale(0.98);
  background: #FFF5F5;
}

.button-text {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
}

/* 底部装饰 */
.bottom-decoration {
  text-align: center;
  margin-top: 24px;
  z-index: 1;
}

.tip {
  font-size: 13px;
  color: #999;
  padding: 12px 20px;
  background: rgba(255, 255, 255, 0.6);
  border-radius: 20px;
  display: inline-block;
}
</style>
