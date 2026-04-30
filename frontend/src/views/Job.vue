<template>
  <div class="job-container">
    <div class="header-actions">
      <el-button type="primary" @click="showPublishDialog = true">发布职位</el-button>
    </div>

    <el-card class="filter-panel" shadow="never">
      <div class="filter-grid">
        <el-input
          v-model="filters.keyword"
          placeholder="搜索职位关键词"
          clearable
          @keyup.enter="handleSearch"
        />
        <el-input v-model="filters.location" placeholder="地点筛选" clearable @keyup.enter="handleSearch" />
        <el-select v-model="filters.jobType" placeholder="全部岗位类型" clearable>
          <el-option label="全职" value="全职"></el-option>
          <el-option label="兼职" value="兼职"></el-option>
          <el-option label="实习" value="实习"></el-option>
        </el-select>
        <el-input-number v-model="filters.minSalary" :min="0" :precision="0" :controls="false" placeholder="最低薪资" />
        <el-input-number v-model="filters.maxSalary" :min="0" :precision="0" :controls="false" placeholder="最高薪资" />
        <el-select v-model="filters.sort" placeholder="排序方式">
          <el-option label="最新发布" value="latest"></el-option>
          <el-option label="薪资从低到高" value="salaryAsc"></el-option>
          <el-option label="薪资从高到低" value="salaryDesc"></el-option>
          <el-option label="浏览量优先" value="viewsDesc"></el-option>
        </el-select>
        <div class="filter-actions">
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="resetFilters">重置</el-button>
        </div>
      </div>
    </el-card>

    <div class="jobs-grid">
      <el-card
        v-for="job in jobs"
        :key="job.id"
        class="job-card"
        @click="viewJobDetail(job)"
      >
        <div class="job-info">
          <h3>{{ job.title }}</h3>
          <p class="company">{{ job.company }}</p>
          <p class="location">? {{ job.location }}</p>
          <p class="salary">? {{ job.salary ? '?' + job.salary : '面议' }}</p>
          <el-tag size="small" type="success">{{ job.jobType }}</el-tag>
          <p class="description">{{ job.description }}</p>
          <p class="view-count">浏览 {{ job.viewCount }} 次</p>
        </div>
      </el-card>
    </div>

    <!-- 发布职位对话框 -->
    <el-dialog v-model="showPublishDialog" title="发布职位" width="600px">
      <el-form :model="publishForm" label-width="100px">
        <el-form-item label="职位标题" required>
          <el-input v-model="publishForm.title" placeholder="请输入职位标题"></el-input>
        </el-form-item>
        <el-form-item label="公司名称" required>
          <el-input v-model="publishForm.company" placeholder="请输入公司名称"></el-input>
        </el-form-item>
        <el-form-item label="工作地点" required>
          <el-input v-model="publishForm.location" placeholder="请输入工作地点"></el-input>
        </el-form-item>
        <el-form-item label="工作类型" required>
          <el-select v-model="publishForm.jobType" placeholder="请选择工作类型" style="width: 100%;">
            <el-option label="全职" value="全职"></el-option>
            <el-option label="兼职" value="兼职"></el-option>
            <el-option label="实习" value="实习"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="薪资">
          <el-input-number v-model="publishForm.salary" :min="0" :precision="0" style="width: 100%;"></el-input-number>
        </el-form-item>
        <el-form-item label="职位描述" required>
          <el-input
            v-model="publishForm.description"
            type="textarea"
            :rows="4"
            placeholder="请输入职位描述"
          ></el-input>
        </el-form-item>
        <el-form-item label="任职要求">
          <el-input
            v-model="publishForm.requirements"
            type="textarea"
            :rows="3"
            placeholder="请输入任职要求"
          ></el-input>
        </el-form-item>
        <el-form-item label="联系方式">
          <el-input v-model="publishForm.contact" placeholder="请输入联系方式"></el-input>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showPublishDialog = false">取消</el-button>
        <el-button type="primary" @click="handlePublish" :loading="publishing">发布</el-button>
      </template>
    </el-dialog>

    <!-- 职位详情对话框 -->
    <el-dialog v-model="showDetailDialog" title="职位详情" width="700px">
      <div v-if="selectedJob">
        <h2>{{ selectedJob.title }}</h2>
        <p class="detail-company">{{ selectedJob.company }}</p>
        <p><strong>工作地点：</strong>{{ selectedJob.location }}</p>
        <p><strong>工作类型：</strong><el-tag type="success">{{ selectedJob.jobType }}</el-tag></p>
        <p><strong>薪资：</strong>{{ selectedJob.salary ? '?' + selectedJob.salary : '面议' }}</p>
        <p><strong>职位描述：</strong></p>
        <p>{{ selectedJob.description }}</p>
        <p v-if="selectedJob.requirements"><strong>任职要求：</strong></p>
        <p v-if="selectedJob.requirements">{{ selectedJob.requirements }}</p>
        <p v-if="selectedJob.contact"><strong>联系方式：</strong>{{ selectedJob.contact }}</p>
        <p><strong>浏览次数：</strong>{{ selectedJob.viewCount }}</p>
        <el-button type="primary" @click="contactPublisher">联系发布者</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import axios from '../utils/axios'
import { ElMessage } from 'element-plus'

export default {
  name: 'Job',
  setup() {
    const router = useRouter()
    const currentUser = JSON.parse(localStorage.getItem('user') || '{}')
    const jobs = ref([])
    const showPublishDialog = ref(false)
    const showDetailDialog = ref(false)
    const selectedJob = ref(null)
    const publishing = ref(false)
    const filters = ref({
      keyword: '',
      location: '',
      jobType: '',
      minSalary: null,
      maxSalary: null,
      sort: 'latest'
    })

    const publishForm = ref({
      title: '',
      company: '',
      location: '',
      jobType: '',
      salary: null,
      description: '',
      requirements: '',
      contact: ''
    })

    const buildJobQueryParams = () => {
      const params = {}
      if (filters.value.keyword) {
        params.keyword = filters.value.keyword.trim()
      }
      if (filters.value.location) {
        params.location = filters.value.location.trim()
      }
      if (filters.value.jobType) {
        params.jobType = filters.value.jobType
      }
      if (filters.value.minSalary !== null && filters.value.minSalary !== undefined) {
        params.minSalary = filters.value.minSalary
      }
      if (filters.value.maxSalary !== null && filters.value.maxSalary !== undefined) {
        params.maxSalary = filters.value.maxSalary
      }
      if (filters.value.sort) {
        params.sort = filters.value.sort
      }
      return params
    }

    const loadJobs = async () => {
      try {
        const res = await axios.get('/job/list', { params: buildJobQueryParams() })
        if (res.code === 0) {
          jobs.value = res.data
        }
      } catch (error) {
        console.error('加载职位失败:', error)
      }
    }

    const handleSearch = () => {
      loadJobs()
    }

    const resetFilters = () => {
      filters.value = {
        keyword: '',
        location: '',
        jobType: '',
        minSalary: null,
        maxSalary: null,
        sort: 'latest'
      }
      loadJobs()
    }

    const handlePublish = async () => {
      if (!publishForm.value.title || !publishForm.value.company || !publishForm.value.location || !publishForm.value.jobType || !publishForm.value.description) {
        ElMessage.warning('请填写完整信息')
        return
      }
      publishing.value = true
      try {
        const res = await axios.post('/job/publish', {
          publisherId: currentUser.id,
          ...publishForm.value
        })
        if (res.code === 0) {
          ElMessage.success('发布成功')
          showPublishDialog.value = false
          const preferenceKeyword = publishForm.value.jobType
          publishForm.value = {
            title: '',
            company: '',
            location: '',
            jobType: '',
            salary: null,
            description: '',
            requirements: '',
            contact: ''
          }
          loadJobs()
          // 记录偏好
          await axios.post('/recommend/preference', {
            userId: currentUser.id,
            category: 'job',
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

    const viewJobDetail = async (job) => {
      try {
        const res = await axios.get(`/job/${job.id}`)
        if (res.code === 0) {
          selectedJob.value = res.data
          showDetailDialog.value = true
          // 记录偏好
          await axios.post('/recommend/preference', {
            userId: currentUser.id,
            category: 'job',
            keyword: res.data.jobType
          })
        }
      } catch (error) {
        ElMessage.error('加载详情失败')
      }
    }

    const contactPublisher = () => {
      if (selectedJob.value) {
        router.push(`/chat?userId=${selectedJob.value.publisherId}`)
      }
    }

    onMounted(() => {
      loadJobs()
    })

    return {
      jobs,
      filters,
      showPublishDialog,
      showDetailDialog,
      selectedJob,
      publishing,
      publishForm,
      buildJobQueryParams,
      handleSearch,
      resetFilters,
      handlePublish,
      viewJobDetail,
      contactPublisher
    }
  }
}
</script>

<style scoped>
.job-container {
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

.jobs-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(350px, 1fr));
  gap: 20px;
  margin-top: 20px;
}

.job-card {
  cursor: pointer;
  transition: transform 0.2s;
}

.job-card:hover {
  transform: translateY(-5px);
}

.job-info h3 {
  margin-bottom: 10px;
  color: #333;
}

.company {
  color: #409EFF;
  font-size: 16px;
  font-weight: bold;
  margin: 10px 0;
}

.location {
  color: #909399;
  margin: 10px 0;
}

.salary {
  color: #f56c6c;
  font-size: 18px;
  font-weight: bold;
  margin: 10px 0;
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

.view-count {
  color: #999;
  font-size: 12px;
  margin-top: 10px;
}

.detail-company {
  color: #409EFF;
  font-size: 18px;
  font-weight: bold;
  margin: 15px 0;
}
</style>
