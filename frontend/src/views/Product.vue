<template>
  <div class="product-container">
    <div class="header-actions">
      <el-button type="primary" @click="showPublishDialog = true">发布商品</el-button>
    </div>

    <el-card class="filter-panel" shadow="never">
      <div class="filter-grid">
        <el-input
          v-model="filters.keyword"
          placeholder="搜索商品关键词"
          clearable
          @keyup.enter="handleSearch"
        />
        <el-select v-model="filters.category" placeholder="全部分类" clearable>
          <el-option
            v-for="category in productCategories"
            :key="category.value"
            :label="category.label"
            :value="category.value"
          />
        </el-select>
        <el-input-number
          v-model="filters.minPrice"
          :min="0"
          :precision="2"
          :controls="false"
          placeholder="最低价"
        />
        <el-input-number
          v-model="filters.maxPrice"
          :min="0"
          :precision="2"
          :controls="false"
          placeholder="最高价"
        />
        <el-select v-model="filters.sort" placeholder="排序方式">
          <el-option label="最新发布" value="latest" />
          <el-option label="价格从低到高" value="priceAsc" />
          <el-option label="价格从高到低" value="priceDesc" />
          <el-option label="浏览量优先" value="viewsDesc" />
        </el-select>
        <div class="filter-actions">
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="resetFilters">重置</el-button>
        </div>
      </div>
    </el-card>

    <div class="products-grid">
      <el-card
        v-for="product in products"
        :key="product.id"
        class="product-card"
        @click="viewProductDetail(product)"
      >
        <img
          v-if="getProductImage(product)"
          :src="getProductImage(product)"
          alt="product"
          class="product-image preview-image"
        />
        <div v-else class="product-image">?</div>
        <div class="product-info">
          <h3>{{ product.title }}</h3>
          <p class="price">￥{{ product.price }}</p>
          <p class="description">{{ product.description }}</p>
          <el-tag size="small">{{ getCategoryLabel(product.category) }}</el-tag>
          <p class="view-count">浏览 {{ product.viewCount }} 次</p>
        </div>
      </el-card>
    </div>

    <el-dialog v-model="showPublishDialog" title="发布商品" width="600px">
      <el-form :model="publishForm" label-width="100px">
        <el-form-item label="商品标题" required>
          <el-input v-model="publishForm.title" placeholder="请输入商品标题" />
        </el-form-item>
        <el-form-item label="商品描述" required>
          <el-input
            v-model="publishForm.description"
            type="textarea"
            :rows="4"
            placeholder="请输入商品描述"
          />
        </el-form-item>
        <el-form-item label="价格" required>
          <el-input-number
            v-model="publishForm.price"
            :min="0"
            :precision="2"
            style="width: 100%;"
          />
        </el-form-item>
        <el-form-item label="分类" required>
          <el-select v-model="publishForm.category" placeholder="请选择分类" style="width: 100%;">
            <el-option
              v-for="category in productCategories"
              :key="`publish-${category.value}`"
              :label="category.label"
              :value="category.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="Image">
          <div class="upload-block">
            <el-upload
              :show-file-list="false"
              :http-request="uploadProductImage"
              :before-upload="beforeImageUpload"
              accept=".jpg,.jpeg,.png,.gif,.webp"
            >
              <el-button type="primary" :loading="imageUploading">Upload Image</el-button>
            </el-upload>
            <img
              v-if="publishForm.images"
              :src="resolveImageUrl(publishForm.images)"
              alt="product preview"
              class="upload-preview"
            />
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showPublishDialog = false">取消</el-button>
        <el-button type="primary" @click="handlePublish" :loading="publishing">发布</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="showDetailDialog" title="商品详情" width="700px">
      <div v-if="selectedProduct">
        <h2>{{ selectedProduct.title }}</h2>
        <img
          v-if="getProductImage(selectedProduct)"
          :src="getProductImage(selectedProduct)"
          alt="product detail"
          class="detail-image"
        />
        <p class="detail-price">￥{{ selectedProduct.price }}</p>
        <p><strong>分类：</strong><el-tag>{{ getCategoryLabel(selectedProduct.category) }}</el-tag></p>
        <p><strong>描述：</strong>{{ selectedProduct.description }}</p>
        <p><strong>浏览次数：</strong>{{ selectedProduct.viewCount }}</p>
        <el-button type="primary" @click="contactSeller">联系卖家</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import axios from '../utils/axios'
import { ElMessage } from 'element-plus'

const productCategories = [
  { label: '书籍', value: 'books' },
  { label: '电子产品', value: 'electronics' },
  { label: '生活用品', value: 'daily' },
  { label: '其他', value: 'other' }
]

const productCategoryLabels = {
  books: '书籍',
  '书籍': '书籍',
  electronics: '电子产品',
  '电子产品': '电子产品',
  daily: '生活用品',
  'daily用品': '生活用品',
  '生活用品': '生活用品',
  other: '其他',
  '其他': '其他'
}

export default {
  name: 'Product',
  setup() {
    const router = useRouter()
    const currentUser = JSON.parse(localStorage.getItem('user') || '{}')
    const products = ref([])
    const showPublishDialog = ref(false)
    const showDetailDialog = ref(false)
    const selectedProduct = ref(null)
    const publishing = ref(false)
    const imageUploading = ref(false)
    const filters = ref({
      keyword: '',
      category: '',
      minPrice: null,
      maxPrice: null,
      sort: 'latest'
    })

    const publishForm = ref({
      title: '',
      description: '',
      price: 0,
      category: '',
      images: ''
    })

    const resolveImageUrl = (url) => {
      if (!url) {
        return ''
      }
      return url.startsWith('http') ? url : url
    }

    const getProductImage = (product) => resolveImageUrl(product?.images || '')
    const getCategoryLabel = (category) => productCategoryLabels[category] || category

    const buildProductQueryParams = () => {
      const params = {}
      if (filters.value.keyword) {
        params.keyword = filters.value.keyword.trim()
      }
      if (filters.value.category) {
        params.category = filters.value.category
      }
      if (filters.value.minPrice !== null && filters.value.minPrice !== undefined) {
        params.minPrice = filters.value.minPrice
      }
      if (filters.value.maxPrice !== null && filters.value.maxPrice !== undefined) {
        params.maxPrice = filters.value.maxPrice
      }
      if (filters.value.sort) {
        params.sort = filters.value.sort
      }
      return params
    }

    const beforeImageUpload = (file) => {
      const allowedTypes = ['image/jpeg', 'image/png', 'image/gif', 'image/webp']
      if (!allowedTypes.includes(file.type)) {
        ElMessage.error('Only jpg, jpeg, png, gif, webp images are allowed')
        return false
      }
      if (file.size / 1024 / 1024 > 5) {
        ElMessage.error('Image size must be 5MB or less')
        return false
      }
      return true
    }

    const uploadProductImage = async ({ file, onSuccess, onError }) => {
      imageUploading.value = true
      try {
        const formData = new FormData()
        formData.append('image', file)
        const res = await axios.post('/upload/image', formData, {
          headers: {
            'Content-Type': 'multipart/form-data'
          }
        })
        if (res.code === 0) {
          publishForm.value.images = res.data.url
          ElMessage.success('Image uploaded')
          onSuccess?.(res.data)
        } else {
          throw new Error(res.msg || 'Upload failed')
        }
      } catch (error) {
        ElMessage.error(error.response?.data?.msg || error.message || 'Upload failed')
        onError?.(error)
      } finally {
        imageUploading.value = false
      }
    }

    const loadProducts = async () => {
      try {
        const res = await axios.get('/product/list', { params: buildProductQueryParams() })
        if (res.code === 0) {
          products.value = res.data
        }
      } catch (error) {
        console.error('加载商品失败:', error)
      }
    }

    const handleSearch = () => {
      loadProducts()
    }

    const resetFilters = () => {
      filters.value = {
        keyword: '',
        category: '',
        minPrice: null,
        maxPrice: null,
        sort: 'latest'
      }
      loadProducts()
    }

    const handlePublish = async () => {
      if (!publishForm.value.title || !publishForm.value.description || !publishForm.value.category) {
        ElMessage.warning('请填写完整信息')
        return
      }
      publishing.value = true
      try {
        const preferenceKeyword = publishForm.value.category
        const res = await axios.post('/product/publish', {
          sellerId: currentUser.id,
          ...publishForm.value
        })
        if (res.code === 0) {
          ElMessage.success('发布成功')
          showPublishDialog.value = false
          publishForm.value = { title: '', description: '', price: 0, category: '', images: '' }
          loadProducts()
          await axios.post('/recommend/preference', {
            userId: currentUser.id,
            category: 'product',
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

    const viewProductDetail = async (product) => {
      try {
        const res = await axios.get(`/product/${product.id}`)
        if (res.code === 0) {
          selectedProduct.value = res.data
          showDetailDialog.value = true
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
      productCategories,
      filters,
      showPublishDialog,
      showDetailDialog,
      selectedProduct,
      publishing,
      imageUploading,
      publishForm,
      resolveImageUrl,
      getCategoryLabel,
      getProductImage,
      buildProductQueryParams,
      beforeImageUpload,
      uploadProductImage,
      handleSearch,
      resetFilters,
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

.preview-image {
  object-fit: cover;
  border-radius: 8px;
}

.upload-block {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 12px;
}

.upload-preview {
  width: 160px;
  height: 160px;
  object-fit: cover;
  border-radius: 8px;
  border: 1px solid #dcdfe6;
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

.detail-image {
  width: 100%;
  max-height: 320px;
  object-fit: cover;
  border-radius: 12px;
  margin: 12px 0 16px;
}
</style>
