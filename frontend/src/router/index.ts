import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/auth',
      name: 'auth',
      component: () => import('@/views/auth/LoginViaAPI.vue'),
    },
    {
      path: '/:paste/embed',
      name: 'paste-embed',
      component: () => import('@/views/PasteEmbed.vue'),
    },
    {
      path: '/',
      component: () => import('@/views/layouts/MainLayout.vue'),
      children: [
        {
          path: '/',
          name: 'home',
          component: () => import('@/views/HomeView.vue'),
        },
        {
          path: '/home',
          name: 'home-page',
          component: () => import('@/views/HomeView.vue'),
        },
        {
          path: '/explore',
          name: 'explore',
          component: () => import('@/views/ExploreView.vue'),
        },
        {
          path: '/apikeys',
          name: 'api-keys',
          component: () => import('@/views/developers/ApiKeysPage.vue'),
        },

        {
          path: '/admin',
          component: () => import('@/views/layouts/AdminLayout.vue'),
          children: [
            {
              path: '',
              name: 'admin-home',
              component: () => import('@/views/admin/AdminHome.vue'),
            },
            {
              path: '/pastes',
              name: 'admin-pastes',
              component: () => import('@/views/admin/AdminPastes.vue'),
            },
            {
              path: '/users',
              name: 'admin-users',
              component: () => import('@/views/admin/AdminUsers.vue'),
            },
          ],
        },

        {
          path: '/folder/:folder',
          name: 'folder',
          component: () => import('@/views/FolderView.vue'),
        },
        {
          path: '/:paste',
          name: 'paste',
          component: () => import('@/views/PasteView.vue'),
        },
        {
          path: ':path(.*)',
          name: 'not-found',
          component: () => import('@/views/NotFound.vue'),
        },
      ],
    },
  ],
})

export default router
