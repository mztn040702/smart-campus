<template>
  <div class="product-container">
    <div class="header-actions">
      <el-button type="primary" @click="showPublishDialog = true">发布商品</el-button>
      <el-input
        v-model="searchKeyword"
        placeholder="搜索商品..."
        style="width: 300px; margin-left: 20px;"
        @keyup.enter="handleSearch"
      >
        <template #append>
          <el-button @click="handleSearch">搜索</el-button>
        </template>
      </el-input>
    </div>

    <el-tabs v-model="activeCategory" @tab-change="handleCategoryChange">
      <el-tab-pane label="全部" name=""></el-tab-pane>
      <el-tab-pane label="书籍" name="书籍"></el-tab-pane>
      <el-tab-pane label="电子产品" name="电子产品"></el-tab-pane>
      <el-tab-pane label="生活用品" name="生活用品"></el-tab-pane>
      <el-tab-pane label="其他" name="其他"></el-tab-pane>
    </el-tabs>

    <div class="products-grid">
      <el-card
        v-for="product in products"
        :key="product.id"
        class="product-card"
        @click="viewProductDetail(product)"
      >
        <div class="product-image">?</div>
        <div class="product-info">
          <h3>{{ product.title }}</h3>
          <p class="price">?{{ product.price }}</p>
          <p class="description">{{ product.description }}</p>
          <el-tag size="small">{{ product.category }}</el-tag>
          <p class="view-count">浏览 {{ product.viewCount }} 次</p>
        </div>
      </el-card>
    </div>

    <!-- 发布商品对话框 -->
    <el-dialog v-model="showPublishDialog" title="发布商品" width="600px">
      <el-form :model="publishForm" label-width="100px">
        <el-form-item label="商品标题" required>
          <el-input v-model="publishForm.title" placeholder="请输入商品标题"></el-input>
        </el-form-item>
        <el-form-item label="商品描述" required>
          <el-input
            v-model="publishForm.description"
            type="textarea"
            :rows="4"
            placeholder="请输入商品描述"
          ></el-input>
        </el-form-item>
        <el-form-item label="价格" required>
          <el-input-number v-model="publishForm.price" :min="0" :precision="2" style="width: 100%;"></el-input-number>
        </el-form-item>
        <el-form-item label="分类" required>
          <el-select v-model="publishForm.category" placeholder="请选择分类" style="width: 100%;">
            <el-option label="书籍" value="书籍"></el-option>
            <el-option label="电子产品" value="电子产品"></el-option>
            <el-option label="生活用品" value="生活用品"></el-option>
            <el-option label="其他" value="其他"></el-option>
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showPublishDialog = false">取消</el-button>
        <el-button type="primary" @click="handlePublish" :loading="publishing">发布</el-button>
      </template>
    </el-dialog>

    <!-- 商品详情对话框 -->
    <el-dialog v-model="showDetailDialog" title="商品详情" width="700px">
      <div v-if="selectedProduct">
        <h2>{{ selectedProduct.title }}</h2>
        <p class="detail-price">?{{ selectedProduct.price }}</p>
        <p><strong>分类：</strong><el-tag>{{ selectedProduct.category }}</el-tag></p>
        <p><strong>描述：</strong>{{ selectedProduct.description }}</p>
        <p><strong>浏览次数：</strong>{{ selectedProduct.viewCount }}</p>
        <el-button type="primary" @click="contactSeller">联系卖家</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import axios from '../utils/axios'
import { ElMessage } from 'element-plus'

export default {
  name: 'Product',
  setup() {
    const router = useRouter()
    const currentUser = JSON.parse(localStorage.getItem('user') || '{}')
    const products = ref([])
    const activeCategory = ref('')
    const searchKeyword = ref('')
    const showPublishDialog = ref(false)
    const showDetailDialog = ref(false)
    const selectedProduct = ref(null)
    const publishing = ref(false)

    const publishForm = ref({
      title: '',
      description: '',
      price: 0,
      category: ''
    })

    const loadProducts = async () => {
      try {
        let res
        if (searchKeyword.value) {
          res = await axios.get('/product/search', { params: { keyword: searchKeyword.value } })
        } else if (activeCategory.value) {
          res = await axios.get('/product/list', { params: { category: activeCategory.value } })
        } else {
          res = await axios.get('/product/list')
        }
        if (res.code === 0) {
          products.value = res.data
        }
      } catch (error) {
        console.error('加载商品失败:', error)
      }
    }

    const handleCategoryChange = () => {
      searchKeyword.value = ''
      loadProducts()
    }

    const handleSearch = () => {
      activeCategory.value = ''
      loadProducts()
    }

    const handlePublish = async () => {
      if (!publishForm.value.title || !publishForm.value.description || !publishForm.value.category) {
        ElMessage.warning('请填写完整信息')
        return
      }
      publishing.value = true
      try {
        const res = await axios.post('/product/publish', {
          sellerId: currentUser.id,
          ...publishForm.value
        })
        if (res.code === 0) {
          ElMessage.success('发布成功')
          showPublishDialog.value = false
          publishForm.value = { title: '', description: '', price: 0, category: '' }
          loadProducts()
          // 记录偏好
          await axios.post('/recommend/preference', {
            userId: currentUser.id,
            category: 'product',
            keyword: publishForm.value.category
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

    const viewProductDetail = async (product) => {
      try {
        const res = await axios.get(`/product/${product.id}`)
        if (res.code === 0) {
          selectedProduct.value = res.data
          showDetailDialog.value = true
          // 记录偏好
          await axios.post('/recommend/preference', {
            userId: currentUser.id,
            category: 'product',
            keyword: res.data.category
          })
        }
      } catch (error) {
        ElMessage.error('加载详情失败')
      }
    }

    const contactSeller = () => {
      if (selectedProduct.value) {
        router.push(`/chat?userId=${selectedProduct.value.sellerId}`)
      }
    }

    onMounted(() => {
      loadProducts()
    })

    return {
      products,
      activeCategory,
      searchKeyword,
      showPublishDialog,
      showDetailDialog,
      selectedProduct,
      publishing,
      publishForm,
      handleCategoryChange,
      handleSearch,
      handlePublish,
      viewProductDetail,
      contactSeller
    }
  }
}
</script>

<style scoped>
.product-container {
  padding: 20px;
}

.header-actions {
  display: flex;
  align-items: center;
  margin-bottom: 20px;
}

.products-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 20px;
  margin-top: 20px;
}

.product-card {
  cursor: pointer;
  transition: transform 0.2s;
}

.product-card:hover {
  transform: translateY(-5px);
}

.product-image {
  width: 100%;
  height: 200px;
  background-color: #f5f5f5;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 60px;
  margin-bottom: 10px;
}

.product-info h3 {
  margin-bottom: 10px;
  color: #333;
}

.price {
  color: #f56c6c;
  font-size: 20px;
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

.detail-price {
  color: #f56c6c;
  font-size: 24px;
  font-weight: bold;
  margin: 15px 0;
}
</style>

