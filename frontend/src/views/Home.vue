<template>
  <div class="home-container">
    <h2>智能推荐</h2>
    <el-tabs v-model="activeTab" @tab-change="handleTabChange">
      <el-tab-pane label="推荐商品" name="products">
        <div class="content-grid">
          <el-card v-for="product in recommendedProducts" :key="product.id" class="content-card" @click="viewProduct(product)">
            <div class="card-content">
              <h3>{{ product.title }}</h3>
              <p class="price">?{{ product.price }}</p>
              <p class="description">{{ product.description }}</p>
              <el-tag size="small">{{ product.category }}</el-tag>
            </div>
          </el-card>
        </div>
      </el-tab-pane>
      <el-tab-pane label="推荐职位" name="jobs">
        <div class="content-grid">
          <el-card v-for="job in recommendedJobs" :key="job.id" class="content-card" @click="viewJob(job)">
            <div class="card-content">
              <h3>{{ job.title }}</h3>
              <p class="company">{{ job.company }}</p>
              <p class="location">{{ job.location }}</p>
              <el-tag size="small" type="success">{{ job.jobType }}</el-tag>
            </div>
          </el-card>
        </div>
      </el-tab-pane>
      <el-tab-pane label="推荐求助" name="helps">
        <div class="content-grid">
          <el-card v-for="help in recommendedHelps" :key="help.id" class="content-card" @click="viewHelp(help)">
            <div class="card-content">
              <h3>{{ help.title }}</h3>
              <p class="description">{{ help.description }}</p>
              <el-tag size="small" :type="getUrgencyType(help.urgency)">{{ help.urgency }}</el-tag>
            </div>
          </el-card>
        </div>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import axios from '../utils/axios'
import { ElMessage } from 'element-plus'

export default {
  name: 'Home',
  setup() {
    const router = useRouter()
    const activeTab = ref('products')
    const recommendedProducts = ref([])
    const recommendedJobs = ref([])
    const recommendedHelps = ref([])
    const currentUser = JSON.parse(localStorage.getItem('user') || '{}')

    const loadRecommendations = async () => {
      if (!currentUser.id) return
      try {
        const [productsRes, jobsRes, helpsRes] = await Promise.all([
          axios.get(`/recommend/products/${currentUser.id}`),
          axios.get(`/recommend/jobs/${currentUser.id}`),
          axios.get(`/recommend/helps/${currentUser.id}`)
        ])
        if (productsRes.code === 0) recommendedProducts.value = productsRes.data
        if (jobsRes.code === 0) recommendedJobs.value = jobsRes.data
        if (helpsRes.code === 0) recommendedHelps.value = helpsRes.data
      } catch (error) {
        console.error('加载推荐失败:', error)
      }
    }

    const handleTabChange = (tab) => {
      if (tab === 'products' && recommendedProducts.value.length === 0) {
        loadRecommendations()
      } else if (tab === 'jobs' && recommendedJobs.value.length === 0) {
        loadRecommendations()
      } else if (tab === 'helps' && recommendedHelps.value.length === 0) {
        loadRecommendations()
      }
    }

    const viewProduct = (product) => {
      router.push(`/product?id=${product.id}`)
    }

    const viewJob = (job) => {
      router.push(`/job?id=${job.id}`)
    }

    const viewHelp = (help) => {
      router.push(`/help?id=${help.id}`)
    }

    const getUrgencyType = (urgency) => {
      const map = { high: 'danger', medium: 'warning', low: 'info' }
      return map[urgency] || 'info'
    }

    onMounted(() => {
      loadRecommendations()
    })

    return {
      activeTab,
      recommendedProducts,
      recommendedJobs,
      recommendedHelps,
      handleTabChange,
      viewProduct,
      viewJob,
      viewHelp,
      getUrgencyType
    }
  }
}
</script>

<style scoped>
.home-container {
  padding: 20px;
}

.content-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 20px;
  margin-top: 20px;
}

.content-card {
  cursor: pointer;
  transition: transform 0.2s;
}

.content-card:hover {
  transform: translateY(-5px);
}

.card-content h3 {
  margin-bottom: 10px;
  color: #333;
}

.price {
  color: #f56c6c;
  font-size: 18px;
  font-weight: bold;
  margin: 10px 0;
}

.company {
  color: #409EFF;
  margin: 10px 0;
}

.location {
  color: #909399;
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
</style>

