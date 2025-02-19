import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

export default defineConfig({
  plugins: [react()],
  server: {
    port: 3000, // 프론트 엔드를 원하는 포트 번호 지정
    proxy: {
      '/api': {
        target: 'http://43.200.140.40:8080', // 백엔드 서버 주소
        changeOrigin: true
      }
    }
  },
  define: {
    global: 'window', // 👈 global 변수를 window로 매핑하여 Vite에서 인식할 수 있도록 설정
  }
})
