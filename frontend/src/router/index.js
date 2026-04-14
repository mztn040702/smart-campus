import { createRouter, createWebHistory } from 'vue-router'
import Login from '../views/Login.vue'
import Register from '../views/Register.vue'
import Home from '../views/Home.vue'
import Chat from '../views/Chat.vue'
import Product from '../views/Product.vue'
import Job from '../views/Job.vue'
import Help from '../views/Help.vue'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: Login
  },
  {
    path: '/register',
    name: 'Register',
    component: Register
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
    path: '/',
    redirect: '/home'
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router

