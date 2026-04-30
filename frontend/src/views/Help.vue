<template>
  <div class="help-container">
    <div class="header-actions">
      <el-button type="primary" @click="showPublishDialog = true">发布求助</el-button>
    </div>

    <el-card class="filter-panel" shadow="never">
      <div class="filter-grid">
        <el-input
          v-model="filters.keyword"
          placeholder="搜索求助关键词"
          clearable
          @keyup.enter="handleSearch"
        />
        <el-select v-model="filters.category" placeholder="全部类型" clearable>
          <el-option label="学习" value="学习"></el-option>
          <el-option label="生活" value="生活"></el-option>
          <el-option label="技术" value="技术"></el-option>
          <el-option label="其他" value="其他"></el-option>
        </el-select>
        <el-select v-model="filters.urgency" placeholder="全部紧急程度" clearable>
          <el-option label="高" value="high"></el-option>
          <el-option label="中" value="medium"></el-option>
          <el-option label="低" value="low"></el-option>
        </el-select>
        <el-select v-model="filters.sort" placeholder="排序方式">
          <el-option label="最新发布" value="latest"></el-option>
          <el-option label="紧急程度优先" value="urgencyDesc"></el-option>
          <el-option label="浏览量优先" value="viewsDesc"></el-option>
        </el-select>
        <div class="filter-actions">
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="resetFilters">重置</el-button>
        </div>
      </div>
    </el-card>

    <div class="helps-grid">
      <el-card
        v-for="help in helps"
        :key="help.id"
        class="help-card"
        @click="viewHelpDetail(help)"
      >
        <div class="help-info">
          <div class="help-header">
            <h3>{{ help.title }}</h3>
            <el-tag :type="getUrgencyType(help.urgency)" size="small">{{ getUrgencyText(help.urgency) }}</el-tag>
          </div>
          <p class="description">{{ help.description }}</p>
          <p class="location" v-if="help.location">地点：{{ help.location }}</p>
          <el-tag size="small">{{ help.category }}</el-tag>
          <p class="view-count">浏览 {{ help.viewCount }} 次</p>
          <el-button
            v-if="help.status === 'pending' && help.requesterId !== currentUser.id"
            type="primary"
            size="small"
            @click.stop="acceptHelp(help)"
          >
            接受帮助
          </el-button>
        </div>
      </el-card>
    </div>

    <el-dialog v-model="showPublishDialog" title="发布求助" width="600px">
      <el-form :model="publishForm" label-width="100px">
        <el-form-item label="求助标题" required>
          <el-input v-model="publishForm.title" placeholder="请输入求助标题"></el-input>
        </el-form-item>
        <el-form-item label="求助描述" required>
          <el-input
            v-model="publishForm.description"
            type="textarea"
            :rows="4"
            placeholder="请输入求助描述"
          ></el-input>
        </el-form-item>
        <el-form-item label="分类" required>
          <el-select v-model="publishForm.category" placeholder="请选择分类" style="width: 100%;">
            <el-option label="学习" value="学习"></el-option>
            <el-option label="生活" value="生活"></el-option>
            <el-option label="技术" value="技术"></el-option>
            <el-option label="其他" value="其他"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="地点">
          <el-input v-model="publishForm.location" placeholder="请输入地点（可选）"></el-input>
        </el-form-item>
        <el-form-item label="紧急程度" required>
          <el-select v-model="publishForm.urgency" placeholder="请选择紧急程度" style="width: 100%;">
            <el-option label="低" value="low"></el-option>
            <el-option label="中" value="medium"></el-option>
            <el-option label="高" value="high"></el-option>
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showPublishDialog = false">取消</el-button>
        <el-button type="primary" @click="handlePublish" :loading="publishing">发布</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="showDetailDialog" title="求助详情" width="700px">
      <div v-if="selectedHelp">
        <h2>{{ selectedHelp.title }}</h2>
        <p><strong>分类：</strong><el-tag>{{ selectedHelp.category }}</el-tag></p>
        <p><strong>紧急程度：</strong>
          <el-tag :type="getUrgencyType(selectedHelp.urgency)">{{ getUrgencyText(selectedHelp.urgency) }}</el-tag>
        </p>
        <p v-if="selectedHelp.location"><strong>地点：</strong>{{ selectedHelp.location }}</p>
        <p><strong>描述：</strong></p>
        <p>{{ selectedHelp.description }}</p>
        <p><strong>状态：</strong>
          <el-tag :type="getStatusType(selectedHelp.status)">{{ getStatusText(selectedHelp.status) }}</el-tag>
        </p>
        <p><strong>浏览次数：</strong>{{ selectedHelp.viewCount }}</p>
        <el-button
          v-if="selectedHelp.status === 'pending' && selectedHelp.requesterId !== currentUser.id"
          type="primary"
          @click="acceptHelp(selectedHelp)"
        >
          接受帮助
        </el-button>
        <el-button
          v-if="selectedHelp.status === 'helping' && selectedHelp.helperId === currentUser.id"
          type="success"
          @click="completeHelp(selectedHelp.id)"
        >
          完成帮助
        </el-button>
        <el-button @click="contactRequester">联系求助者</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import axios from '../utils/axios'
import { ElMessage, ElMessageBox } from 'element-plus'

export default {
  name: 'Help',
  setup() {
    const router = useRouter()
    const currentUser = JSON.parse(localStorage.getItem('user') || '{}')
    const helps = ref([])
    const showPublishDialog = ref(false)
    const showDetailDialog = ref(false)
    const selectedHelp = ref(null)
    const publishing = ref(false)
    const filters = ref({
      keyword: '',
      category: '',
      urgency: '',
      sort: 'latest'
    })

    const publishForm = ref({
      title: '',
      description: '',
      category: '',
      location: '',
      urgency: 'medium'
    })

    const buildHelpQueryParams = () => {
      const params = {}
      if (currentUser.id) {
        params.userId = currentUser.id
      }
      if (filters.value.keyword) {
        params.keyword = filters.value.keyword.trim()
      }
      if (filters.value.category) {
        params.category = filters.value.category
      }
      if (filters.value.urgency) {
        params.urgency = filters.value.urgency
      }
      if (filters.value.sort) {
        params.sort = filters.value.sort
      }
      return params
    }

    const loadHelps = async () => {
      try {
        const res = await axios.get('/help/list', { params: buildHelpQueryParams() })
        if (res.code === 0) {
          helps.value = res.data
        }
      } catch (error) {
        console.error('加载求助信息失败：', error)
      }
    }

    const handleSearch = () => {
      loadHelps()
    }

    const resetFilters = () => {
      filters.value = {
        keyword: '',
        category: '',
        urgency: '',
        sort: 'latest'
      }
      loadHelps()
    }

    const handlePublish = async () => {
      if (!publishForm.value.title || !publishForm.value.description || !publishForm.value.category) {
        ElMessage.warning('请填写完整信息')
        return
      }
      publishing.value = true
      try {
        const res = await axios.post('/help/publish', {
          requesterId: currentUser.id,
          ...publishForm.value
        })
        if (res.code === 0) {
          ElMessage.success('发布成功')
          showPublishDialog.value = false
          const preferenceKeyword = publishForm.value.category
          publishForm.value = {
            title: '',
            description: '',
            category: '',
            location: '',
            urgency: 'medium'
          }
          loadHelps()
          await axios.post('/recommend/preference', {
            userId: currentUser.id,
            category: 'help',
            keyword: preferenceKeyword
          })
        } else {
          ElMessage.error(res.msg || '发布失败')
        }
      } catch (error) {
        ElMessage.error('发布失败：' + (error.response?.data?.msg || error.message))
      } finally {
        publishing.value = false
      }
    }

    const viewHelpDetail = async (help) => {
      try {
        const res = await axios.get(`/help/${help.id}`, {
          params: {
            userId: currentUser.id
          }
        })
        if (res.code === 0) {
          selectedHelp.value = res.data
          showDetailDialog.value = true
          await axios.post('/recommend/preference', {
            userId: currentUser.id,
            category: 'help',
            keyword: res.data.category
          })
        }
      } catch (error) {
        ElMessage.error('加载求助详情失败')
      }
    }

    const acceptHelp = async (help) => {
      try {
        await ElMessageBox.confirm('确定要接受这条求助吗？', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        })
        const res = await axios.post('/help/accept', {
          requestId: help.id,
          helperId: currentUser.id
        })
        if (res.code === 0) {
          ElMessage.success('接受成功')
          loadHelps()
          if (showDetailDialog.value) {
            selectedHelp.value = res.data
          }
        } else {
          ElMessage.error(res.msg || '接受失败')
        }
      } catch (error) {
        if (error !== 'cancel') {
          ElMessage.error('接受失败：' + (error.response?.data?.msg || error.message))
        }
      }
    }

    const completeHelp = async (helpId) => {
      try {
        await ElMessageBox.confirm('确定要标记为已完成吗？', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        })
        const res = await axios.post(`/help/complete/${helpId}`)
        if (res.code === 0) {
          ElMessage.success('标记成功')
          loadHelps()
          if (showDetailDialog.value) {
            selectedHelp.value = res.data
          }
        } else {
          ElMessage.error(res.msg || '操作失败')
        }
      } catch (error) {
        if (error !== 'cancel') {
          ElMessage.error('操作失败：' + (error.response?.data?.msg || error.message))
        }
      }
    }

    const contactRequester = () => {
      if (selectedHelp.value) {
        router.push(`/chat?userId=${selectedHelp.value.requesterId}`)
      }
    }

    const getUrgencyType = (urgency) => {
      const map = { high: 'danger', medium: 'warning', low: 'info' }
      return map[urgency] || 'info'
    }

    const getUrgencyText = (urgency) => {
      const map = { high: '高', medium: '中', low: '低' }
      return map[urgency] || urgency
    }

    const getStatusType = (status) => {
      const map = { pending: 'info', helping: 'warning', completed: 'success' }
      return map[status] || 'info'
    }

    const getStatusText = (status) => {
      const map = { pending: '待帮助', helping: '帮助中', completed: '已完成' }
      return map[status] || status
    }

    onMounted(() => {
      loadHelps()
    })

    return {
      currentUser,
      helps,
      filters,
      showPublishDialog,
      showDetailDialog,
      selectedHelp,
      publishing,
      publishForm,
      buildHelpQueryParams,
      handleSearch,
      resetFilters,
      handlePublish,
      viewHelpDetail,
      acceptHelp,
      completeHelp,
      contactRequester,
      getUrgencyType,
      getUrgencyText,
      getStatusType,
      getStatusText
    }
  }
}
</script>

<style scoped>
.help-container {
  padding: 20px;
}

.header-actions {
  display: flex;
  align-items: center;
  margin-bottom: 20px;
  justify-content: space-between;
}

.filter-panel {
  margin-bottom: 20px;
}

.filter-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
  gap: 12px;
}

.filter-actions {
  display: flex;
  gap: 12px;
  align-items: center;
}

.helps-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(350px, 1fr));
  gap: 20px;
  margin-top: 20px;
}

.help-card {
  cursor: pointer;
  transition: transform 0.2s;
}

.help-card:hover {
  transform: translateY(-5px);
}

.help-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.help-info h3 {
  color: #333;
  margin: 0;
}

.description {
  color: #666;
  margin: 10px 0;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.location {
  color: #909399;
  margin: 10px 0;
}

.view-count {
  color: #999;
  font-size: 12px;
  margin-top: 10px;
}
</style>
