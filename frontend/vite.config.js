import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import { fileURLToPath, URL } from 'node:url'

// https://vite.dev/config/
export default defineConfig({
  plugins: [vue()],
  
  // 路径别名
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url))
    }
  },
  
  // 开发服务器配置
  server: {
    port: 3000,
    host: '0.0.0.0',
    // API代理配置
    proxy: {
      '/api': {
        target: 'http://localhost:10043',  // 匹配后端实际运行端口
        changeOrigin: true,
      }
    }
  },
  
  // 构建配置
  build: {
    outDir: 'dist',
    assetsDir: 'assets',
    sourcemap: false,
  }
})
