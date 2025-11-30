<template>
  <div class="dashboard-layout">
    <aside class="sidebar">
      <div class="sidebar-header">
        <div class="role-tag">供应商</div>
        <h2>{{ authStore.user?.username || '未登录' }}</h2>
        <p class="muted-text">我的供应商：{{ supplierName }}</p>
      </div>
      <el-menu class="sidebar-menu" default-active="section-components">
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
          <h1>供应商工作台</h1>
          <p class="muted-text">管理自己提供的电子器件，操作需管理员审核后生效</p>
        </div>
        <div class="header-actions">
          <span>当前用户：{{ authStore.user?.username }}</span>
          <el-button size="small" type="primary" @click="openCreate">新增申请</el-button>
          <el-button size="small" @click="authStore.logout">退出登录</el-button>
        </div>
      </header>

      <section ref="componentsSectionRef" id="section-components" class="section-card">
        <el-card shadow="never">
          <template #header>
            <div class="card-header">
              <span>我的元器件（已通过）</span>
              <div class="table-actions">
                <el-input
                  v-model="approvedKeyword"
                  size="small"
                  clearable
                  placeholder="搜索名称/型号"
                  class="table-search"
                  @keyup.enter="handleApprovedSearch"
                />
                <el-button size="small" @click="handleApprovedSearch">搜索</el-button>
                <el-tag size="small" type="success">共 {{ approvedComponents.length }} 条</el-tag>
              </div>
            </div>
          </template>
          <el-table
            :data="approvedComponents"
            :loading="loading.list"
            empty-text="暂无元器件"
            stripe
          >
            <el-table-column label="名称">
              <template #default="{ row }">
                <span class="component-name" @click="previewImage(row.image)">
                  {{ row.name }}
                  <el-icon v-if="row.image" class="icon-inline">
                    <Picture />
                  </el-icon>
                </span>
              </template>
            </el-table-column>
            <el-table-column prop="model" label="型号" />
            <el-table-column prop="unitPrice" label="单价" width="120">
              <template #default="{ row }">
                {{ formatPrice(row.unitPrice) }}
              </template>
            </el-table-column>
            <el-table-column prop="stockQuantity" label="数量" width="120">
              <template #default="{ row }">
                {{ row.stockQuantity ?? 0 }}
              </template>
            </el-table-column>
            <el-table-column label="规格">
              <template #default="{ row }">
                {{ row.specification?.parameters || '-' }}
              </template>
            </el-table-column>
            <el-table-column label="状态" width="120">
              <template #default="{ row }">
                <el-tag :type="getStatusTagType(getComponentStatus(row))">
                  {{ formatStatus(getComponentStatus(row)) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="180">
              <template #default="{ row }">
                <el-button size="small" link type="primary" @click="openEdit(row)">编辑</el-button>
                <el-popconfirm title="提交删除申请？" @confirm="handleDelete(row.id)">
                  <template #reference>
                    <el-button size="small" link type="danger">删除</el-button>
                  </template>
                </el-popconfirm>
              </template>
            </el-table-column>
          </el-table>
          <el-alert
            class="mt-12"
            type="info"
            show-icon
            title="每次新增、修改或删除元器件都会提交审批，审批通过后才会反映在列表中。"
          />
        </el-card>
        <el-card shadow="never" class="mt-16">
          <template #header>
            <div class="card-header">
              <span>待审核申请</span>
              <div class="table-actions">
                <el-input
                  v-model="pendingKeyword"
                  size="small"
                  clearable
                  placeholder="搜索名称/型号"
                  class="table-search"
                  @keyup.enter="handlePendingSearch"
                />
                <el-button size="small" @click="handlePendingSearch">搜索</el-button>
                <el-tag size="small" type="info">共 {{ pendingComponents.length }} 条</el-tag>
              </div>
            </div>
          </template>
          <el-table :data="pendingComponents" empty-text="暂无待审核申请" stripe>
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
                {{ formatPrice(row.unitPrice) }}
              </template>
            </el-table-column>
            <el-table-column prop="stockQuantity" label="数量" width="120">
              <template #default="{ row }">
                {{ row.stockQuantity ?? 0 }}
              </template>
            </el-table-column>
            <el-table-column label="状态" width="120">
              <template #default="{ row }">
                <el-tag :type="getStatusTagType(getComponentStatus(row))">
                  {{ formatStatus(getComponentStatus(row)) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="200">
              <template #default="{ row }">
                <el-button size="small" link type="primary" @click="openEdit(row)">编辑</el-button>
                <el-popconfirm title="撤回该申请？" @confirm="handleDelete(row.id)">
                  <template #reference>
                    <el-button size="small" link type="danger">删除</el-button>
                  </template>
                </el-popconfirm>
              </template>
            </el-table-column>
          </el-table>
        </el-card>

        <el-card shadow="never" class="mt-16">
          <template #header>
            <div class="card-header">
              <span>审核失败记录</span>
              <div class="table-actions">
                <el-input
                  v-model="rejectedKeyword"
                  size="small"
                  clearable
                  placeholder="搜索名称/型号"
                  class="table-search"
                  @keyup.enter="handleRejectedSearch"
                />
                <el-button size="small" @click="handleRejectedSearch">搜索</el-button>
                <el-tag size="small" type="danger">共 {{ rejectedComponents.length }} 条</el-tag>
              </div>
            </div>
          </template>
          <el-table :data="rejectedComponents" empty-text="暂无审核失败记录" stripe>
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
                {{ formatPrice(row.unitPrice) }}
              </template>
            </el-table-column>
            <el-table-column prop="stockQuantity" label="数量" width="120">
              <template #default="{ row }">
                {{ row.stockQuantity ?? 0 }}
              </template>
            </el-table-column>
            <el-table-column label="状态" width="120">
              <template #default="{ row }">
                <el-tag :type="getStatusTagType(getComponentStatus(row))" effect="plain">
                  {{ formatStatus(getComponentStatus(row)) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="200">
              <template #default="{ row }">
                <el-button size="small" link type="primary" @click="openEdit(row)">再次提交</el-button>
                <el-popconfirm title="删除该记录？" @confirm="handleDelete(row.id)">
                  <template #reference>
                    <el-button size="small" link type="danger">删除</el-button>
                  </template>
                </el-popconfirm>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </section>

      <section ref="guideSectionRef" id="section-guide" class="section-card">
        <el-card shadow="never">
          <template #header>
            <span>操作说明</span>
          </template>
          <ul class="guide-list">
            <li>· 新增申请：填写完整信息后提交，等待管理员审核。</li>
            <li>· 修改申请：点击编辑按钮，可调整名称、型号、价格和规格。</li>
            <li>· 删除申请：点击删除后会提交审批，审批通过后该元器件将被移除。</li>
            <li>· 如遇审批被拒，可联系管理员获取备注原因并重新提交。</li>
          </ul>
        </el-card>
      </section>

      <el-dialog
        v-model="dialog.visible"
        :title="dialog.isEdit ? '修改元器件（提交审批）' : '新增元器件（提交审批）'"
        width="520px"
        :body-style="{ maxHeight: '70vh', overflowY: 'auto', overflowX: 'hidden', padding: '20px' }"
        append-to-body
        :lock-scroll="true"
        class="scrollable-dialog"
      >
        <el-form :model="dialog.form" :rules="dialogRules" ref="dialogRef" label-width="120px">
          <el-form-item label="名称" prop="name">
            <el-input v-model="dialog.form.name" />
          </el-form-item>
          <el-form-item label="型号" prop="model">
            <el-input v-model="dialog.form.model" />
          </el-form-item>
          <el-form-item label="单价">
            <el-input-number
              v-model="dialog.form.unitPrice"
              :min="0"
              :step="0.1"
              :precision="2"
              style="width: 100%"
            />
          </el-form-item>
        <el-form-item label="数量">
          <el-input-number
            v-model="dialog.form.stockQuantity"
            :min="0"
            :step="1"
              style="width: 100%"
            />
          </el-form-item>
          <el-form-item label="规格说明">
            <el-input
              v-model="dialog.form.specification"
              type="textarea"
              placeholder="例如：封装、精度等"
            />
          </el-form-item>
        <el-form-item label="图片">
          <div class="upload-block">
            <el-upload
              class="upload-trigger"
              :show-file-list="false"
              accept="image/*"
              :before-upload="handleComponentImageSelect"
            >
              <el-button :loading="dialogUploadLoading" plain>上传图片</el-button>
            </el-upload>
            <div v-if="dialog.form.image" class="image-thumb">
              <el-image
                :src="buildFileUrl(dialog.form.image)"
                fit="cover"
                style="width: 120px; height: 80px"
                @click="previewImage(dialog.form.image)"
              />
              <el-button link type="danger" size="small" @click="clearDialogImage">移除</el-button>
            </div>
            <p class="hint-text">支持 JPG/PNG，最大 2MB</p>
          </div>
          </el-form-item>
        </el-form>
        <template #footer>
          <el-button @click="dialog.visible = false">取消</el-button>
          <el-button type="primary" :loading="dialog.loading" @click="submitDialog">
            提交审批
          </el-button>
        </template>
      </el-dialog>

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
import type { FormInstance, FormRules, UploadProps } from 'element-plus'
import { ElMessage } from 'element-plus'
import { Picture } from '@element-plus/icons-vue'
import { fileAPI, supplierComponentAPI } from '@/api'
import { useAuthStore } from '@/store/auth'

type ComponentStatus = 'PENDING' | 'APPROVED' | 'REJECTED'

interface SupplierComponent {
  id: number
  name: string
  model: string
  unitPrice?: number
  image?: string
  stockQuantity?: number
  status?: ComponentStatus
  specification?: { parameters?: string }
}

const authStore = useAuthStore()
const apiBaseUrl = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api'
const MAX_IMAGE_SIZE = 2 * 1024 * 1024
const buildFileUrl = (fileName?: string) => (fileName ? `${apiBaseUrl}/files/${fileName}` : '')
const imagePreview = reactive({ visible: false, url: '' })
const dialogUploadLoading = ref(false)
const components = ref<SupplierComponent[]>([])
const loading = reactive({ list: false })
const dialogRef = ref<FormInstance>()
const componentsSectionRef = ref<HTMLElement | null>(null)
const guideSectionRef = ref<HTMLElement | null>(null)
const approvedKeyword = ref('')
const pendingKeyword = ref('')
const rejectedKeyword = ref('')

const navItems = [
  { key: 'section-components', label: '我的元器件' },
  { key: 'section-guide', label: '操作说明' },
]

const sectionMap: Record<string, Ref<HTMLElement | null>> = {
  'section-components': componentsSectionRef,
  'section-guide': guideSectionRef,
}

const getComponentStatus = (component: SupplierComponent): ComponentStatus =>
  (component.status || 'PENDING') as ComponentStatus

const statusLabelMap: Record<ComponentStatus, string> = {
  PENDING: '待审核',
  APPROVED: '已通过',
  REJECTED: '已驳回',
}

const statusTagTypeMap: Record<ComponentStatus, 'info' | 'success' | 'danger'> = {
  PENDING: 'info',
  APPROVED: 'success',
  REJECTED: 'danger',
}

const formatStatus = (status: ComponentStatus) => statusLabelMap[status] || status
const getStatusTagType = (status: ComponentStatus) => statusTagTypeMap[status] || 'info'

const filterComponentsByStatus = (status: ComponentStatus, keyword: string) => {
  const normalized = keyword.trim().toLowerCase()
  return components.value.filter((item) => {
    const actualStatus = getComponentStatus(item)
    if (actualStatus !== status) return false
    if (!normalized) return true
    return (
      item.name.toLowerCase().includes(normalized) ||
      item.model.toLowerCase().includes(normalized)
    )
  })
}

const approvedComponents = computed(() => filterComponentsByStatus('APPROVED', approvedKeyword.value))
const pendingComponents = computed(() => filterComponentsByStatus('PENDING', pendingKeyword.value))
const rejectedComponents = computed(() => filterComponentsByStatus('REJECTED', rejectedKeyword.value))
const supplierName = computed(() => authStore.user?.supplier?.name || '未绑定')

const ensureSupplierProfile = async () => {
  if (typeof authStore.fetchSupplierProfile === 'function') {
    await authStore.fetchSupplierProfile()
  }
}

const handleApprovedSearch = () => {
  approvedKeyword.value = approvedKeyword.value.trim()
}

const handlePendingSearch = () => {
  pendingKeyword.value = pendingKeyword.value.trim()
}

const handleRejectedSearch = () => {
  rejectedKeyword.value = rejectedKeyword.value.trim()
}

const dialog = reactive({
  visible: false,
  isEdit: false,
  loading: false,
  form: {
    id: 0,
    name: '',
    model: '',
    unitPrice: 0,
    stockQuantity: 0,
    specification: '',
    image: '',
  },
})

const dialogRules: FormRules<typeof dialog.form> = {
  name: [{ required: true, message: '请输入名称', trigger: 'blur' }],
  model: [{ required: true, message: '请输入型号', trigger: 'blur' }],
}

const formatPrice = (value?: number) => {
  if (value === undefined || value === null) return '-'
  return `￥${value.toFixed(2)}`
}

const handleNavClick = (key: string) => {
  const target = sectionMap[key]?.value
  if (target) {
    target.scrollIntoView({ behavior: 'smooth', block: 'start' })
  }
}

const previewImage = (image?: string) => {
  if (!image) {
    ElMessage.info('该元器件暂无图片')
    return
  }
  imagePreview.url = buildFileUrl(image)
  imagePreview.visible = true
}

const clearDialogImage = () => {
  dialog.form.image = ''
}

const handleComponentImageSelect: UploadProps['beforeUpload'] = async (rawFile) => {
  if (rawFile.size > MAX_IMAGE_SIZE) {
    ElMessage.error('图片大小不能超过 2MB')
    return false
  }
  dialogUploadLoading.value = true
  try {
    const fileKey = (await fileAPI.upload(rawFile)) as string
    dialog.form.image = fileKey
    ElMessage.success('图片上传成功')
  } catch (error) {
    console.error(error)
    ElMessage.error('图片上传失败')
  } finally {
    dialogUploadLoading.value = false
  }
  return false
}

const fetchComponents = async () => {
  loading.list = true
  try {
    components.value =
      ((await supplierComponentAPI.getMyComponents()) as unknown as SupplierComponent[]) || []
  } catch (error) {
    const message = (error as Error)?.message || '加载失败'
    if (message.includes('未登录')) {
      ElMessage.error('登录状态已过期，请重新登录')
      authStore.logout()
    } else if (message.includes('Network Error')) {
      ElMessage.error('无法连接服务器，请稍后再试')
    } else {
      ElMessage.error(message)
    }
  } finally {
    loading.list = false
  }
}

const openCreate = () => {
  dialog.visible = true
  dialog.isEdit = false
  dialog.form = {
    id: 0,
    name: '',
    model: '',
    unitPrice: 0,
    stockQuantity: 0,
    specification: '',
    image: '',
  }
}

const openEdit = (item: SupplierComponent) => {
  dialog.visible = true
  dialog.isEdit = true
  dialog.form = {
    id: item.id,
    name: item.name,
    model: item.model,
    unitPrice: item.unitPrice || 0,
    stockQuantity: item.stockQuantity ?? 0,
    specification: item.specification?.parameters || '',
    image: item.image || '',
  }
}

const submitDialog = async () => {
  if (!dialogRef.value) return
  await dialogRef.value.validate(async (valid) => {
    if (!valid) return
    dialog.loading = true
    const payload = {
      name: dialog.form.name,
      model: dialog.form.model,
      unitPrice: dialog.form.unitPrice || undefined,
      stockQuantity: dialog.form.stockQuantity ?? 0,
      image: dialog.form.image || undefined,
      specification: dialog.form.specification
        ? { parameters: dialog.form.specification }
        : undefined,
    }
    try {
      if (dialog.isEdit) {
        await supplierComponentAPI.requestUpdate(dialog.form.id, payload)
        ElMessage.success('更新申请已提交，等待审核')
      } else {
        await supplierComponentAPI.requestCreate(payload)
        ElMessage.success('新增申请已提交，等待审核')
      }
      dialog.visible = false
      fetchComponents()
    } finally {
      dialog.loading = false
    }
  })
}

const handleDelete = async (id: number) => {
  try {
    await supplierComponentAPI.requestDelete(id)
    ElMessage.success('删除申请已提交，等待审核')
    fetchComponents()
  } catch (error) {
    console.error(error)
  }
}

onMounted(() => {
  ensureSupplierProfile()
  fetchComponents()
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
  position: sticky;
  top: 0;
  height: 100vh;
  display: flex;
  flex-direction: column;
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

.table-actions {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
  align-items: center;
}

.table-search {
  width: 200px;
}

.mt-16 {
  margin-top: 16px;
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
}

.upload-block {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.image-thumb {
  margin-top: 8px;
}

.hint-text {
  font-size: 12px;
  color: #94a3b8;
}

.image-preview-wrapper {
  text-align: center;
}

.page-header {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 24px;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 8px;
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

.muted-text {
  margin: 4px 0 0;
  color: #6b7280;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.guide-list {
  margin: 0;
  padding-left: 16px;
  color: #475569;
  line-height: 1.8;
}

.mt-12 {
  margin-top: 12px;
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

