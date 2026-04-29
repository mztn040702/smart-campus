<template>
  <div class="profile-page">
    <el-row :gutter="20">
      <el-col :xs="24" :lg="14">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span>Personal Profile</span>
              <el-button type="primary" :loading="profileLoading" @click="loadProfile">Refresh</el-button>
            </div>
          </template>

          <el-form :model="profileForm" label-width="120px">
            <el-form-item label="Username">
              <el-input v-model="profileForm.username" disabled />
            </el-form-item>
            <el-form-item label="Role">
              <el-input v-model="profileForm.role" disabled />
            </el-form-item>
            <el-form-item label="Status">
              <el-input v-model="profileForm.status" disabled />
            </el-form-item>
            <el-form-item label="Real Name">
              <el-input v-model="profileForm.realName" disabled />
            </el-form-item>
            <el-form-item label="College">
              <el-input v-model="profileForm.college" disabled />
            </el-form-item>
            <el-form-item label="Nickname">
              <el-input v-model="profileForm.nickname" placeholder="Enter nickname" />
            </el-form-item>
            <el-form-item label="Email">
              <el-input v-model="profileForm.email" placeholder="Enter email" />
            </el-form-item>
            <el-form-item label="Phone">
              <el-input v-model="profileForm.phone" placeholder="Enter phone" />
            </el-form-item>
            <el-form-item label="Avatar URL">
              <el-input v-model="profileForm.avatar" placeholder="Enter avatar URL" />
            </el-form-item>
            <el-form-item label="Bio">
              <el-input
                v-model="profileForm.bio"
                type="textarea"
                :rows="4"
                placeholder="Write something about yourself"
              />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" :loading="profileSaving" @click="saveProfile">
                Save Profile
              </el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-col>

      <el-col :xs="24" :lg="10">
        <el-card shadow="hover">
          <template #header>
            <span>Change Password</span>
          </template>

          <el-form :model="passwordForm" label-width="120px">
            <el-form-item label="Old Password">
              <el-input
                v-model="passwordForm.oldPassword"
                type="password"
                show-password
                placeholder="Enter old password"
              />
            </el-form-item>
            <el-form-item label="New Password">
              <el-input
                v-model="passwordForm.newPassword"
                type="password"
                show-password
                placeholder="Enter new password"
              />
            </el-form-item>
            <el-form-item label="Confirm Password">
              <el-input
                v-model="passwordForm.confirmPassword"
                type="password"
                show-password
                placeholder="Confirm new password"
              />
            </el-form-item>
            <el-form-item>
              <el-button type="warning" :loading="passwordSaving" @click="changePassword">
                Update Password
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

async function loadProfile() {
  profileLoading.value = true
  try {
    const res = await request.get('/user/profile')
    if (res.code === 0) {
      applyProfile(res.data)
      const session = getStoredSession()
      saveSession(res.data, session.token)
    } else {
      ElMessage.error(res.msg || 'Failed to load profile')
    }
  } catch (error) {
    ElMessage.error(error.response?.data?.msg || error.message || 'Failed to load profile')
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
      ElMessage.success('Profile updated')
    } else {
      ElMessage.error(res.msg || 'Failed to update profile')
    }
  } catch (error) {
    ElMessage.error(error.response?.data?.msg || error.message || 'Failed to update profile')
  } finally {
    profileSaving.value = false
  }
}

async function changePassword() {
  if (!passwordForm.oldPassword || !passwordForm.newPassword || !passwordForm.confirmPassword) {
    ElMessage.warning('Please complete all password fields')
    return
  }

  if (passwordForm.newPassword !== passwordForm.confirmPassword) {
    ElMessage.warning('New passwords do not match')
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
      ElMessage.success('Password updated. Please log in again.')
      router.push('/login')
    } else {
      ElMessage.error(res.msg || 'Failed to change password')
    }
  } catch (error) {
    ElMessage.error(error.response?.data?.msg || error.message || 'Failed to change password')
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
</style>
