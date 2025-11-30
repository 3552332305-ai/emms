<template>
  <div class="dashboard-layout">
    <aside class="sidebar">
      <div class="sidebar-header">
        <div class="role-tag">管理员</div>
        <h2>{{ authStore.user?.username || '未登录' }}</h2>
        <p class="muted-text">电子物料管理</p>
      </div>
      <el-menu class="sidebar-menu" default-active="section-component">
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
        <h1>管理员控制台</h1>
        <p class="muted-text">管理元器件、处理入库并查看使用记录</p>
      </div>
      <div class="header-actions">
        <span>当前用户：{{ authStore.user?.username }}</span>
        <el-button size="small" @click="authStore.logout">退出登录</el-button>
      </div>
    </header>

      <section ref="componentSectionRef" id="section-component" class="section-card">
        <el-card shadow="never">
          <template #header>
            <div class="card-header">
              <span>元器件类型</span>
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
                <el-button size="small" type="primary" @click="openCreate">
                  新增类型
                </el-button>
              </div>
            </div>
          </template>
          <el-table
            :data="filteredComponentTypes"
            :loading="loading.types"
            stripe
            empty-text="暂无类型"
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
            <el-table-column label="型号">
              <template #default="{ row }">
                {{ row.model }}
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
            <el-table-column label="规格">
              <template #default="{ row }">
                {{ row.specification?.parameters ?? '-' }}
              </template>
            </el-table-column>
            <el-table-column label="操作" width="160">
              <template #default="{ row }">
                <el-button size="small" link type="primary" @click="openEdit(row)">
                  编辑
                </el-button>
                <el-popconfirm title="确认删除该类型？" @confirm="deleteType(row.id)">
                  <template #reference>
                    <el-button size="small" link type="danger">删除</el-button>
                  </template>
                </el-popconfirm>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </section>

      <section ref="stockSectionRef" id="section-stock" class="section-card">
        <el-row :gutter="16">
          <el-col :md="12" :sm="24">
            <el-card shadow="never" class="mb-16">
              <template #header>
                <span>元器件入库</span>
              </template>
              <el-form
                :model="stockInForm"
                :rules="stockInRules"
                ref="stockInRef"
                label-position="top"
              >
                <el-form-item label="元器件" prop="componentTypeId">
                  <el-select
                    v-model="stockInForm.componentTypeId"
                    placeholder="请选择"
                    filterable
                  >
                    <el-option
                      v-for="item in componentTypes"
                      :key="item.id"
                      :label="`${item.name} (${item.model})`"
                      :value="item.id"
                    />
                  </el-select>
                </el-form-item>
                <el-form-item label="数量" prop="quantity">
                  <el-input-number
                    v-model="stockInForm.quantity"
                    :min="1"
                    style="width: 100%"
                  />
                </el-form-item>
                <el-form-item label="采购时间" prop="purchaseDate">
                  <el-date-picker
                    v-model="stockInForm.purchaseDate"
                    type="datetime"
                    placeholder="选择时间"
                    style="width: 100%"
                  />
                </el-form-item>
                <el-form-item label="采购人" prop="purchaser">
                  <el-input v-model="stockInForm.purchaser" placeholder="请输入采购人" />
                </el-form-item>
                <el-form-item>
                  <el-button
                    type="primary"
                    class="w-full"
                    :loading="loading.stockIn"
                    @click="handleStockIn"
                  >
                    提交入库
                  </el-button>
                </el-form-item>
              </el-form>
            </el-card>
          </el-col>
          <el-col :md="12" :sm="24">
            <el-card shadow="never">
              <template #header>
                <div class="card-header">
                  <span>使用记录</span>
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
              </template>
              <el-table
                :data="filteredHistory"
                height="320"
                :loading="loading.history"
                empty-text="暂无记录"
              >
                <el-table-column label="型号">
                  <template #default="{ row }">
                    {{ row.componentType?.model ?? '-' }}
                  </template>
                </el-table-column>
                <el-table-column prop="quantity" label="数量" width="100" />
                <el-table-column prop="takenBy" label="领用人" width="120" />
                <el-table-column label="时间" width="200">
                  <template #default="{ row }">
                    {{ formatDate(row.takeDate) }}
                  </template>
                </el-table-column>
              </el-table>
            </el-card>
          </el-col>
        </el-row>
      </section>

      <section ref="supplierSectionRef" id="section-supplier" class="section-card">
    <el-card shadow="never" class="supplier-card">
      <template #header>
        <div class="card-header">
          <span>供应商管理</span>
          <div class="supplier-actions">
            <el-input
              v-model="supplierSearch"
              size="small"
              placeholder="输入供应商名称"
              class="supplier-search"
              @keyup.enter.native="handleSupplierSearch"
            />
            <el-button size="small" @click="handleSupplierSearch">搜索</el-button>
            <el-button size="small" @click="resetSupplierSearch">重置</el-button>
            <el-button size="small" type="primary" @click="openSupplierCreate">
              新增供应商
            </el-button>
          </div>
        </div>
      </template>
      <el-table
        :data="suppliers"
        :loading="supplierLoading.list"
        stripe
        empty-text="暂无供应商"
      >
        <el-table-column prop="name" label="名称" width="200" />
        <el-table-column label="联系电话" width="160">
          <template #default="{ row }">
            {{ row.phone || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="地址">
          <template #default="{ row }">
            {{ formatAddress(row.address) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180">
          <template #default="{ row }">
            <el-button size="small" link type="primary" @click.stop="openSupplierEdit(row)">
              编辑
            </el-button>
            <el-popconfirm title="确认删除该供应商？" @confirm="deleteSupplier(row.id)">
              <template #reference>
                <el-button size="small" link type="danger">删除</el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
      </section>

      <section ref="userSectionRef" id="section-user" class="section-card">
    <el-card shadow="never" class="user-card">
      <template #header>
        <div class="card-header">
          <span>系统用户</span>
          <div class="table-actions">
            <el-input
              v-model="userKeyword"
              size="small"
              clearable
              placeholder="搜索用户名/角色"
              class="table-search"
            />
            <el-tag size="small" type="info">共 {{ filteredUsers.length }} 人</el-tag>
            <el-button size="small" type="primary" @click="openUserCreate">
              新增用户
            </el-button>
          </div>
        </div>
      </template>
      <el-table
        :data="filteredUsers"
        :loading="userLoading.list"
        empty-text="暂无用户"
        stripe
      >
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="username" label="用户名" />
        <el-table-column prop="role" label="角色" width="180">
          <template #default="{ row }">
            <el-tag
              :type="row.role === 'ADMIN' ? 'danger' : row.role === 'SUPPLIER' ? 'warning' : 'success'"
              size="small"
            >
              {{
                row.role === 'ADMIN'
                  ? '管理员'
                  : row.role === 'SUPPLIER'
                    ? '供应商'
                    : '普通用户'
              }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="email" label="邮箱" />
        <el-table-column prop="phone" label="电话" width="150" />
      </el-table>
    </el-card>
      </section>

      <section ref="takeApprovalSectionRef" id="section-take" class="section-card">
        <el-card shadow="never">
          <template #header>
            <div class="card-header">
              <span>用户领用审核</span>
              <div class="table-actions">
                <el-input
                  v-model="takeApprovalKeyword"
                  size="small"
                  clearable
                  placeholder="搜索领用人/型号"
                  class="table-search"
                  @keyup.enter="handleTakeApprovalSearch"
                />
                <el-button size="small" @click="handleTakeApprovalSearch">搜索</el-button>
                <el-tag size="small" type="warning">共 {{ filteredTakeApprovals.length }} 条</el-tag>
              </div>
            </div>
          </template>
          <el-table
            :data="filteredTakeApprovals"
            :loading="loading.takeApproval"
            empty-text="暂无待审核领用"
            stripe
          >
            <el-table-column prop="takenBy" label="领用人" width="140" />
            <el-table-column label="元器件">
              <template #default="{ row }">
                {{ row.componentType?.name || '-' }}（{{ row.componentType?.model || '-' }}）
              </template>
            </el-table-column>
            <el-table-column prop="quantity" label="数量" width="100" />
            <el-table-column label="提交时间" width="200">
              <template #default="{ row }">
                {{ formatDate(row.takeDate) }}
              </template>
            </el-table-column>
            <el-table-column label="操作" width="220">
              <template #default="{ row }">
                <el-button
                  size="small"
                  type="success"
                  plain
                  @click="handleTakeApprovalDecision(row, 'approve')"
                >
                  通过
                </el-button>
                <el-button
                  size="small"
                  type="danger"
                  plain
                  @click="handleTakeApprovalDecision(row, 'reject')"
                >
                  拒绝
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </section>

      <section ref="auditSectionRef" id="section-audit" class="section-card">
        <el-card shadow="never" class="mb-16">
          <template #header>
            <div class="card-header">
              <span>供应商待审核申请</span>
              <div class="table-actions">
                <el-input
                  v-model="pendingKeyword"
                  size="small"
                  clearable
                  placeholder="搜索供应商/型号"
                  class="table-search"
                  @keyup.enter="handlePendingSearch"
                />
                <el-button size="small" @click="handlePendingSearch">搜索</el-button>
                <el-tag size="small" type="info">共 {{ pendingComponents.length }} 条</el-tag>
              </div>
            </div>
          </template>
          <el-table
            :data="pendingComponents"
            empty-text="暂无待审核记录"
            stripe
          >
            <el-table-column label="供应商" width="160">
              <template #default="{ row }">
                {{ getSupplierName(row) }}
              </template>
            </el-table-column>
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
            <el-table-column label="数量" width="120">
              <template #default="{ row }">
                {{ row.stockQuantity ?? 0 }}
              </template>
            </el-table-column>
            <el-table-column label="状态" width="120">
              <template #default="{ row }">
                <el-tag :type="getStatusTagType(row.status)">
                  {{ formatStatus(row.status) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="220">
              <template #default="{ row }">
                <el-button
                  size="small"
                  type="success"
                  plain
                  :loading="approvalState.action === 'approve' && approvalState.id === row.id"
                  @click="handleApprovalDecision(row, 'approve')"
                >
                  通过
                </el-button>
                <el-button
                  size="small"
                  type="danger"
                  plain
                  :loading="approvalState.action === 'reject' && approvalState.id === row.id"
                  @click="handleApprovalDecision(row, 'reject')"
                >
                  拒绝
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>

        <el-card shadow="never">
          <template #header>
            <div class="card-header">
              <span>审核失败记录</span>
              <div class="table-actions">
                <el-input
                  v-model="rejectedKeyword"
                  size="small"
                  clearable
                  placeholder="搜索供应商/型号"
                  class="table-search"
                  @keyup.enter="handleRejectedSearch"
                />
                <el-button size="small" @click="handleRejectedSearch">搜索</el-button>
                <el-tag size="small" type="danger">共 {{ rejectedComponents.length }} 条</el-tag>
              </div>
            </div>
          </template>
          <el-table
            :data="rejectedComponents"
            empty-text="暂无审核失败记录"
            stripe
          >
            <el-table-column label="供应商" width="160">
              <template #default="{ row }">
                {{ getSupplierName(row) }}
              </template>
            </el-table-column>
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
            <el-table-column label="数量" width="120">
              <template #default="{ row }">
                {{ row.stockQuantity ?? 0 }}
              </template>
            </el-table-column>
            <el-table-column label="状态" width="120">
              <template #default="{ row }">
                <el-tag :type="getStatusTagType(row.status)" effect="plain">
                  {{ formatStatus(row.status) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="160">
              <template #default="{ row }">
                <el-button
                  size="small"
                  type="primary"
                  plain
                  :loading="approvalState.action === 'approve' && approvalState.id === row.id"
                  @click="handleApprovalDecision(row, 'approve')"
                >
                  重新审核通过
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </section>

    <el-dialog
      v-model="dialog.visible"
      :title="dialog.isEdit ? '编辑类型' : '新增类型'"
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
            :precision="2"
            :step="0.1"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="初始库存">
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
            <p class="muted-text mt-6">支持 JPG/PNG，大小不超过 2MB</p>
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialog.visible = false">取消</el-button>
        <el-button type="primary" :loading="dialog.loading" @click="submitDialog">
          保存
        </el-button>
      </template>
    </el-dialog>

    <el-dialog
      v-model="supplierDialog.visible"
      :title="supplierDialog.isEdit ? '编辑供应商' : '新增供应商'"
      width="520px"
      :body-style="{ maxHeight: '70vh', overflowY: 'auto', overflowX: 'hidden', padding: '20px' }"
      append-to-body
      :close-on-click-modal="false"
      :lock-scroll="true"
      class="scrollable-dialog"
    >
      <el-form
        :model="supplierDialog.form"
        :rules="supplierDialogRules"
        ref="supplierDialogRef"
        label-width="100px"
      >
        <el-form-item label="供应商名称" prop="name">
          <el-input v-model="supplierDialog.form.name" placeholder="请输入供应商名称" />
        </el-form-item>
        <el-form-item label="联系电话">
          <el-input v-model="supplierDialog.form.phone" placeholder="请输入联系电话" />
        </el-form-item>
        <el-form-item label="省份">
          <el-select
            v-model="supplierDialog.form.province"
            placeholder="请选择省份"
            @change="handleProvinceChange"
          >
            <el-option
              v-for="item in regionOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="城市">
          <el-select
            v-model="supplierDialog.form.city"
            placeholder="请选择城市"
            :disabled="!supplierDialog.form.province"
          >
            <el-option
              v-for="city in cityOptions"
              :key="city"
              :label="city"
              :value="city"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="详细地址">
          <el-input
            v-model="supplierDialog.form.detail"
            type="textarea"
            placeholder="街道/园区/门牌号"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="supplierDialog.visible = false">取消</el-button>
        <el-button type="primary" :loading="supplierDialog.loading" @click="submitSupplierDialog">
          保存
        </el-button>
      </template>
    </el-dialog>

    <el-dialog
      v-model="userDialog.visible"
      :title="userDialog.isEdit ? '编辑用户' : '新增用户'"
      width="520px"
      :body-style="{ maxHeight: '70vh', overflowY: 'auto', overflowX: 'hidden', padding: '20px' }"
      append-to-body
      :close-on-click-modal="false"
      :lock-scroll="true"
      class="scrollable-dialog"
    >
      <el-form
        :model="userDialog.form"
        :rules="userDialogRules"
        ref="userDialogRef"
        label-width="100px"
      >
        <el-form-item label="用户名" prop="username">
          <el-input v-model="userDialog.form.username" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item label="密码" prop="password" :required="!userDialog.isEdit">
          <el-input
            v-model="userDialog.form.password"
            type="password"
            placeholder="请输入密码"
            show-password
          />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="userDialog.form.email" placeholder="请输入邮箱" />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="userDialog.form.phone" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item label="角色" prop="role">
          <el-select v-model="userDialog.form.role" placeholder="请选择角色" @change="handleUserRoleChange">
            <el-option label="管理员" value="ADMIN" />
            <el-option label="普通用户" value="USER" />
            <el-option label="供应商" value="SUPPLIER" />
          </el-select>
        </el-form-item>
        <el-form-item
          v-if="userDialog.form.role === 'SUPPLIER'"
          label="供应商"
          prop="supplierOption"
        >
          <el-radio-group v-model="userDialog.form.supplierOption" @change="handleSupplierOptionChange">
            <el-radio label="existing">关联已有供应商</el-radio>
            <el-radio label="new">创建新供应商</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item
          v-if="userDialog.form.role === 'SUPPLIER' && userDialog.form.supplierOption === 'existing'"
          label="选择供应商"
          prop="supplierId"
        >
          <el-select
            v-model="userDialog.form.supplierId"
            placeholder="请选择供应商"
            filterable
            clearable
            style="width: 100%"
          >
            <el-option
              v-for="supplier in suppliers"
              :key="supplier.id"
              :label="supplier.name"
              :value="supplier.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item
          v-if="userDialog.form.role === 'SUPPLIER' && userDialog.form.supplierOption === 'new'"
          label="供应商名称"
          prop="newSupplierName"
        >
          <el-input
            v-model="userDialog.form.newSupplierName"
            placeholder="请输入供应商名称"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="userDialog.visible = false">取消</el-button>
        <el-button type="primary" :loading="userDialog.loading" @click="submitUserDialog">
          保存
        </el-button>
      </template>
    </el-dialog>

    <el-dialog
      v-model="imagePreview.visible"
      title="图片预览"
      width="420px"
      append-to-body
      :body-style="{ maxHeight: '70vh', overflowY: 'auto' }"
    >
      <div class="image-preview-wrapper" v-if="imagePreview.url">
        <el-image :src="imagePreview.url" fit="contain" style="width: 100%" />
      </div>
      <div v-else class="muted-text">暂无图片</div>
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
import dayjs from 'dayjs'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Picture } from '@element-plus/icons-vue'
import {
  adminComponentTypeAPI,
  auditAPI,
  fileAPI,
  historyAPI,
  stockAPI,
  supplierAPI,
  takeApprovalAPI,
  userAPI,
} from '@/api'
import { chinaRegions } from '@/constants/regions'
import { useAuthStore } from '@/store/auth'

interface ComponentType {
  id: number
  name: string
  model: string
  unitPrice?: number
  stockQuantity?: number
  image?: string
  supplierId?: number
  status?: 'PENDING' | 'APPROVED' | 'REJECTED'
  supplier?: { id?: number; name?: string }
  specification?: { parameters?: string }
}

interface StockOutRecord {
  id: number
  quantity: number
  takenBy: string
  takeDate?: string
  componentType?: {
    name?: string
    model?: string
  }
}

interface SupplierAddress {
  province?: string
  city?: string
  detail?: string
}

interface Supplier {
  id: number
  name: string
  phone?: string
  address?: SupplierAddress
}

interface SystemUser {
  id: number
  username: string
  role: 'ADMIN' | 'USER' | 'SUPPLIER'
  phone?: string
  email?: string
  supplierId?: number
}

interface TakeApprovalRecord {
  id: number
  quantity: number
  takenBy: string
  takeDate?: string
  status: 'PENDING' | 'APPROVED' | 'REJECTED'
  componentType?: {
    id: number
    name?: string
    model?: string
  }
}

const regionOptions = chinaRegions
const apiBaseUrl = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api'
const MAX_IMAGE_SIZE = 2 * 1024 * 1024

const buildFileUrl = (fileName?: string) => (fileName ? `${apiBaseUrl}/files/${fileName}` : '')

const imagePreview = reactive({
  visible: false,
  url: '',
})

const authStore = useAuthStore()
const componentTypes = ref<ComponentType[]>([])
const history = ref<StockOutRecord[]>([])
const suppliers = ref<Supplier[]>([])
const users = ref<SystemUser[]>([])
const takeApprovals = ref<TakeApprovalRecord[]>([])
const componentKeyword = ref('')
const historyKeyword = ref('')
const userKeyword = ref('')
const pendingKeyword = ref('')
const rejectedKeyword = ref('')
const takeApprovalKeyword = ref('')
const componentSectionRef = ref<HTMLElement | null>(null)
const stockSectionRef = ref<HTMLElement | null>(null)
const supplierSectionRef = ref<HTMLElement | null>(null)
const userSectionRef = ref<HTMLElement | null>(null)
const takeApprovalSectionRef = ref<HTMLElement | null>(null)
const auditSectionRef = ref<HTMLElement | null>(null)

const navItems = [
  { key: 'section-component', label: '元器件管理' },
  { key: 'section-stock', label: '入库与使用' },
  { key: 'section-supplier', label: '供应商管理' },
  { key: 'section-user', label: '用户管理' },
  { key: 'section-take', label: '领用审核' },
  { key: 'section-audit', label: '操作审核' },
]

const sectionMap: Record<string, Ref<HTMLElement | null>> = {
  'section-component': componentSectionRef,
  'section-stock': stockSectionRef,
  'section-supplier': supplierSectionRef,
  'section-user': userSectionRef,
  'section-take': takeApprovalSectionRef,
  'section-audit': auditSectionRef,
}

const loading = reactive({
  types: false,
  history: false,
  stockIn: false,
  takeApproval: false,
})

const supplierLoading = reactive({
  list: false,
})

const userLoading = reactive({
  list: false,
})

const approvalState = reactive({
  id: 0,
  action: '' as '' | 'approve' | 'reject',
})

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

const dialogUploadLoading = ref(false)
const dialogRef = ref<FormInstance>()
const stockInRef = ref<FormInstance>()
const supplierDialogRef = ref<FormInstance>()
const userDialogRef = ref<FormInstance>()
const supplierSearch = ref('')

const dialogRules: FormRules = {
  name: [{ required: true, message: '请输入名称', trigger: 'blur' }],
  model: [{ required: true, message: '请输入型号', trigger: 'blur' }],
}

const stockInForm = reactive({
  componentTypeId: undefined as number | undefined,
  quantity: 1,
  purchaseDate: new Date(),
  purchaser: authStore.user?.username || '',
})

const stockInRules: FormRules = {
  componentTypeId: [{ required: true, message: '请选择元器件', trigger: 'change' }],
  quantity: [{ required: true, message: '请输入数量', trigger: 'change' }],
  purchaseDate: [{ required: true, message: '请选择时间', trigger: 'change' }],
  purchaser: [{ required: true, message: '请输入采购人', trigger: 'blur' }],
}

const supplierDialog = reactive({
  visible: false,
  isEdit: false,
  loading: false,
  form: {
    id: 0,
    name: '',
    phone: '',
    province: '',
    city: '',
    detail: '',
  },
})

const supplierDialogRules: FormRules = {
  name: [{ required: true, message: '请输入供应商名称', trigger: 'blur' }],
}

const userDialog = reactive({
  visible: false,
  isEdit: false,
  loading: false,
  form: {
    id: 0,
    username: '',
    password: '',
    email: '',
    phone: '',
    role: 'USER' as 'ADMIN' | 'USER' | 'SUPPLIER',
    supplierOption: 'existing' as 'existing' | 'new',
    supplierId: undefined as number | undefined,
    newSupplierName: '',
  },
})

const userDialogRules: FormRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [
    {
      validator: (rule: any, value: string, callback: any) => {
        if (!userDialog.isEdit && !value) {
          callback(new Error('请输入密码'))
        } else {
          callback()
        }
      },
      trigger: 'blur',
    },
  ],
  role: [{ required: true, message: '请选择角色', trigger: 'change' }],
  supplierOption: [
    {
      validator: (rule: any, value: string, callback: any) => {
        if (userDialog.form.role === 'SUPPLIER' && !value) {
          callback(new Error('请选择供应商选项'))
        } else {
          callback()
        }
      },
      trigger: 'change',
    },
  ],
  supplierId: [
    {
      validator: (rule: any, value: number | undefined, callback: any) => {
        if (
          userDialog.form.role === 'SUPPLIER' &&
          userDialog.form.supplierOption === 'existing' &&
          !value
        ) {
          callback(new Error('请选择供应商'))
        } else {
          callback()
        }
      },
      trigger: 'change',
    },
  ],
  newSupplierName: [
    {
      validator: (rule: any, value: string, callback: any) => {
        if (
          userDialog.form.role === 'SUPPLIER' &&
          userDialog.form.supplierOption === 'new' &&
          !value
        ) {
          callback(new Error('请输入供应商名称'))
        } else {
          callback()
        }
      },
      trigger: 'blur',
    },
  ],
}

const cityOptions = computed(() => {
  const province = regionOptions.find((item) => item.value === supplierDialog.form.province)
  return province ? province.cities : []
})

const handleNavClick = (key: string) => {
  const target = sectionMap[key]?.value
  if (target) {
    target.scrollIntoView({ behavior: 'smooth', block: 'start' })
  }
}

const filteredComponentTypes = computed(() =>
  componentTypes.value.filter((item) => {
    if (!componentKeyword.value) return true
    const keyword = componentKeyword.value.toLowerCase()
    return (
      item.name.toLowerCase().includes(keyword) ||
      item.model.toLowerCase().includes(keyword)
    )
  })
)

const filterComponentsByKeyword = (list: ComponentType[], keyword: string) => {
  if (!keyword.trim()) return list
  const normalized = keyword.trim().toLowerCase()
  return list.filter((item) => {
    const supplierName =
      item.supplier?.name?.toLowerCase() ||
      (item.supplierId ? supplierLookup.value.get(item.supplierId)?.toLowerCase() : '') ||
      ''
    return (
      item.name.toLowerCase().includes(normalized) ||
      item.model.toLowerCase().includes(normalized) ||
      supplierName.includes(normalized)
    )
  })
}

const pendingComponents = computed(() =>
  filterComponentsByKeyword(
    componentTypes.value.filter((item) => item.status === 'PENDING'),
    pendingKeyword.value
  )
)

const rejectedComponents = computed(() => {
  // 严格过滤：只显示status严格等于'REJECTED'的记录
  const rejected = componentTypes.value.filter((item) => {
    const status = item.status?.toUpperCase()
    return status === 'REJECTED'
  })
  return filterComponentsByKeyword(rejected, rejectedKeyword.value)
})

const filteredTakeApprovals = computed(() =>
  takeApprovals.value.filter((record) => {
    if (!takeApprovalKeyword.value) return true
    const keyword = takeApprovalKeyword.value.toLowerCase()
    const componentName = record.componentType?.name?.toLowerCase() || ''
    const model = record.componentType?.model?.toLowerCase() || ''
    return (
      componentName.includes(keyword) ||
      model.includes(keyword) ||
      record.takenBy.toLowerCase().includes(keyword)
    )
  })
)

const statusLabelMap: Record<NonNullable<ComponentType['status']>, string> = {
  PENDING: '待审核',
  APPROVED: '已通过',
  REJECTED: '已驳回',
}

const statusTagTypeMap: Record<NonNullable<ComponentType['status']>, 'info' | 'success' | 'danger'> = {
  PENDING: 'info',
  APPROVED: 'success',
  REJECTED: 'danger',
}

const formatStatus = (status?: ComponentType['status']) =>
  status ? statusLabelMap[status] || status : '未知'

const getStatusTagType = (status?: ComponentType['status']) =>
  status ? statusTagTypeMap[status] : 'info'

const handleComponentSearch = () => {
  componentKeyword.value = componentKeyword.value.trim()
}

const handleHistorySearch = () => {
  historyKeyword.value = historyKeyword.value.trim()
}

const handlePendingSearch = () => {
  pendingKeyword.value = pendingKeyword.value.trim()
}

const handleRejectedSearch = () => {
  rejectedKeyword.value = rejectedKeyword.value.trim()
}

const handleTakeApprovalSearch = () => {
  takeApprovalKeyword.value = takeApprovalKeyword.value.trim()
}

const supplierLookup = computed(() => {
  const map = new Map<number, string>()
  suppliers.value.forEach((item) => map.set(item.id, item.name))
  return map
})

const getSupplierName = (component: ComponentType) => {
  const direct = component.supplier?.name
  if (direct) return direct
  if (component.supplierId) {
    return supplierLookup.value.get(component.supplierId) || '-'
  }
  return '-'
}

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

const filteredUsers = computed(() =>
  users.value.filter((user) => {
    if (!userKeyword.value) return true
    const keyword = userKeyword.value.toLowerCase()
    return (
      user.username.toLowerCase().includes(keyword) ||
      user.role.toLowerCase().includes(keyword)
    )
  })
)

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

const fetchComponentTypes = async () => {
  loading.types = true
  try {
    const list = (await adminComponentTypeAPI.getAllTypes()) as unknown as
      | ComponentType[]
      | undefined
    componentTypes.value = list || []
    // 调试：检查是否有status不一致的记录
    const statusIssues = componentTypes.value.filter(
      (item) => item.model === 'RES-10K-1/25W' && item.status !== 'APPROVED'
    )
    if (statusIssues.length > 0) {
      console.warn('发现状态不一致的记录:', statusIssues)
    }
    if (!stockInForm.componentTypeId && componentTypes.value.length) {
      stockInForm.componentTypeId = componentTypes.value[0].id
    }
  } finally {
    loading.types = false
  }
}

const fetchHistory = async () => {
  loading.history = true
  try {
    const records = (await historyAPI.getUsageHistory(0, 20)) as unknown as
      | StockOutRecord[]
      | undefined
    // 按时间倒序排列，最新的在最上面
    history.value = (records || []).sort((a, b) => {
      const dateA = a.takeDate ? new Date(a.takeDate).getTime() : 0
      const dateB = b.takeDate ? new Date(b.takeDate).getTime() : 0
      return dateB - dateA
    })
  } finally {
    loading.history = false
  }
}

const fetchSuppliers = async () => {
  supplierLoading.list = true
  try {
    const list = (await supplierAPI.getSuppliers()) as unknown as
      | Supplier[]
      | undefined
    suppliers.value = list || []
  } finally {
    supplierLoading.list = false
  }
}

const fetchUsers = async () => {
  userLoading.list = true
  try {
    const list = (await userAPI.getUsers()) as unknown as SystemUser[] | undefined
    users.value = list || []
  } finally {
    userLoading.list = false
  }
}

const fetchTakeApprovals = async () => {
  loading.takeApproval = true
  try {
    const list = (await takeApprovalAPI.getPending()) as unknown as
      | TakeApprovalRecord[]
      | undefined
    takeApprovals.value = list || []
  } finally {
    loading.takeApproval = false
  }
}

const handleApprovalDecision = async (component: ComponentType, action: 'approve' | 'reject') => {
  try {
    await ElMessageBox.confirm(
      action === 'approve' ? `确认通过 ${component.name} ？` : `确认拒绝 ${component.name} ？`,
      '审核确认',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      }
    )
    approvalState.id = component.id
    approvalState.action = action
    if (action === 'approve') {
      await auditAPI.approve(component.id)
      ElMessage.success('已通过申请')
    } else {
      await auditAPI.reject(component.id)
      ElMessage.success('已拒绝申请')
    }
    fetchComponentTypes()
  } catch (error) {
    if (error === 'cancel' || error === 'close') return
  } finally {
    approvalState.id = 0
    approvalState.action = ''
  }
}

const handleTakeApprovalDecision = async (
  record: TakeApprovalRecord,
  action: 'approve' | 'reject'
) => {
  try {
    await ElMessageBox.confirm(
      action === 'approve' ? `确认通过 ${record.takenBy} 的领用申请？` : '确认拒绝该申请？',
      '领用审核',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      }
    )
    const approver = authStore.user?.username || 'ADMIN'
    if (action === 'approve') {
      await takeApprovalAPI.approve(record.id, approver)
      ElMessage.success('已通过领用申请')
    } else {
      await takeApprovalAPI.reject(record.id, approver)
      ElMessage.success('已拒绝领用申请')
    }
    fetchTakeApprovals()
    fetchHistory()
  } catch (error) {
    if (error === 'cancel' || error === 'close') return
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

const openEdit = (item: ComponentType) => {
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

const deleteType = async (id: number) => {
  await adminComponentTypeAPI.deleteType(id)
  ElMessage.success('删除成功')
  fetchComponentTypes()
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
        await adminComponentTypeAPI.updateType(dialog.form.id, payload)
      } else {
        await adminComponentTypeAPI.createType(payload)
      }
      ElMessage.success('保存成功')
      dialog.visible = false
      fetchComponentTypes()
    } finally {
      dialog.loading = false
    }
  })
}

const handleStockIn = async () => {
  if (!stockInRef.value) return
  await stockInRef.value.validate(async (valid) => {
    if (!valid) return
    loading.stockIn = true
    try {
      await stockAPI.stockIn({
        componentTypeId: stockInForm.componentTypeId,
        quantity: stockInForm.quantity,
        purchaseDate: dayjs(stockInForm.purchaseDate).toISOString(),
        purchaser: stockInForm.purchaser,
      })
      ElMessage.success('入库成功')
      fetchHistory()
    } finally {
      loading.stockIn = false
    }
  })
}

const formatDate = (value?: string) => {
  if (!value) return '-'
  return dayjs(value).format('YYYY-MM-DD HH:mm')
}

const openSupplierCreate = () => {
  supplierDialog.visible = true
  supplierDialog.isEdit = false
  supplierDialog.form = {
    id: 0,
    name: '',
    phone: '',
    province: '',
    city: '',
    detail: '',
  }
}

const openSupplierEdit = (supplier: Supplier) => {
  console.log('打开编辑供应商对话框:', supplier)
  try {
    supplierDialog.visible = true
    supplierDialog.isEdit = true
    supplierDialog.form = {
      id: supplier.id,
      name: supplier.name,
      phone: supplier.phone || '',
      province: supplier.address?.province || '',
      city: supplier.address?.city || '',
      detail: supplier.address?.detail || '',
    }
    console.log('供应商对话框状态:', supplierDialog)
  } catch (error) {
    console.error('打开编辑供应商对话框失败:', error)
    ElMessage.error('打开编辑对话框失败')
  }
}

const submitSupplierDialog = async () => {
  if (!supplierDialogRef.value) return
  await supplierDialogRef.value.validate(async (valid) => {
    if (!valid) return
    supplierDialog.loading = true
    const payload = {
      name: supplierDialog.form.name,
      phone: supplierDialog.form.phone || undefined,
      address: {
        province: supplierDialog.form.province || undefined,
        city: supplierDialog.form.city || undefined,
        detail: supplierDialog.form.detail || undefined,
      },
    }
    try {
      if (supplierDialog.isEdit) {
        await supplierAPI.updateSupplier(supplierDialog.form.id, payload)
      } else {
        await supplierAPI.createSupplier(payload)
      }
      ElMessage.success('保存成功')
      supplierDialog.visible = false
      fetchSuppliers()
    } catch (error: any) {
      console.error('保存供应商失败:', error)
      ElMessage.error(error?.response?.data?.message || '保存失败，请重试')
    } finally {
      supplierDialog.loading = false
    }
  })
}

const deleteSupplier = async (id: number) => {
  await supplierAPI.deleteSupplier(id)
  ElMessage.success('删除成功')
  fetchSuppliers()
}

const handleSupplierSearch = async () => {
  if (!supplierSearch.value) {
    fetchSuppliers()
    return
  }
  supplierLoading.list = true
  try {
    const list = (await supplierAPI.searchSuppliers(supplierSearch.value)) as unknown as
      | Supplier[]
      | undefined
    suppliers.value = list || []
  } finally {
    supplierLoading.list = false
  }
}

const resetSupplierSearch = () => {
  supplierSearch.value = ''
  fetchSuppliers()
}

const openUserCreate = () => {
  userDialog.visible = true
  userDialog.isEdit = false
  userDialog.form = {
    id: 0,
    username: '',
    password: '',
    email: '',
    phone: '',
    role: 'USER',
    supplierOption: 'existing',
    supplierId: undefined,
    newSupplierName: '',
  }
}

const handleUserRoleChange = () => {
  // 当角色不是供应商时，清空供应商相关字段
  if (userDialog.form.role !== 'SUPPLIER') {
    userDialog.form.supplierId = undefined
    userDialog.form.supplierOption = 'existing'
    userDialog.form.newSupplierName = ''
  }
}

const handleSupplierOptionChange = () => {
  // 切换供应商选项时，清空另一个选项的值
  if (userDialog.form.supplierOption === 'existing') {
    userDialog.form.newSupplierName = ''
  } else {
    userDialog.form.supplierId = undefined
  }
}

const submitUserDialog = async () => {
  if (!userDialogRef.value) return
  await userDialogRef.value.validate(async (valid) => {
    if (!valid) return
    // 创建用户时密码是必填的
    if (!userDialog.isEdit && !userDialog.form.password) {
      ElMessage.error('请输入密码')
      return
    }
    userDialog.loading = true
    try {
      let supplierId: number | undefined = undefined

      // 如果是供应商角色，需要处理供应商
      if (userDialog.form.role === 'SUPPLIER') {
        if (userDialog.form.supplierOption === 'new') {
          // 创建新供应商
          if (!userDialog.form.newSupplierName) {
            ElMessage.error('请输入供应商名称')
            userDialog.loading = false
            return
          }
          const newSupplier = await supplierAPI.createSupplier({
            name: userDialog.form.newSupplierName,
          })
          supplierId = (newSupplier as any).id
          ElMessage.success('供应商创建成功')
          // 刷新供应商列表
          await fetchSuppliers()
        } else {
          // 关联已有供应商
          if (!userDialog.form.supplierId) {
            ElMessage.error('请选择供应商')
            userDialog.loading = false
            return
          }
          supplierId = userDialog.form.supplierId
        }
      }

      const payload: any = {
        username: userDialog.form.username,
        email: userDialog.form.email || undefined,
        phone: userDialog.form.phone || undefined,
        role: userDialog.form.role,
      }
      // 只有创建用户或修改时提供了密码才发送密码字段
      if (userDialog.form.password) {
        payload.password = userDialog.form.password
      }
      // 如果是供应商角色，设置供应商ID
      if (userDialog.form.role === 'SUPPLIER' && supplierId) {
        payload.supplierId = supplierId
      }

      if (userDialog.isEdit) {
        await userAPI.updateUser(userDialog.form.id, payload)
        ElMessage.success('更新用户成功')
      } else {
        await userAPI.createUser(payload)
        ElMessage.success('创建用户成功')
      }
      userDialog.visible = false
      fetchUsers()
    } catch (error: any) {
      console.error('保存用户失败:', error)
      ElMessage.error(error?.response?.data?.message || '保存用户失败，请重试')
    } finally {
      userDialog.loading = false
    }
  })
}

const formatAddress = (address?: SupplierAddress) => {
  if (!address) return '-'
  const parts = [address.province, address.city, address.detail].filter(Boolean)
  return parts.length ? parts.join(' ') : '-'
}

const handleProvinceChange = () => {
  supplierDialog.form.city = ''
}

onMounted(() => {
  fetchComponentTypes()
  fetchHistory()
  fetchSuppliers()
  fetchUsers()
  fetchTakeApprovals()
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
  color: inherit;
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

.content {
  flex: 1;
  padding: 24px;
}

.section-card {
  margin-bottom: 24px;
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
  gap: 12px;
  font-size: 14px;
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

.w-full {
  width: 100%;
}

.mb-16 {
  margin-bottom: 16px;
}

.supplier-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  align-items: center;
}

.supplier-search {
  width: 200px;
}

.table-actions {
  display: flex;
  gap: 8px;
  align-items: center;
}

.table-search {
  width: 180px;
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

.upload-block {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.mt-6 {
  margin-top: 6px;
}

.image-thumb {
  margin-top: 8px;
}

.image-preview-wrapper {
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

