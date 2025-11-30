<template>
  <div class="dashboard-layout">
    <aside class="sidebar">
      <div class="sidebar-header">
        <div class="role-tag">用户</div>
        <h2>{{ authStore.user?.username || '未登录' }}</h2>
        <p class="muted-text">个人工作台</p>
      </div>
      <el-menu class="sidebar-menu" default-active="section-profile">
        <el-menu-item
          v-for="item in navItems"
          :key="item.key"
          :index="item.key"
          @click="handleNavClick(item.key)"
        >
          {{ item.label }}
        </el-menu-item>
      </el-menu>
    </aside>

    <div class="screen content">
      <header class="page-header">
        <div>
          <h1>个人工作台</h1>
          <p class="muted-text">查看元器件并提交领用申请</p>
        </div>
        <div class="header-actions">
          <span>当前用户：{{ authStore.user?.username }}</span>
          <el-button size="small" @click="authStore.logout">退出登录</el-button>
        </div>
      </header>

      <section ref="profileSectionRef" id="section-profile" class="section-card">
        <el-card shadow="never" class="profile-card">
          <template #header>
            <span>个人信息</span>
          </template>
          <el-descriptions :column="2" border>
            <el-descriptions-item label="用户名">
              {{ authStore.user?.username || '-' }}
            </el-descriptions-item>
            <el-descriptions-item label="角色">
              {{ formatRole(authStore.user?.role) }}
            </el-descriptions-item>
            <el-descriptions-item label="邮箱">
              {{ authStore.user?.email || '未填写' }}
            </el-descriptions-item>
            <el-descriptions-item label="联系电话">
              {{ authStore.user?.phone || '未填写' }}
            </el-descriptions-item>
          </el-descriptions>
        </el-card>
      </section>

      <section ref="inventorySectionRef" id="section-inventory" class="section-card">
        <el-card shadow="never">
          <template #header>
            <div class="card-header">
              <span>可用元器件</span>
              <div class="table-actions">
                <el-input
                  v-model="componentKeyword"
                  size="small"
                  clearable
                  placeholder="搜索名称/型号"
                  class="table-search"
                  @keyup.enter="handleComponentSearch"
                />
                <el-button size="small" @click="handleComponentSearch">搜索</el-button>
              </div>
            </div>
          </template>
          <el-table
            :data="filteredComponents"
            :loading="loading.types"
            empty-text="暂无数据"
            stripe
          >
            <el-table-column label="名称/型号">
              <template #default="{ row }">
                <div class="component-name" @click="previewImage(row.image)">
                  {{ row.name }}（{{ row.model }}）
                  <el-icon v-if="row.image" class="icon-inline">
                    <Picture />
                  </el-icon>
                </div>
              </template>
            </el-table-column>
            <el-table-column prop="unitPrice" label="单价" width="120">
              <template #default="{ row }">
                {{ row.unitPrice ?? '-' }}
              </template>
            </el-table-column>
          <el-table-column prop="stockQuantity" label="库存" width="120">
            <template #default="{ row }">
              {{ row.stockQuantity ?? 0 }}
            </template>
          </el-table-column>
            <el-table-column prop="parameters" label="规格" />
          </el-table>
        </el-card>
      </section>

      <section ref="requestSectionRef" id="section-request" class="section-card">
        <el-card shadow="never" class="mb-16">
          <template #header>
            <span>领用申请</span>
          </template>
          <el-form
            ref="usageRef"
            :model="usageForm"
            :rules="usageRules"
            label-position="top"
          >
            <el-form-item label="选择元器件" prop="model">
              <el-select
                v-model="usageForm.model"
                placeholder="请选择型号"
                filterable
                @change="handleModelChange"
              >
                <el-option
                  v-for="item in componentTypes"
                  :key="item.id"
                  :label="`${item.name} (${item.model})`"
                  :value="item.model"
                />
              </el-select>
            </el-form-item>
            <el-form-item label="数量" prop="quantity">
              <el-input-number
                v-model="usageForm.quantity"
                :min="1"
                style="width: 100%"
              />
            </el-form-item>
            <el-form-item label="领用人" prop="takenBy">
              <el-input v-model="usageForm.takenBy" placeholder="请输入领用人" />
            </el-form-item>
            <el-form-item>
              <el-button
                type="primary"
                class="w-full"
                :loading="loading.submit"
                @click="submitUsage"
              >
                提交申请
              </el-button>
            </el-form-item>
          </el-form>
          <el-alert
            v-if="inventoryInfo"
            type="info"
            show-icon
            :title="`当前库存：${inventoryInfo.currentInventory}`"
            class="mt-12"
          />
        </el-card>
      </section>

      <section ref="historySectionRef" id="section-history" class="section-card">
        <el-card shadow="never">
          <template #header>
            <div class="card-header">
              <span>我的领用记录</span>
              <div class="table-actions">
                <el-input
                  v-model="historyKeyword"
                  size="small"
                  clearable
                  placeholder="搜索型号/领用人"
                  class="table-search"
                  @keyup.enter="handleHistorySearch"
                />
                <el-button size="small" @click="handleHistorySearch">搜索</el-button>
              </div>
            </div>
          </template>
          <el-timeline v-if="filteredHistory.length" style="max-height: 320px; overflow: auto">
            <el-timeline-item
              v-for="item in filteredHistory"
              :key="item.id"
              :timestamp="formatDate(item.takeDate)"
            >
              <div style="display: flex; align-items: center; gap: 8px; flex-wrap: wrap">
                <span>
                  {{ item.componentType?.name ?? '-' }}（{{ item.componentType?.model ?? '-' }}）
                  × {{ item.quantity }}（{{ item.takenBy }}）
                </span>
                <el-tag
                  v-if="item.status"
                  :type="
                    item.status === 'APPROVED'
                      ? 'success'
                      : item.status === 'REJECTED'
                        ? 'danger'
                        : 'warning'
                  "
                  size="small"
                >
                  {{
                    item.status === 'APPROVED'
                      ? '已通过'
                      : item.status === 'REJECTED'
                        ? '已拒绝'
                        : '待审核'
                  }}
                </el-tag>
                <el-tag v-if="item.rejectionReason" type="info" size="small">
                  拒绝原因：{{ item.rejectionReason }}
                </el-tag>
              </div>
            </el-timeline-item>
          </el-timeline>
          <el-empty v-else description="暂无记录" />
        </el-card>
      </section>

      <el-dialog
        v-model="imagePreview.visible"
        title="图片预览"
        width="420px"
        append-to-body
        :body-style="{ maxHeight: '70vh', overflowY: 'auto', overflowX: 'hidden', padding: '20px' }"
        :lock-scroll="true"
        class="scrollable-dialog"
      >
        <div v-if="imagePreview.url" class="image-preview-wrapper">
          <el-image :src="imagePreview.url" fit="contain" style="width: 100%" />
        </div>
        <div v-else class="hint-text">暂无图片</div>
        <template #footer>
          <el-button @click="imagePreview.visible = false">关闭</el-button>
        </template>
      </el-dialog>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref, onMounted, computed } from 'vue'
import type { Ref } from 'vue'
import type { FormInstance, FormRules } from 'element-plus'
import { ElMessage } from 'element-plus'
import dayjs from 'dayjs'
import {
  historyAPI,
  statisticsAPI,
  userComponentTypeAPI,
  stockAPI,
} from '@/api'
import { useAuthStore } from '@/store/auth'
import { Picture } from '@element-plus/icons-vue'

const apiBaseUrl = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api'
const buildFileUrl = (fileName?: string) => (fileName ? `${apiBaseUrl}/files/${fileName}` : '')
const imagePreview = reactive({ visible: false, url: '' })

interface ComponentTypeDTO {
  id: number
  name: string
  model: string
  unitPrice?: number
  stockQuantity?: number
  status?: string
  parameters?: string
  image?: string
}

interface StockOutRecord {
  id: number
  quantity: number
  takenBy: string
  takeDate?: string
  status?: 'PENDING' | 'APPROVED' | 'REJECTED'
  approvedBy?: string
  rejectionReason?: string
  componentType?: {
    name?: string
    model?: string
  }
}

const authStore = useAuthStore()
const componentTypes = ref<ComponentTypeDTO[]>([])
const history = ref<StockOutRecord[]>([])
const inventoryInfo = ref<{ currentInventory: number } | null>(null)
const componentKeyword = ref('')
const historyKeyword = ref('')
const profileSectionRef = ref<HTMLElement | null>(null)
const inventorySectionRef = ref<HTMLElement | null>(null)
const requestSectionRef = ref<HTMLElement | null>(null)
const historySectionRef = ref<HTMLElement | null>(null)

const loading = reactive({
  types: false,
  submit: false,
})

const navItems = [
  { key: 'section-profile', label: '个人信息' },
  { key: 'section-inventory', label: '元器件列表' },
  { key: 'section-request', label: '领用申请' },
  { key: 'section-history', label: '领用记录' },
]

const sectionMap: Record<string, Ref<HTMLElement | null>> = {
  'section-profile': profileSectionRef,
  'section-inventory': inventorySectionRef,
  'section-request': requestSectionRef,
  'section-history': historySectionRef,
}

const usageForm = reactive({
  model: '',
  quantity: 1,
  takenBy: authStore.user?.username || '',
})

const usageRules: FormRules = {
  model: [{ required: true, message: '请选择型号', trigger: 'change' }],
  quantity: [{ required: true, message: '请输入数量', trigger: 'change' }],
  takenBy: [{ required: true, message: '请输入领用人', trigger: 'blur' }],
}

const usageRef = ref<FormInstance>()

const handleNavClick = (key: string) => {
  const target = sectionMap[key]?.value
  if (target) {
    target.scrollIntoView({ behavior: 'smooth', block: 'start' })
  }
}

const filteredComponents = computed(() =>
  componentTypes.value.filter((item) => {
    if (!componentKeyword.value) return true
    const keyword = componentKeyword.value.toLowerCase()
    return (
      item.name.toLowerCase().includes(keyword) ||
      item.model.toLowerCase().includes(keyword)
    )
  })
)

const filteredHistory = computed(() =>
  history.value.filter((item) => {
    if (!historyKeyword.value) return true
    const keyword = historyKeyword.value.toLowerCase()
    return (
      item.componentType?.model?.toLowerCase().includes(keyword) ||
      item.takenBy?.toLowerCase().includes(keyword)
    )
  })
)

const handleComponentSearch = () => {
  componentKeyword.value = componentKeyword.value.trim()
}

const handleHistorySearch = () => {
  historyKeyword.value = historyKeyword.value.trim()
}

const fetchTypes = async () => {
  loading.types = true
  try {
    const list = (await userComponentTypeAPI.getTypesForSelect()) as unknown as
      | ComponentTypeDTO[]
      | undefined
    componentTypes.value = list || []
  } finally {
    loading.types = false
  }
}

const fetchHistory = async () => {
  if (!authStore.user?.username) {
    history.value = []
    return
  }
  const list = (await historyAPI.getUsageHistoryByUser(authStore.user.username)) as unknown as
    | StockOutRecord[]
    | undefined
  history.value = (list || []).sort(
    (a, b) =>
      new Date(b.takeDate || '').getTime() - new Date(a.takeDate || '').getTime()
  )
}

const handleModelChange = async () => {
  if (!usageForm.model) {
    inventoryInfo.value = null
    return
  }
  try {
    const result = (await statisticsAPI.getInventoryByModel(
      usageForm.model
    )) as unknown as { currentInventory: number }
    inventoryInfo.value = result
  } catch (error: any) {
    console.error('查询库存失败:', error)
    inventoryInfo.value = null
    // 不显示错误消息，因为这只是查询库存信息，不影响提交
  }
}

const submitUsage = async () => {
  if (!usageRef.value) return
  await usageRef.value.validate(async (valid) => {
    if (!valid) return
    loading.submit = true
    try {
      await stockAPI.stockOut({
        model: usageForm.model,
        quantity: usageForm.quantity,
        takenBy: usageForm.takenBy,
      })
      ElMessage.success('提交成功')
      fetchHistory()
      handleModelChange()
    } finally {
      loading.submit = false
    }
  })
}

const formatDate = (value?: string) => {
  if (!value) return '-'
  return dayjs(value).format('YYYY-MM-DD HH:mm')
}

const formatRole = (role?: 'ADMIN' | 'USER') => {
  if (!role) return '-'
  return role === 'ADMIN' ? '管理员' : '普通用户'
}

const previewImage = (image?: string) => {
  if (!image) {
    ElMessage.info('该元器件暂无图片')
    return
  }
  imagePreview.url = buildFileUrl(image)
  imagePreview.visible = true
}

onMounted(() => {
  fetchTypes()
  fetchHistory()
})
</script>

<style scoped>
.dashboard-layout {
  display: flex;
  min-height: 100vh;
  background: transparent;
}

.sidebar {
  width: 260px;
  background: var(--brand-primary);
  color: #fff;
  border-right: none;
  padding: 24px 16px;
  display: flex;
  flex-direction: column;
  position: sticky;
  top: 0;
  height: 100vh;
  box-shadow: 6px 0 20px rgba(0, 0, 0, 0.08);
}

.sidebar-header {
  text-align: center;
  margin-bottom: 24px;
  color: #fff;
}

.role-tag {
  display: inline-block;
  padding: 4px 12px;
  background: rgba(255, 255, 255, 0.2);
  color: #fff;
  border-radius: 999px;
  font-size: 12px;
  margin-bottom: 8px;
}

.sidebar-menu {
  border: none;
  flex: 1;
}

.sidebar-menu :deep(.el-menu) {
  background: transparent;
  border: none;
}

.sidebar-menu :deep(.el-menu-item) {
  color: #1f2933;
}

.sidebar-menu :deep(.el-menu-item.is-active),
.sidebar-menu :deep(.el-menu-item:hover) {
  background: rgba(255, 255, 255, 0.35);
  color: var(--brand-primary);
}
.sidebar-menu {
  border: none;
  flex: 1;
}

.content {
  flex: 1;
  padding: 24px;
}

.section-card {
  margin-bottom: 24px;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.table-search {
  width: 180px;
}

.table-actions {
  display: flex;
  gap: 8px;
  align-items: center;
}

.page-header {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 24px;
}

.muted-text {
  margin: 4px 0 0;
  color: #6b7280;
}

.profile-card {
  margin-bottom: 0;
}

.w-full {
  width: 100%;
}

.mb-16 {
  margin-bottom: 16px;
}

.mt-12 {
  margin-top: 12px;
}

.logout-btn {
  background: #fff;
  color: #1f2933;
  border-color: transparent;
}

.logout-btn:hover {
  background: rgba(255, 255, 255, 0.85);
  color: #1f2933;
  border-color: transparent;
  box-shadow: none;
}

.component-name {
  color: #2563eb;
  cursor: pointer;
  display: inline-flex;
  align-items: center;
  gap: 4px;
}

.component-name:hover {
  text-decoration: underline;
}

.icon-inline {
  font-size: 16px;
  color: #2563eb;
}

.image-preview-wrapper {
  text-align: center;
}

.hint-text {
  font-size: 12px;
  color: #94a3b8;
  text-align: center;
}

.scrollable-dialog :deep(.el-dialog__body) {
  max-height: 70vh;
  overflow-y: auto;
  overflow-x: hidden;
  padding: 20px;
}

.scrollable-dialog :deep(.el-dialog__body::-webkit-scrollbar) {
  width: 6px;
}

.scrollable-dialog :deep(.el-dialog__body::-webkit-scrollbar-track) {
  background: #f1f1f1;
  border-radius: 3px;
}

.scrollable-dialog :deep(.el-dialog__body::-webkit-scrollbar-thumb) {
  background: #c1c1c1;
  border-radius: 3px;
}

.scrollable-dialog :deep(.el-dialog__body::-webkit-scrollbar-thumb:hover) {
  background: #a8a8a8;
}

@media (max-width: 992px) {
  .dashboard-layout {
    flex-direction: column;
  }

  .sidebar {
    position: relative;
    width: 100%;
    height: auto;
    border-right: none;
    border-bottom: 1px solid #e5e7eb;
  }
}
</style>

