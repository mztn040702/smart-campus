<template>
  <div class="login-container">
    <el-card class="login-card">
      <h2>Smart Campus - Login</h2>
      <el-form :model="form" label-width="80px" style="margin-top: 30px;">
        <el-form-item label="User">
          <el-input v-model="form.username" placeholder="Enter username"></el-input>
        </el-form-item>
        <el-form-item label="Password">
          <el-input v-model="form.password" type="password" placeholder="Enter password" @keyup.enter="handleLogin"></el-input>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleLogin" :loading="loading" style="width: 100%;">Login</el-button>
        </el-form-item>
        <el-form-item>
          <el-link type="primary" @click="$router.push('/register')">No account? Register now</el-link>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script>
import { ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import axios from '../utils/axios'
import { saveSession } from '../utils/auth.mjs'
import { ElMessage } from 'element-plus'

export default {
  name: 'Login',
  setup() {
    const router = useRouter()
    const route = useRoute()
    const form = ref({
      username: '',
      password: ''
    })
    const loading = ref(false)

    const handleLogin = async () => {
      if (!form.value.username || !form.value.password) {
        ElMessage.warning('Please enter username and password')
        return
      }
      loading.value = true
      try {
        const res = await axios.post('/user/login', form.value)
        if (res.code === 0) {
          saveSession(res.data, res.token)
          ElMessage.success('Login successful')
          router.push(route.query.redirect || '/home')
        } else {
          ElMessage.error(res.msg || 'Login failed')
        }
      } catch (error) {
        ElMessage.error('Login failed: ' + (error.response?.data?.msg || error.message))
      } finally {
        loading.value = false
      }
    }

    return {
      form,
      loading,
      handleLogin
    }
  }
}
</script>

<style scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.login-card {
  width: 400px;
  padding: 20px;
}

.login-card h2 {
  text-align: center;
  margin-bottom: 20px;
  color: #333;
}
</style>
