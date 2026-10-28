import { createRouter, createWebHistory } from 'vue-router'
import { TOKEN_KEY } from '@/api/admin'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/login',
      name: 'login',
      component: () => import('@/views/LoginView.vue')
    },
    {
      path: '/',
      component: () => import('@/layouts/AdminLayout.vue'),
      meta: { requiresAuth: true },
      children: [
        { path: '', redirect: '/users' },
        {
          path: 'users',
          name: 'users',
          component: () => import('@/views/UsersView.vue'),
          meta: { title: '注册用户' }
        },
        {
          path: 'domains',
          name: 'domains',
          component: () => import('@/views/DomainsView.vue'),
          meta: { title: '短链域名' }
        }
      ]
    },
    { path: '/:pathMatch(.*)*', redirect: '/users' }
  ]
})

router.beforeEach((to) => {
  const loggedIn = Boolean(localStorage.getItem(TOKEN_KEY))
  if (to.meta.requiresAuth && !loggedIn) {
    return { name: 'login' }
  }
  if (to.name === 'login' && loggedIn) {
    return { name: 'users' }
  }
  return true
})

export default router
