import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useUserStore = defineStore('user', () => {
  // 状态
  const token = ref(localStorage.getItem('token') || '')
  const userInfo = ref(null)

  // 设置token
  const setToken = (newToken) => {
    token.value = newToken
    if (newToken) {
      localStorage.setItem('token', newToken)
    } else {
      localStorage.removeItem('token')
    }
  }

  // 设置用户信息
  const setUserInfo = (info) => {
    userInfo.value = info
  }

  // 登出
  const logout = () => {
    token.value = ''
    userInfo.value = null
    localStorage.removeItem('token')
  }

  // 是否已登录
  const isLoggedIn = () => {
    return !!token.value
  }

  return {
    token,
    userInfo,
    setToken,
    setUserInfo,
    logout,
    isLoggedIn
  }
})
