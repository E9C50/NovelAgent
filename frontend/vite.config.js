import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import { resolve } from 'path'

export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      '@': resolve(__dirname, 'src')
    }
  },
  server: {
    port: 3000,
    open: true,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        // 如果后端不是以 /api 打头，可用下行改写（当前后端已以 /api 开头，保持不改写）
        // rewrite: (path) => path.replace(/^\/api/, '')
      }
    }
  }
})
