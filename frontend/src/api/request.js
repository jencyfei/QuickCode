import axios from 'axios'
import { showToast } from 'vant'
import { useUserStore } from '@/store/user'
import router from '@/router'

// 创建axios实例
const request = axios.create({
  baseURL: '/api',
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json'
  }
})

// 请求拦截器
request.interceptors.request.use(
  (config) => {
    // 添加token到请求头
    const userStore = useUserStore()
    if (userStore.token) {
      config.headers.Authorization = `Bearer ${userStore.token}`
    }
    return config
  },
  (error) => {
    console.error('请求错误:', error)
    return Promise.reject(error)
  }
)

// 响应拦截器
request.interceptors.response.use(
  (response) => {
    return response.data
  },
  (error) => {
    console.error('响应错误:', error)
    
    if (error.response) {
      const { status, data } = error.response
      
      switch (status) {
        case 401:
          // 未授权，清除token并跳转到登录页
          showToast('登录已过期，请重新登录')
          const userStore = useUserStore()
          userStore.logout()
          router.push('/login')
          break
        case 403:
          showToast('没有权限访问')
          break
        case 404:
          showToast('请求的资源不存在')
          break
        case 500:
          showToast('服务器错误')
          break
        default:
          showToast(data.detail || '请求失败')
      }
    } else if (error.request) {
      showToast('网络错误，请检查网络连接')
    } else {
      showToast('请求配置错误')
    }
    
    return Promise.reject(error)
  }
)

export default request
