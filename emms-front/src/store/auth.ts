import { defineStore } from 'pinia'
import router from '@/router'
import { authAPI, supplierSelfAPI } from '@/api'

interface UserInfo {
    id: number
    username: string
    role: 'ADMIN' | 'USER' | 'SUPPLIER'
    supplierId?: number
    supplier?: {
        id: number
        name: string
        [key: string]: any
    }
    [key: string]: any
}

interface LoginPayload {
    username: string
    password: string
}

interface LoginResponse {
    user: UserInfo
    sessionId: string
}

const STORAGE_KEYS = {
    SESSION_ID: 'sessionId',
    USER: 'user',
    ROLE: 'role',
    LEGACY_TOKEN: 'token',
}

export const useAuthStore = defineStore('auth', {
    state: () => ({
        sessionId:
            localStorage.getItem(STORAGE_KEYS.SESSION_ID) ||
            localStorage.getItem(STORAGE_KEYS.LEGACY_TOKEN) ||
            '',
        role: (localStorage.getItem(STORAGE_KEYS.ROLE) as UserInfo['role']) || 'USER',
        user: localStorage.getItem(STORAGE_KEYS.USER)
            ? (JSON.parse(localStorage.getItem(STORAGE_KEYS.USER)!) as UserInfo)
            : null,
    }),
    getters: {
        isAuthenticated: (state) => Boolean(state.sessionId),
    },
    actions: {
        persistSession(payload: LoginResponse) {
            this.sessionId = payload.sessionId
            this.user = payload.user
            this.role = payload.user?.role || 'USER'

            localStorage.setItem(STORAGE_KEYS.SESSION_ID, payload.sessionId)
            localStorage.setItem(STORAGE_KEYS.USER, JSON.stringify(payload.user))
            localStorage.setItem(STORAGE_KEYS.ROLE, this.role)
            localStorage.removeItem(STORAGE_KEYS.LEGACY_TOKEN)
        },
        async login(credentials: LoginPayload) {
            const data = await authAPI.login<LoginResponse>(credentials)
            this.persistSession(data)
            await this.fetchSupplierProfile()
            return data
        },
        async fetchSupplierProfile() {
            if (this.user?.role !== 'SUPPLIER' || !this.user?.supplierId) {
                return null
            }
            if (this.user.supplier) {
                return this.user.supplier
            }
            try {
                const supplier = await supplierSelfAPI.getProfile()
                this.user = this.user ? { ...this.user, supplier } : this.user
                localStorage.setItem(STORAGE_KEYS.USER, JSON.stringify(this.user))
                return supplier
            } catch (error) {
                console.error('Failed to fetch supplier info', error)
                return null
            }
        },
        logout() {
            this.sessionId = ''
            this.role = 'USER'
            this.user = null
            localStorage.removeItem(STORAGE_KEYS.SESSION_ID)
            localStorage.removeItem(STORAGE_KEYS.USER)
            localStorage.removeItem(STORAGE_KEYS.ROLE)
            localStorage.removeItem(STORAGE_KEYS.LEGACY_TOKEN)
            router.push({ name: 'login' })
        },
    },
})

