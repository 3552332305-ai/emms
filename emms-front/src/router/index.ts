import { createRouter, createWebHistory, RouteRecordRaw } from 'vue-router'
import pinia from '@/store'
import { useAuthStore } from '@/store/auth'

const roleRouteMap: Record<'ADMIN' | 'USER' | 'SUPPLIER', string> = {
    ADMIN: 'admin',
    USER: 'user',
    SUPPLIER: 'supplier',
}

const routes: RouteRecordRaw[] = [
    {
        path: '/',
        redirect: { name: 'login' },
    },
    {
        path: '/login',
        name: 'login',
        component: () => import('@/views/LoginView.vue'),
        meta: { public: true },
    },
    {
        path: '/admin',
        name: 'admin',
        component: () => import('@/views/AdminDashboard.vue'),
        meta: { requiresAuth: true, role: 'ADMIN' },
    },
    {
        path: '/user',
        name: 'user',
        component: () => import('@/views/UserDashboard.vue'),
        meta: { requiresAuth: true, role: 'USER' },
    },
    {
        path: '/supplier',
        name: 'supplier',
        component: () => import('@/views/SupplierDashboard.vue'),
        meta: { requiresAuth: true, role: 'SUPPLIER' },
    },
]

const router = createRouter({
    history: createWebHistory(),
    routes,
})

router.beforeEach((to, _from, next) => {
    const auth = useAuthStore(pinia)
    if (to.meta.public) {
        return next()
    }
    if (to.meta.requiresAuth && !auth.isAuthenticated) {
        return next({ name: 'login' })
    }
    if (to.meta.role && auth.role !== to.meta.role) {
        return next({ name: roleRouteMap[auth.role] })
    }
    return next()
})

export default router

