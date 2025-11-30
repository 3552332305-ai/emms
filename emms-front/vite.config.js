import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import path from 'node:path'

export default defineConfig({
    plugins: [vue()],
    resolve: {
        alias: {
            '@': path.resolve(__dirname, 'src'),
        },
    },
    server: {
        host: '0.0.0.0',
        port: Number(process.env.VITE_PORT) || 5173,
    },
    preview: {
        port: Number(process.env.VITE_PREVIEW_PORT) || 4173,
    },
})
