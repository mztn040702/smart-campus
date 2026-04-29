<template>
  <div id="app">
    <el-container v-if="isLoggedIn">
      <el-header>
        <div class="header-content">
          <h2>智慧校园系统</h2>
          <div class="user-info">
            <span>欢迎，{{ currentUser.realName || currentUser.username }}</span>
            <el-button type="danger" size="small" @click="logout">退出</el-button>
          </div>
        </div>
      </el-header>
      <el-container>
        <el-aside width="200px">
          <el-menu
            :default-active="activeMenu"
            router
            class="sidebar-menu"
          >
            <el-menu-item index="/home">
              <el-icon><House /></el-icon>
              <span>首页推荐</span>
            </el-menu-item>
            <el-menu-item index="/chat">
              <el-icon><ChatLineRound /></el-icon>
              <span>消息</span>
            </el-menu-item>
            <el-menu-item index="/product">
              <el-icon><ShoppingBag /></el-icon>
              <span>二手交易</span>
            </el-menu-item>
            <el-menu-item index="/job">
              <el-icon><Briefcase /></el-icon>
              <span>求职招聘</span>
            </el-menu-item>
            <el-menu-item index="/help">
              <el-icon><HelpFilled /></el-icon>
              <span>互助</span>
            </el-menu-item>
          </el-menu>
        </el-aside>
        <el-main>
          <router-view />
        </el-main>
      </el-container>
    </el-container>
    <router-view v-else />
  </div>
</template>

<script>
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'

export default {
  name: 'App',
  setup() {
    const router = useRouter()
    const route = useRoute()
    const currentUser = ref(JSON.parse(localStorage.getItem('user') || '{}'))
    const token = ref(localStorage.getItem('token') || '')
    const isLoggedIn = computed(() => currentUser.value.id && token.value && route.path !== '/login' && route.path !== '/register')
    const activeMenu = computed(() => route.path)

    const logout = () => {
      localStorage.removeItem('user')
      localStorage.removeItem('token')
      currentUser.value = {}
      token.value = ''
      router.push('/login')
    }

    onMounted(() => {
      if (!isLoggedIn.value && route.path !== '/login' && route.path !== '/register') {
        router.push('/login')
      }
    })

    return {
      currentUser,
      isLoggedIn,
      activeMenu,
      logout
    }
  }
}
</script>

<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

#app {
  font-family: 'Helvetica Neue', Helvetica, 'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei', Arial, sans-serif;
  height: 100vh;
}

.el-header {
  background-color: #409EFF;
  color: white;
  display: flex;
  align-items: center;
  padding: 0 20px;
}

.header-content {
  width: 100%;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 15px;
}

.el-aside {
  background-color: #f5f5f5;
}

.sidebar-menu {
  height: 100%;
  border-right: none;
}

.el-main {
  background-color: #f0f2f5;
  padding: 20px;
}
</style>