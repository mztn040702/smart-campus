import { createRouter, createWebHistory } from 'vue-router'
import Login from '../views/Login.vue'
import Register from '../views/Register.vue'
import Home from '../views/Home.vue'
import Chat from '../views/Chat.vue'
import Product from '../views/Product.vue'
import Job from '../views/Job.vue'
import Help from '../views/Help.vue'
import Admin from '../views/Admin.vue'
import { getStoredSession } from '../utils/auth.mjs'
import { ADMIN_PATHS, canAccessRoute } from './access.mjs'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: Login,
    meta: { public: true }
  },
  {
    path: '/register',
    name: 'Register',
    component: Register,
    meta: { public: true }
  },
  {
    path: '/home',
    name: 'Home',
    component: Home
  },
  {
    path: '/chat',
    name: 'Chat',
    component: Chat
  },
  {
    path: '/product',
    name: 'Product',
    component: Product
  },
  {
    path: '/job',
    name: 'Job',
    component: Job
  },
  {
    path: '/help',
    name: 'Help',
    component: Help
  },
  {
    path: '/admin',
    name: 'Admin',
    component: Admin
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

export default router
