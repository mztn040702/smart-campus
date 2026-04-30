<template>
  <div class="profile-page">
    <el-row :gutter="20">
      <el-col :xs="24" :lg="14">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span>个人资料</span>
              <el-button type="primary" :loading="profileLoading" @click="loadProfile">刷新</el-button>
            </div>
          </template>

          <el-form :model="profileForm" label-width="120px">
            <el-form-item label="用户名">
              <el-input v-model="profileForm.username" disabled />
            </el-form-item>
            <el-form-item label="角色">
              <el-input v-model="profileForm.role" disabled />
            </el-form-item>
            <el-form-item label="状态">
              <el-input v-model="profileForm.status" disabled />
            </el-form-item>
            <el-form-item label="真实姓名">
              <el-input v-model="profileForm.realName" disabled />
            </el-form-item>
            <el-form-item label="学院">
              <el-input v-model="profileForm.college" disabled />
            </el-form-item>
            <el-form-item label="昵称">
              <el-input v-model="profileForm.nickname" placeholder="请输入昵称" />
            </el-form-item>
            <el-form-item label="邮箱">
              <el-input v-model="profileForm.email" placeholder="请输入邮箱" />
            </el-form-item>
            <el-form-item label="手机号">
              <el-input v-model="profileForm.phone" placeholder="请输入手机号" />
            </el-form-item>
            <el-form-item label="头像链接">
              <el-input v-model="profileForm.avatar" placeholder="请输入头像地址" />
            </el-form-item>
            <el-form-item label="上传头像">
              <div class="avatar-upload">
                <el-upload
                  :show-file-list="false"
                  :http-request="uploadAvatar"
                  :before-upload="beforeImageUpload"
                  accept=".jpg,.jpeg,.png,.gif,.webp"
                >
                  <el-button type="primary" :loading="avatarUploading">上传头像</el-button>
                </el-upload>
                <img
                  v-if="profileForm.avatar"
                  :src="resolveImageUrl(profileForm.avatar)"
                  alt="avatar preview"
                  class="avatar-preview"
                />
              </div>
            </el-form-item>
            <el-form-item label="个人简介">
              <el-input
                v-model="profileForm.bio"
                type="textarea"
                :rows="4"
                placeholder="介绍一下自己"
              />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" :loading="profileSaving" @click="saveProfile">
                保存资料
              </el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-col>

      <el-col :xs="24" :lg="10">
        <el-card shadow="hover">
          <template #header>
            <span>修改密码</span>
          </template>

          <el-form :model="passwordForm" label-width="120px">
            <el-form-item label="原密码">
              <el-input
                v-model="passwordForm.oldPassword"
                type="password"
                show-password
                placeholder="请输入原密码"
              />
            </el-form-item>
            <el-form-item label="新密码">
              <el-input
                v-model="passwordForm.newPassword"
                type="password"
                show-password
                placeholder="请输入新密码"
              />
            </el-form-item>
            <el-form-item label="确认密码">
              <el-input
                v-model="passwordForm.confirmPassword"
                type="password"
                show-password
                placeholder="请再次输入新密码"
              />
            </el-form-item>
            <el-form-item>
              <el-button type="warning" :loading="passwordSaving" @click="changePassword">
                更新密码
              </el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import request from '../utils/axios'
import { clearSession, getStoredSession, saveSession } from '../utils/auth.mjs'

const router = useRouter()
const profileLoading = ref(false)
const profileSaving = ref(false)
const passwordSaving = ref(false)
const avatarUploading = ref(false)

const profileForm = reactive({
  id: '',
  username: '',
  role: '',
  status: '',
  realName: '',
  college: '',
  nickname: '',
  email: '',
  phone: '',
  avatar: '',
  bio: ''
})

const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

function applyProfile(user = {}) {
  profileForm.id = user.id || ''
  profileForm.username = user.username || ''
  profileForm.role = user.role || ''
  profileForm.status = user.status || ''
  profileForm.realName = user.realName || ''
  profileForm.college = user.college || ''
  profileForm.nickname = user.nickname || ''
  profileForm.email = user.email || ''
  profileForm.phone = user.phone || ''
  profileForm.avatar = user.avatar || ''
  profileForm.bio = user.bio || ''
}

function resolveImageUrl(url) {
  if (!url) {
    return ''
  }
  return url.startsWith('http') ? url : url
}

function beforeImageUpload(file) {
  const allowedTypes = ['image/jpeg', 'image/png', 'image/gif', 'image/webp']
  if (!allowedTypes.includes(file.type)) {
    ElMessage.error('仅支持 jpg、jpeg、png、gif、webp 图片')
    return false
  }
  if (file.size / 1024 / 1024 > 5) {
    ElMessage.error('图片大小不能超过 5MB')
    return false
  }
  return true
}

const uploadAvatar = async ({ file, onSuccess, onError }) => {
  avatarUploading.value = true
  try {
    const formData = new FormData()
    formData.append('image', file)
    const res = await request.post('/upload/image', formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })

    if (res.code === 0) {
      profileForm.avatar = res.data.url
      ElMessage.success('头像上传成功')
      onSuccess?.(res.data)
    } else {
      throw new Error(res.msg || '上传失败')
    }
  } catch (error) {
    ElMessage.error(error.response?.data?.msg || error.message || '上传失败')
    onError?.(error)
  } finally {
    avatarUploading.value = false
  }
}

async function loadProfile() {
  profileLoading.value = true
  try {
    const res = await request.get('/user/profile')
    if (res.code === 0) {
      applyProfile(res.data)
      const session = getStoredSession()
      saveSession(res.data, session.token)
    } else {
      ElMessage.error(res.msg || '加载个人资料失败')
    }
  } catch (error) {
    ElMessage.error(error.response?.data?.msg || error.message || '加载个人资料失败')
  } finally {
    profileLoading.value = false
  }
}

async function saveProfile() {
  profileSaving.value = true
  try {
    const res = await request.put('/user/profile', {
      nickname: profileForm.nickname,
      email: profileForm.email,
      phone: profileForm.phone,
      bio: profileForm.bio,
      avatar: profileForm.avatar
    })

    if (res.code === 0) {
      applyProfile(res.data)
      const session = getStoredSession()
      saveSession(res.data, session.token)
      ElMessage.success('个人资料已更新')
    } else {
      ElMessage.error(res.msg || '更新个人资料失败')
    }
  } catch (error) {
    ElMessage.error(error.response?.data?.msg || error.message || '更新个人资料失败')
  } finally {
    profileSaving.value = false
  }
}

async function changePassword() {
  if (!passwordForm.oldPassword || !passwordForm.newPassword || !passwordForm.confirmPassword) {
    ElMessage.warning('请完整填写密码信息')
    return
  }

  if (passwordForm.newPassword !== passwordForm.confirmPassword) {
    ElMessage.warning('两次输入的新密码不一致')
    return
  }

  passwordSaving.value = true
  try {
    const res = await request.put('/user/password', {
      oldPassword: passwordForm.oldPassword,
      newPassword: passwordForm.newPassword
    })

    if (res.code === 0) {
      clearSession()
      ElMessage.success('密码修改成功，请重新登录')
      router.push('/login')
    } else {
      ElMessage.error(res.msg || '修改密码失败')
    }
  } catch (error) {
    ElMessage.error(error.response?.data?.msg || error.message || '修改密码失败')
  } finally {
    passwordSaving.value = false
  }
}

onMounted(() => {
  loadProfile()
})
</script>

<style scoped>
.profile-page {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.avatar-upload {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 12px;
}

.avatar-preview {
  width: 120px;
  height: 120px;
  object-fit: cover;
  border-radius: 50%;
  border: 1px solid #dcdfe6;
}
</style>
