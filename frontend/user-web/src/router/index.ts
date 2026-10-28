import { createRouter, createWebHistory } from 'vue-router'
import { TOKEN_KEY } from '@/api/http'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/',
      name: 'home',
      component: () => import('@/views/HomeView.vue')
    },
    {
      path: '/login',
      name: 'login',
      component: () => import('@/views/LoginView.vue'),
      meta: { guestOnly: true }
    },
    {
      path: '/register',
      name: 'register',
      component: () => import('@/views/RegisterView.vue'),
      meta: { guestOnly: true }
    },
    {
      path: '/console',
      component: () => import('@/layouts/ConsoleLayout.vue'),
      meta: { requiresAuth: true },
      children: [
        { path: '', redirect: '/console/overview' },
        {
          path: 'overview',
          name: 'overview',
          component: () => import('@/views/ConsoleOverview.vue'),
          meta: { title: '概览' }
        },
        {
          path: 'links',
          name: 'links',
          component: () => import('@/views/ConsoleLinks.vue'),
          meta: { title: '短链管理' }
        },
        {
          path: 'stats',
          name: 'stats',
          component: () => import('@/views/ConsoleStats.vue'),
          meta: { title: '数据统计' }
        },
        {
          path: 'profile',
          name: 'profile',
          component: () => import('@/views/ConsoleProfile.vue'),
          meta: { title: '个人中心' }
        }
      ]
    },
    { path: '/:pathMatch(.*)*', redirect: '/' }
  ]
})

router.beforeEach((to) => {
  const loggedIn = Boolean(localStorage.getItem(TOKEN_KEY))
  if (to.meta.requiresAuth && !loggedIn) {
    return { name: 'login', query: { redirect: to.fullPath } }
  }
  if (to.meta.guestOnly && loggedIn) {
    return { name: 'overview' }
  }
  return true
})

export default router
