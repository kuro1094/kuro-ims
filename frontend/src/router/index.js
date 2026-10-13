import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '../store/auth'

const routes = [
  { path: '/login', name: 'Login', component: () => import('../views/LoginView.vue'), meta: { public: true } },
  { path: '/', name: 'Home', component: () => import('../views/HomeView.vue') },
  { path: '/admin', name: 'Admin', component: () => import('../views/AdminView.vue') },
  { path: '/:pathMatch(.*)*', redirect: '/' }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to) => {
  const auth = useAuthStore()
  if (!to.meta.public && !auth.isLogin) {
    return '/login'
  }
  if (to.path === '/login' && auth.isLogin) {
    return '/'
  }
})

export default router
