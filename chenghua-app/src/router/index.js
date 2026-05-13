import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/',
    redirect: '/guide'
  },
  {
    path: '/guide',
    name: 'Guide',
    component: () => import('../views/Guide.vue')
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/Login.vue')
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('../views/Register.vue')
  },
  {
    path: '/home',
    name: 'Home',
    component: () => import('../views/Home.vue'),
    meta: { showTab: true }
  },
  {
    path: '/history',
    name: 'History',
    component: () => import('../views/History.vue'),
    meta: { showTab: true }
  },
  {
    path: '/learning',
    name: 'Learning',
    component: () => import('../views/Learning.vue'),
    meta: { showTab: true }
  },
  {
    path: '/diy',
    name: 'DIY',
    component: () => import('../views/DIY.vue'),
    meta: { showTab: true }
  },
  {
    path: '/experience',
    name: 'Experience',
    component: () => import('../views/Experience.vue'),
    meta: { showTab: false }
  },
  {
    path: '/market',
    name: 'Market',
    component: () => import('../views/Market.vue'),
    meta: { showTab: true }
  },
  {
    path: '/community',
    name: 'Community',
    component: () => import('../views/Community.vue'),
    meta: { showTab: true }
  },
  {
    path: '/ai',
    name: 'AI',
    component: () => import('../views/AI.vue'),
    meta: { showTab: true }
  },
  {
    path: '/mine',
    name: 'Mine',
    component: () => import('../views/Mine.vue'),
    meta: { showTab: true }
  },
  {
    path: '/address',
    name: 'AddressList',
    component: () => import('../views/AddressList.vue')
  },
  {
    path: '/address/edit',
    name: 'AddressEdit',
    component: () => import('../views/AddressEdit.vue')
  },
  {
    path: '/orders',
    name: 'OrderList',
    component: () => import('../views/OrderList.vue')
  },
  {
    path: '/my-works',
    name: 'MyWorks',
    component: () => import('../views/MyWorks.vue')
  },
  {
    path: '/profile/edit',
    name: 'ProfileEdit',
    component: () => import('../views/ProfileEdit.vue')
  },
  {
    path: '/learning-progress',
    name: 'LearningProgress',
    component: () => import('../views/LearningProgress.vue')
  },
  {
    path: '/security',
    name: 'SecurityCenter',
    component: () => import('../views/SecurityCenter.vue')
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router
