<template>
  <div class="register-container">
    <el-card class="register-card">
      <h2>智慧校园系统 - 注册</h2>
      <el-form :model="form" label-width="80px" style="margin-top: 30px;">
        <el-form-item label="用户名">
          <el-input v-model="form.username" placeholder="请输入用户名"></el-input>
        </el-form-item>
        <el-form-item label="密码">
          <el-input v-model="form.password" type="password" placeholder="请输入密码"></el-input>
        </el-form-item>
        <el-form-item label="真实姓名">
          <el-input v-model="form.realName" placeholder="请输入真实姓名"></el-input>
        </el-form-item>
        <el-form-item label="学院">
          <el-input v-model="form.college" placeholder="请输入学院"></el-input>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleRegister" :loading="loading" style="width: 100%;">注册</el-button>
        </el-form-item>
        <el-form-item>
          <el-link type="primary" @click="$router.push('/login')">已有账号？立即登录</el-link>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import axios from '../utils/axios'
import { ElMessage } from 'element-plus'

export default {
  name: 'Register',
  setup() {
    const router = useRouter()
    const form = ref({
      username: '',
      password: '',
      realName: '',
      college: ''
    })
    const loading = ref(false)

    const handleRegister = async () => {
      if (!form.value.username || !form.value.password || !form.value.realName || !form.value.college) {
        ElMessage.warning('请填写完整信息')
        return
      }
      loading.value = true
      try {
        const res = await axios.post('/user/register', form.value)
        if (res.code === 0) {
          ElMessage.success('注册成功，请登录')
          router.push('/login')
        } else {
          ElMessage.error(res.msg || '注册失败')
        }
      } catch (error) {
        ElMessage.error('注册失败：' + (error.response?.data?.msg || error.message))
      } finally {
        loading.value = false
      }
    }

    return {
      form,
      loading,
      handleRegister
    }
  }
}
</script>

<style scoped>
.register-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.register-card {
  width: 400px;
  padding: 20px;
}

.register-card h2 {
  text-align: center;
  margin-bottom: 20px;
  color: #333;
}
</style>

