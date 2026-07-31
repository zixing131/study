import { createRouter, createWebHistory } from 'vue-router'
import { getAdminToken } from '../api/http'

const routes = [
  {
    path: '/',
    name: 'home',
    component: () => import('../views/user/HomeView.vue'),
  },
  {
    path: '/chinese/literacy',
    name: 'literacy',
    component: () => import('../views/user/LiteracyView.vue'),
  },
  {
    path: '/chinese/poem',
    name: 'poem-list',
    component: () => import('../views/user/PoemListView.vue'),
  },
  {
    path: '/chinese/poem/:id',
    name: 'poem-detail',
    component: () => import('../views/user/PoemDetailView.vue'),
  },
  {
    path: '/math/counting',
    name: 'counting',
    component: () => import('../views/user/CountingView.vue'),
  },
  {
    path: '/math/arithmetic',
    name: 'arithmetic',
    component: () => import('../views/user/ArithmeticView.vue'),
  },
  {
    path: '/english/letters',
    name: 'letters',
    component: () => import('../views/user/EnglishLettersView.vue'),
  },
  {
    path: '/english/words',
    name: 'words',
    component: () => import('../views/user/EnglishWordsView.vue'),
  },
  {
    path: '/admin/login',
    name: 'admin-login',
    component: () => import('../views/admin/AdminLogin.vue'),
  },
  {
    path: '/admin',
    component: () => import('../views/admin/AdminLayout.vue'),
    meta: { admin: true },
    children: [
      { path: '', redirect: '/admin/characters' },
      {
        path: 'characters',
        name: 'admin-characters',
        component: () => import('../views/admin/AdminCharacters.vue'),
      },
      {
        path: 'poems',
        name: 'admin-poems',
        component: () => import('../views/admin/AdminPoems.vue'),
      },
      {
        path: 'english',
        name: 'admin-english',
        component: () => import('../views/admin/AdminEnglish.vue'),
      },
    ],
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

router.beforeEach((to) => {
  if (to.matched.some((r) => r.meta.admin) && !getAdminToken()) {
    return '/admin/login'
  }
  return true
})

export default router
