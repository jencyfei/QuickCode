import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import pinia from './store'

// Vant UI
import Vant from 'vant'
import 'vant/lib/index.css'

// 全局样式
import '@/assets/styles/global.css'

const app = createApp(App)

app.use(router)
app.use(pinia)
app.use(Vant)

app.mount('#app')
