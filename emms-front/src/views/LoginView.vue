<template>
  <div class="login-layout">
    <section class="login-hero">
      <div>
        <p class="tag">EMMS · 电子物料管理系统</p>
        <h1>集中化管理，安全高效</h1>
        <p class="subtitle">
          管理员可掌控库存、审批出入库，普通用户可自助申请与查询。
        </p>
        <ul>
          <li>· 实时库存监控与告警</li>
          <li>· 多角色权限分离</li>
          <li>· 支持 Element Plus 生态</li>
        </ul>
      </div>
    </section>
    <section class="login-panel">
      <el-card class="login-card" shadow="always">
        <div class="panel-header">
          <el-radio-group
            v-model="activeRole"
            size="large"
            class="role-switch"
            @change="handleRoleChange"
          >
            <el-radio-button label="ADMIN">管理员登录</el-radio-button>
            <el-radio-button label="USER">用户登录</el-radio-button>
            <el-radio-button label="SUPPLIER">供应商登录</el-radio-button>
          </el-radio-group>
          <p class="role-tip">{{ currentRoleTip }}</p>
        </div>

        <el-form
          ref="formRef"
          :model="form"
          :rules="rules"
          label-position="top"
          @keyup.enter="handleSubmit"
        >
          <el-form-item :label="usernameLabel" prop="username">
            <el-input
              v-model="form.username"
              :placeholder="`请输入${usernameLabel}`"
              clearable
            />
          </el-form-item>
          <el-form-item label="密码" prop="password">
            <el-input
              v-model="form.password"
              type="password"
              placeholder="请输入密码"
              show-password
            />
          </el-form-item>
          <el-form-item
            v-if="activeRole === 'USER' && showRegisterFields"
            label="邮箱"
            prop="email"
          >
            <el-input v-model="form.email" placeholder="请输入邮箱（可选）" />
          </el-form-item>
          <el-form-item
            v-if="activeRole === 'USER' && showRegisterFields"
            label="手机号"
            prop="phone"
          >
            <el-input v-model="form.phone" placeholder="请输入手机号（可选）" />
          </el-form-item>
          <el-form-item class="action-row">
            <el-button
              v-if="!showRegisterFields"
              type="primary"
              class="w-full"
              :loading="loading"
              @click="handleSubmit"
            >
              {{ buttonLabel }}
            </el-button>
            <el-button
              v-else
              type="primary"
              class="w-full"
              :loading="loading"
              @click="handleConfirmRegister"
            >
              确认注册
            </el-button>
          </el-form-item>
        </el-form>

        <div class="helper-text">
          <el-link
            v-if="activeRole === 'USER' && !showRegisterFields"
            type="primary"
            :underline="false"
            @click="handleRegister"
          >
            用户注册
          </el-link>
          <el-link
            v-if="activeRole === 'USER' && showRegisterFields"
            type="primary"
            :underline="false"
            @click="handleCancelRegister"
          >
            返回登录
          </el-link>
        </div>
      </el-card>
    </section>
  </div>
</template>

<script setup lang="ts">
import { computed, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import type { FormInstance, FormRules } from 'element-plus'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '@/store/auth'
import { authAPI } from '@/api'

type RoleType = 'ADMIN' | 'USER' | 'SUPPLIER'

const router = useRouter()
const authStore = useAuthStore()
const formRef = ref<FormInstance>()
const loading = ref(false)
const activeRole = ref<RoleType>('ADMIN')
const showRegisterFields = ref(false)

const roleDescriptions: Record<RoleType, string> = {
  ADMIN: '拥有库存配置、审批与用户管理权限。',
  USER: '可自助发起领用、查看自身审批状态。',
  SUPPLIER: '可提交元器件申请，等待管理员审核。',
}
const roleTextMap: Record<RoleType, string> = {
  ADMIN: '管理员',
  USER: '普通用户',
  SUPPLIER: '供应商',
}
const roleRouteMap: Record<RoleType, string> = {
  ADMIN: 'admin',
  USER: 'user',
  SUPPLIER: 'supplier',
}

const form = reactive({
  username: '',
  password: '',
  email: '',
  phone: '',
})

const rules: FormRules<typeof form> = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
}

const currentRoleTip = computed(() => roleDescriptions[activeRole.value])
const usernameLabel = computed(() => {
  if (activeRole.value === 'ADMIN') return '管理员账号'
  if (activeRole.value === 'SUPPLIER') return '供应商账号'
  return '用户账号'
})
const buttonLabel = computed(() => `${roleTextMap[activeRole.value]}登录`)
// 后端接口提示已移除，保留计算逻辑时可按需恢复
// const backendEndpoint = computed(() => `${import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api'}/auth/login`)

const handleRoleChange = () => {
  form.username = ''
  form.password = ''
  form.email = ''
  form.phone = ''
  showRegisterFields.value = false
}

const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    loading.value = true
    try {
      const result = await authStore.login({ ...form })
      const actualRole = result.user.role as RoleType
      const targetRoute = roleRouteMap[actualRole]
      if (!targetRoute) {
        ElMessage.error('该账号尚未分配可访问的门户，请联系管理员')
        return
      }
      if (actualRole !== activeRole.value) {
        ElMessage.warning(`检测到该账号属于${roleTextMap[actualRole]}，已自动跳转对应门户`)
      } else {
        ElMessage.success('登录成功')
      }
      await router.push({ name: targetRoute })
    } catch (error) {
      console.error(error)
    } finally {
      loading.value = false
    }
  })
}

const handleRegister = () => {
  if (activeRole.value !== 'USER') {
    ElMessage.warning('仅支持普通用户自助注册')
    return
  }
  // 直接显示邮箱和手机号字段
  showRegisterFields.value = true
}

const handleCancelRegister = () => {
  showRegisterFields.value = false
  form.email = ''
  form.phone = ''
}

const handleConfirmRegister = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    loading.value = true
    try {
      await authAPI.register({
        username: form.username,
        password: form.password,
        email: form.email?.trim() || undefined,
        phone: form.phone?.trim() || undefined,
      })
      ElMessage.success('注册成功，请使用该账号登录')
      showRegisterFields.value = false
      form.username = ''
      form.password = ''
      form.email = ''
      form.phone = ''
    } catch (error) {
      console.error(error)
    } finally {
      loading.value = false
    }
  })
}
</script>

<style scoped>
.login-layout {
  min-height: 100vh;
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(320px, 1fr));
  background: transparent;
}

.login-hero {
  padding: 64px 48px;
  display: flex;
  align-items: center;
  color: #0f172a;
}

.login-hero h1 {
  font-size: 40px;
  margin: 12px 0;
}

.login-hero .subtitle {
  font-size: 16px;
  color: #475569;
  margin-bottom: 24px;
  max-width: 420px;
}

.login-hero ul {
  list-style: none;
  padding: 0;
  margin: 0;
  color: #334155;
}

.login-panel {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 48px 24px;
}

.login-card {
  width: 100%;
  max-width: 420px;
  border-radius: 16px;
}

.tag {
  display: inline-block;
  padding: 6px 12px;
  background: rgba(59, 130, 246, 0.12);
  color: #1d4ed8;
  border-radius: 999px;
  font-size: 13px;
}

.panel-header {
  text-align: center;
  margin-bottom: 32px;
}

.role-switch :deep(.el-radio-button__inner) {
  width: 120px;
  text-align: center;
}

.role-tip {
  margin-top: 12px;
  font-size: 13px;
  color: #475569;
}

.action-row {
  margin-top: 12px;
}

.helper-text {
  margin-top: 12px;
  display: flex;
  justify-content: space-between;
  font-size: 12px;
  color: #94a3b8;
}

.w-full {
  width: 100%;
}

@media (max-width: 768px) {
  .login-layout {
    grid-template-columns: 1fr;
  }

  .login-hero {
    padding: 32px 24px 0;
  }

  .login-panel {
    padding: 24px;
  }
}
</style>

