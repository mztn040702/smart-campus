import { createRouter, createWebHistory } from 'vue-router'
import Login from '../views/Login.vue'
import Register from '../views/Register.vue'
import Home from '../views/Home.vue'
import Chat from '../views/Chat.vue'
import Product from '../views/Product.vue'
import Job from '../views/Job.vue'
import Help from '../views/Help.vue'
import Admin from '../views/Admin.vue'
import Profile from '../views/Profile.vue'
import { getStoredSession } from '../utils/auth.mjs'
import { ADMIN_PATHS, canAccessRoute } from './access.mjs'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: Login,
    meta: { public: true, title: '登录' }
  },
  {
    path: '/register',
    name: 'Register',
    component: Register,
    meta: { public: true, title: '注册' }
  },
  {
    path: '/home',
    name: 'Home',
    component: Home,
    meta: { title: '首页' }
  },
  {
    path: '/chat',
    name: 'Chat',
    component: Chat,
    meta: { title: '聊天' }
  },
  {
    path: '/product',
    name: 'Product',
    component: Product,
    meta: { title: '商品' }
  },
  {
    path: '/job',
    name: 'Job',
    component: Job,
    meta: { title: '招聘' }
  },
  {
    path: '/help',
    name: 'Help',
    component: Help,
    meta: { title: '互助' }
  },
  {
    path: '/profile',
    name: 'Profile',
    component: Profile,
    meta: { title: '个人中心' }
  },
  {
    path: '/admin',
    name: 'Admin',
    component: Admin,
    meta: { title: '管理后台' }
  },
  {
    path: '/',
    redirect: '/home'
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to) => {
  const session = getStoredSession()

  if (!canAccessRoute(to.path, session)) {
    if (session.user?.id && session.token && ADMIN_PATHS.includes(to.path)) {
      return { path: '/home' }
    }

    return {
      path: '/login',
      query: { redirect: to.fullPath }
    }
  }

  return true
})

router.afterEach((to) => {
  const pageTitle = to.meta?.title
  document.title = pageTitle ? `${pageTitle} - 智慧校园系统` : '智慧校园系统'
})

export default router
