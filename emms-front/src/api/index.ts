import axios from 'axios'
import { ElMessage } from 'element-plus'

const api = axios.create({
    baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api',
    timeout: 10000,
    headers: {
        'Content-Type': 'application/json'
    }
})

// 请求拦截器
api.interceptors.request.use(config => {
    const sessionId = localStorage.getItem('sessionId')
    if (sessionId) {
        config.headers['Session-Id'] = sessionId
    }
    console.log('发送请求:', config.method?.toUpperCase(), config.url)
    return config
})

// 响应拦截器
api.interceptors.response.use(
    response => {
        console.log('收到响应:', response.status, response.data)
        const data = response.data
        // 处理后端的ApiResponse格式
        if (data && typeof data === 'object' && 'success' in data) {
            if (!data.success) {
                ElMessage.error(data.message || '操作失败')
                return Promise.reject(new Error(data.message || '操作失败'))
            }
            return data.data // 返回data字段中的实际数据
        }
        return data
    },
    error => {
        console.error('API 错误:', error.response?.data || error.message)
        const errorMessage = error.response?.data?.message || error.message || '网络错误'
        ElMessage.error(errorMessage)
        return Promise.reject(error)
    }
)

// 认证 API
export const authAPI = {
    login<T = any>(data: { username: string; password: string }) {
        return api.post<T>('/auth/login', data) as Promise<T>
    },
    register(data: { username: string; password: string; email?: string; phone?: string }) {
        return api.post('/auth/register', data)
    },
}

// 用户管理 API
export const userAPI = {
    getUsers: () => api.get('/admin/users'),
    createUser: (data: any) => api.post('/admin/users', data),
    updateUser: (id: number, data: any) => api.put(`/admin/users/${id}`, data),
    deleteUser: (id: number) => api.delete(`/admin/users/${id}`)
}

// 元器件类型 API (普通用户)
export const componentTypeAPI = {
    getTypes: () => api.get('/component-types'),
}

// 管理员元器件类型 API
export const adminComponentTypeAPI = {
    getAllTypes: () => api.get('/admin/component-types'),
    createType: (data: any) => api.post('/admin/component-types', data),
    updateType: (id: number, data: any) => api.put(`/admin/component-types/${id}`, data),
    deleteType: (id: number) => api.delete(`/admin/component-types/${id}`)
}

// 用户元器件类型 API (用于下拉框和搜索)
export const userComponentTypeAPI = {
    getTypesForSelect: () => api.get('/user/component-types'),
    searchTypes: (keyword: string) => api.get('/user/component-types/search', { params: { keyword } })
}

// 库存操作 API
export const stockAPI = {
    stockIn: (data: any) => api.post('/stock/in', data),
    stockOut: (data: any) => api.post('/stock/out', data)
}

// 统计 API
export const statisticsAPI = {
    getInventory: () => api.get('/statistics/inventory'),
    getInventoryByType: (id: number) => api.get(`/statistics/inventory/${id}`),
    getInventoryByModel: (model: string) =>
        api.get('/statistics/inventory/model', { params: { model } })
}

// 使用历史 API
export const historyAPI = {
    getUsageHistory: (page: number = 0, size: number = 10) =>
        api.get('/history/usage', { params: { page, size } }),
    getUsageHistoryByUser: (takenBy: string) =>
        api.get('/history/usage/search', { params: { takenBy } })
}

// 供应商管理 API
export const supplierAPI = {
    getSuppliers: () => api.get('/admin/suppliers'),
    getSupplier: (id: number) => api.get(`/admin/suppliers/${id}`),
    searchSuppliers: (keyword: string) =>
        api.get('/admin/suppliers/search', { params: { keyword } }),
    createSupplier: (data: any) => api.post('/admin/suppliers', data),
    updateSupplier: (id: number, data: any) => api.put(`/admin/suppliers/${id}`, data),
    deleteSupplier: (id: number) => api.delete(`/admin/suppliers/${id}`),
}

// 供应商端元器件 API
export const supplierComponentAPI = {
    getMyComponents: () => api.get('/supplier/components'),
    requestCreate: (data: any) => api.post('/supplier/components', data),
    requestUpdate: (id: number, data: any) => api.put(`/supplier/components/${id}`, data),
    requestDelete: (id: number) => api.delete(`/supplier/components/${id}`),
}

export const supplierSelfAPI = {
    getProfile: () => api.get('/supplier/profile')
}

export const takeApprovalAPI = {
    getPending: () => api.get('/admin/take-approvals/pending'),
    approve: (id: number, approvedBy: string) =>
        api.put(`/admin/take-approvals/${id}/approve`, null, { params: { approvedBy } }),
    reject: (id: number, approvedBy: string) =>
        api.put(`/admin/take-approvals/${id}/reject`, null, { params: { approvedBy } }),
}

// 操作审核 API
export const auditAPI = {
    getPending: () => api.get('/admin/audit/pending'),
    approve: (id: number) => api.put(`/admin/audit/${id}/approve`),
    reject: (id: number) => api.put(`/admin/audit/${id}/reject`),
}

// 文件上传 API
export const fileAPI = {
    upload: (file: File): Promise<string> => {
        const formData = new FormData()
        formData.append('file', file)
        return api.post('/files/upload', formData, {
            headers: { 'Content-Type': 'multipart/form-data' }
        }) as unknown as Promise<string>
    }
}

export default api