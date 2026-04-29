<template>
  <div class="admin-page">
    <div class="page-header">
      <div>
        <h1>Admin Dashboard</h1>
        <p>Manage users and moderate campus content.</p>
      </div>
      <el-button type="primary" @click="loadAllData" :loading="loading">Refresh</el-button>
    </div>

    <el-alert
      v-if="errorMessage"
      :title="errorMessage"
      type="error"
      show-icon
      :closable="false"
      class="page-alert"
    />

    <el-row :gutter="16" class="stats-grid">
      <el-col :xs="24" :sm="12" :lg="6" v-for="item in statCards" :key="item.key">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-label">{{ item.label }}</div>
          <div class="stat-value">{{ stats[item.key] ?? 0 }}</div>
        </el-card>
      </el-col>
    </el-row>

    <el-tabs v-model="activeTab" class="admin-tabs">
      <el-tab-pane label="Users" name="users">
        <el-table :data="users" v-loading="loading" stripe>
          <el-table-column prop="id" label="ID" width="80" />
          <el-table-column prop="username" label="Username" min-width="140" />
          <el-table-column prop="realName" label="Real Name" min-width="140" />
          <el-table-column prop="college" label="College" min-width="160" />
          <el-table-column prop="role" label="Role" width="100" />
          <el-table-column label="Status" width="120">
            <template #default="{ row }">
              <el-tag :type="isUserActive(row) ? 'success' : 'danger'">
                {{ isUserActive(row) ? 'Active' : 'Disabled' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="Actions" width="160" fixed="right">
            <template #default="{ row }">
              <el-button
                v-if="isUserActive(row)"
                size="small"
                type="danger"
                @click="toggleUser(row, false)"
              >
                Disable
              </el-button>
              <el-button
                v-else
                size="small"
                type="success"
                @click="toggleUser(row, true)"
              >
                Enable
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <el-tab-pane label="Products" name="products">
        <el-table :data="products" v-loading="loading" stripe>
          <el-table-column prop="id" label="ID" width="80" />
          <el-table-column label="Image" width="110">
            <template #default="{ row }">
              <img
                v-if="row.images"
                :src="resolveImageUrl(row.images)"
                alt="product thumbnail"
                class="product-thumb"
              />
              <span v-else>-</span>
            </template>
          </el-table-column>
          <el-table-column prop="title" label="Title" min-width="180" />
          <el-table-column prop="category" label="Category" width="120" />
          <el-table-column prop="status" label="Status" width="120" />
          <el-table-column prop="price" label="Price" width="120" />
          <el-table-column label="Actions" width="120" fixed="right">
            <template #default="{ row }">
              <el-button size="small" type="danger" @click="removeItem('products', row.id)">
                Delete
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <el-tab-pane label="Jobs" name="jobs">
        <el-table :data="jobs" v-loading="loading" stripe>
          <el-table-column prop="id" label="ID" width="80" />
          <el-table-column prop="title" label="Title" min-width="180" />
          <el-table-column prop="company" label="Company" min-width="160" />
          <el-table-column prop="jobType" label="Type" width="120" />
          <el-table-column prop="status" label="Status" width="120" />
          <el-table-column label="Actions" width="120" fixed="right">
            <template #default="{ row }">
              <el-button size="small" type="danger" @click="removeItem('jobs', row.id)">
                Delete
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <el-tab-pane label="Helps" name="helps">
        <el-table :data="helps" v-loading="loading" stripe>
          <el-table-column prop="id" label="ID" width="80" />
          <el-table-column prop="title" label="Title" min-width="180" />
          <el-table-column prop="category" label="Category" width="120" />
          <el-table-column prop="status" label="Status" width="120" />
          <el-table-column prop="location" label="Location" min-width="140" />
          <el-table-column label="Actions" width="120" fixed="right">
            <template #default="{ row }">
              <el-button size="small" type="danger" @click="removeItem('helps', row.id)">
                Delete
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '../utils/axios'

const loading = ref(false)
const errorMessage = ref('')
const activeTab = ref('users')
const stats = ref({
  userCount: 0,
  productCount: 0,
  jobCount: 0,
  helpCount: 0,
  messageCount: 0
})
const users = ref([])
const products = ref([])
const jobs = ref([])
const helps = ref([])

const statCards = computed(() => [
  { key: 'userCount', label: 'Users' },
  { key: 'productCount', label: 'Products' },
  { key: 'jobCount', label: 'Jobs' },
  { key: 'helpCount', label: 'Helps' },
  { key: 'messageCount', label: 'Messages' }
])

function isUserActive(user) {
  return String(user?.status || '').toUpperCase() !== 'DISABLED'
}

function resolveImageUrl(url) {
  if (!url) {
    return ''
  }
  return url.startsWith('http') ? url : url
}

async function loadAllData() {
  loading.value = true
  errorMessage.value = ''

  try {
    const [statsResponse, userResponse, productResponse, jobResponse, helpResponse] = await Promise.all([
      request.get('/admin/stats'),
      request.get('/admin/users'),
      request.get('/admin/products'),
      request.get('/admin/jobs'),
      request.get('/admin/helps')
    ])

    stats.value = statsResponse.data || stats.value
    users.value = userResponse.data || []
    products.value = productResponse.data || []
    jobs.value = jobResponse.data || []
    helps.value = helpResponse.data || []
  } catch (error) {
    errorMessage.value = error?.response?.data?.msg || error?.message || 'Failed to load admin data'
  } finally {
    loading.value = false
  }
}

async function toggleUser(user, enable) {
  try {
    await request.post(`/admin/users/${user.id}/${enable ? 'enable' : 'disable'}`)
    ElMessage.success(`User ${enable ? 'enabled' : 'disabled'}`)
    await loadAllData()
  } catch (error) {
    ElMessage.error(error?.response?.data?.msg || 'Operation failed')
  }
}

async function removeItem(resource, id) {
  try {
    await ElMessageBox.confirm('Delete this record?', 'Confirm', {
      type: 'warning'
    })
    await request.delete(`/admin/${resource}/${id}`)
    ElMessage.success('Deleted')
    await loadAllData()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error?.response?.data?.msg || 'Delete failed')
    }
  }
}

onMounted(() => {
  loadAllData()
})
</script>

<style scoped>
.admin-page {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 16px;
}

.page-header h1 {
  margin-bottom: 6px;
  color: #1f2937;
}

.page-header p {
  color: #6b7280;
}

.page-alert {
  margin-bottom: -4px;
}

.stats-grid {
  margin: 0;
}

.stat-card {
  border-radius: 14px;
}

.stat-label {
  font-size: 14px;
  color: #6b7280;
}

.stat-value {
  margin-top: 10px;
  font-size: 28px;
  font-weight: 700;
  color: #111827;
}

.admin-tabs :deep(.el-tabs__content) {
  background: #fff;
  border-radius: 16px;
  padding: 16px;
}

.product-thumb {
  width: 56px;
  height: 56px;
  object-fit: cover;
  border-radius: 8px;
  border: 1px solid #e5e7eb;
}
</style>
