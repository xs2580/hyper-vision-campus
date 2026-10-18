import { fileURLToPath, URL } from 'node:url'
import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url))
    }
  },
  server: {
    proxy: {
      // AI 聊天相关接口
      '/api': {
        target: 'http://localhost:8080',
        secure: false,
        changeOrigin: true
      },
      // 静态资源与场景数据接口
      '/image': {
        target: 'http://localhost:8080',
        secure: false,
        changeOrigin: true
      },
      '/video': {
        target: 'http://localhost:8080',
        secure: false,
        changeOrigin: true
      },
      '/edge': {
        target: 'http://localhost:8080',
        secure: false,
        changeOrigin: true
      }
    }
  }
})

