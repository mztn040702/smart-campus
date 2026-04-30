<template>
  <div class="home-container">
    <div class="quick-nav">
      <el-card
        v-for="link in quickLinks"
        :key="link.path"
        class="quick-nav-card"
        shadow="hover"
        @click="goTo(link.path)"
      >
        <div class="quick-nav-content">
          <div>
            <h3>{{ link.title }}</h3>
            <p>{{ link.description }}</p>
          </div>
          <el-button :type="link.type" plain>进入</el-button>
        </div>
      </el-card>
    </div>

    <div class="recommendation-header">
      <h2>智能推荐</h2>
      <p class="recommendation-tip">当前推荐基于用户行为、内容热度和语义相似度综合生成。</p>
    </div>
    <el-tabs v-model="activeTab" @tab-change="handleTabChange">
      <el-tab-pane label="推荐商品" name="products">
        <div class="content-grid">
          <el-card v-for="product in recommendedProducts" :key="product.id" class="content-card" @click="viewProduct(product)">
            <div class="card-content">
              <h3>{{ product.title }}</h3>
              <p class="price">￥{{ product.price }}</p>
              <p class="description">{{ product.description }}</p>
              <el-tag size="small">{{ product.category }}</el-tag>
              <p v-if="showRecommendationScores(product)" class="score-meta">推荐分数：{{ formatScore(product.finalScore) }}</p>
              <p v-if="showRecommendationScores(product)" class="score-meta">语义相似度：{{ formatScore(product.semanticScore) }}</p>
            </div>
          </el-card>
        </div>
      </el-tab-pane>
      <el-tab-pane label="推荐招聘" name="jobs">
        <div class="content-grid">
          <el-card v-for="job in recommendedJobs" :key="job.id" class="content-card" @click="viewJob(job)">
            <div class="card-content">
              <h3>{{ job.title }}</h3>
              <p class="company">{{ job.company }}</p>
              <p class="location">{{ job.location }}</p>
              <el-tag size="small" type="success">{{ job.jobType }}</el-tag>
              <p v-if="showRecommendationScores(job)" class="score-meta">推荐分数：{{ formatScore(job.finalScore) }}</p>
              <p v-if="showRecommendationScores(job)" class="score-meta">语义相似度：{{ formatScore(job.semanticScore) }}</p>
            </div>
          </el-card>
        </div>
      </el-tab-pane>
      <el-tab-pane label="推荐互助" name="helps">
        <div class="content-grid">
          <el-card v-for="help in recommendedHelps" :key="help.id" class="content-card" @click="viewHelp(help)">
            <div class="card-content">
              <h3>{{ help.title }}</h3>
              <p class="description">{{ help.description }}</p>
              <el-tag size="small" :type="getUrgencyType(help.urgency)">{{ help.urgency }}</el-tag>
              <p v-if="showRecommendationScores(help)" class="score-meta">推荐分数：{{ formatScore(help.finalScore) }}</p>
              <p v-if="showRecommendationScores(help)" class="score-meta">语义相似度：{{ formatScore(help.semanticScore) }}</p>
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

export default {
  name: 'Home',
  setup() {
    const router = useRouter()
    const activeTab = ref('products')
    const quickLinks = [
      { title: '商品广场', description: '浏览和发布二手商品。', path: '/product', type: 'danger' },
      { title: '招聘信息', description: '查看全职、兼职和实习岗位。', path: '/job', type: 'primary' },
      { title: '校园互助', description: '发布求助信息并响应同学需求。', path: '/help', type: 'success' },
      { title: '即时聊天', description: '联系卖家、招聘方和互助同学。', path: '/chat', type: 'warning' }
    ]
    const recommendedProducts = ref([])
    const recommendedJobs = ref([])
    const recommendedHelps = ref([])
    const currentUser = JSON.parse(localStorage.getItem('user') || '{}')
    const isDevelopment = import.meta.env.DEV

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
        console.error('加载推荐数据失败：', error)
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

    const goTo = (path) => {
      router.push(path)
    }

    const showRecommendationScores = (item) => {
      return isDevelopment && item && (typeof item.finalScore === 'number' || typeof item.semanticScore === 'number')
    }

    const formatScore = (value) => {
      return typeof value === 'number' ? value.toFixed(2) : '--'
    }

    onMounted(() => {
      loadRecommendations()
    })

    return {
      activeTab,
      quickLinks,
      recommendedProducts,
      recommendedJobs,
      recommendedHelps,
      handleTabChange,
      viewProduct,
      viewJob,
      viewHelp,
      getUrgencyType,
      goTo,
      showRecommendationScores,
      formatScore
    }
  }
}
</script>

<style scoped>
.home-container {
  padding: 20px;
}

.recommendation-header {
  margin-bottom: 12px;
}

.recommendation-tip {
  margin: 6px 0 0;
  color: #666;
  font-size: 14px;
}

.quick-nav {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
  gap: 16px;
  margin-bottom: 24px;
}

.quick-nav-card {
  cursor: pointer;
  transition: transform 0.2s;
}

.quick-nav-card:hover {
  transform: translateY(-4px);
}

.quick-nav-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 16px;
}

.quick-nav-content h3 {
  margin-bottom: 8px;
  color: #333;
}

.quick-nav-content p {
  color: #666;
  line-height: 1.5;
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

.score-meta {
  color: #909399;
  font-size: 12px;
  margin-top: 6px;
}
</style>
