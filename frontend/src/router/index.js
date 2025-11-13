import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/',
    redirect: '/express-detail'
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: { requiresAuth: false }
  },
  {
    path: '/sms-list',
    name: 'SmsList',
    component: () => import('@/views/SmsListNew.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/sms-import',
    name: 'SmsImport',
    component: () => import('@/views/SmsImport.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/tag-manage',
    name: 'TagManage',
    component: () => import('@/views/TagManageNew.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/settings',
    name: 'Settings',
    component: () => import('@/views/Settings.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/express-detail',
    name: 'ExpressDetail',
    component: () => import('@/views/ExpressDetail.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/rule-config',
    name: 'RuleConfig',
    component: () => import('@/views/RuleConfig.vue'),
    meta: { requiresAuth: true }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫 - 检查登录状态（临时禁用，方便查看页面）
router.beforeEach((to, from, next) => {
  // 临时禁用登录检查，直接放行
  next()
  
  // const token = localStorage.getItem('token')
  // 
  // if (to.meta.requiresAuth && !token) {
  //   // 需要登录但未登录，跳转到登录页
  //   next('/login')
  // } else if (to.path === '/login' && token) {
  //   // 已登录访问登录页，跳转到短信列表
  //   next('/sms-list')
  // } else {
  //   next()
  // }
})

export default router
