<template>
  <div id="app">
    <el-container v-if="showShell" class="app-shell">
      <el-header>
        <div class="header-content">
          <h2>Smart Campus</h2>
          <div class="user-info">
            <span>Welcome, {{ displayName }}</span>
            <el-button type="danger" size="small" @click="logout">Logout</el-button>
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
              <span>Home</span>
            </el-menu-item>
            <el-menu-item index="/product">
              <el-icon><ShoppingBag /></el-icon>
              <span>Product</span>
            </el-menu-item>
            <el-menu-item index="/job">
              <el-icon><Briefcase /></el-icon>
              <span>Job</span>
            </el-menu-item>
            <el-menu-item index="/help">
              <el-icon><HelpFilled /></el-icon>
              <span>Help</span>
            </el-menu-item>
            <el-menu-item index="/chat">
              <el-icon><ChatLineRound /></el-icon>
              <span>Chat</span>
            </el-menu-item>
            <el-menu-item index="/profile">
              <el-icon><User /></el-icon>
              <span>Profile</span>
            </el-menu-item>
            <el-menu-item v-if="isAdmin" index="/admin">
              <el-icon><DataAnalysis /></el-icon>
              <span>Admin</span>
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
import { computed, onBeforeUnmount, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { PUBLIC_PATHS } from './router/access.mjs'
import { clearSession, syncSessionState, useSessionState } from './utils/auth.mjs'

const SHELL_MENU_PATHS = ['/home', '/product', '/job', '/help', '/chat', '/profile', '/admin']

export default {
  name: 'App',
  setup() {
    const router = useRouter()
    const route = useRoute()
    const session = useSessionState()
    const isAuthPage = computed(() => PUBLIC_PATHS.includes(route.path))
    const showShell = computed(() => Boolean(session.user?.id && session.token) && !isAuthPage.value)
    const isAdmin = computed(() => String(session.user?.role || '').toUpperCase() === 'ADMIN')
    const activeMenu = computed(() => {
      if (SHELL_MENU_PATHS.includes(route.path) && (route.path !== '/admin' || isAdmin.value)) {
        return route.path
      }
      return '/home'
    })
    const displayName = computed(() => session.user?.nickname || session.user?.realName || session.user?.username || '')

    const logout = () => {
      clearSession()
      router.push('/login')
    }

    const handleStorageChange = () => {
      syncSessionState()
    }

    onMounted(() => {
      syncSessionState()
      window.addEventListener('storage', handleStorageChange)
    })

    onBeforeUnmount(() => {
      window.removeEventListener('storage', handleStorageChange)
    })

    return {
      showShell,
      isAdmin,
      activeMenu,
      displayName,
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

.app-shell {
  min-height: 100vh;
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
