import { createRouter, createWebHistory } from 'vue-router'

import { startViewTransition } from 'vue-view-transitions'
import { eventBus } from '@/main.ts'
import { useConfig } from '@/composables/config.ts'

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
          path: '/stars',
          name: 'stars',
          component: () => import('@/views/StarsView.vue'),
        },
        {
          path: '/apikeys',
          name: 'api-keys',
          component: () => import('@/views/developers/ApiKeysPage.vue'),
        },
        {
          path: '/connect/asciinema',
          name: 'connect-asciinema',
          component: () => import('@/views/extra/AsciinemaConnectView.vue'),
        },
        {
          path: '/connect/:ignore',
          redirect: { name: 'connect-asciinema' },
        },

        {
          path: '/search',
          component: () => import('@/views/layouts/SearchLayout.vue'),
          meta: {
            noSpacing: true,
          },
          children: [
            {
              path: '',
              name: 'search-pastes',
              component: () => import('@/views/search/SearchPastes.vue'),
            },
          ],
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
              path: 'pastes',
              name: 'admin-pastes',
              component: () => import('@/views/admin/AdminPastes.vue'),
            },
            {
              path: 'users',
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
          path: '/@:user',
          name: 'user',
          component: () => import('@/views/PublicUserView.vue'),
        },
        {
          path: '/tags/:tag',
          name: 'tag',
          component: () => import('@/views/TagView.vue'),
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

const config = useConfig()

router.beforeResolve(async (to, from) => {
  if (config.value.animations) {
    const viewTransition = startViewTransition(async () => {
      if (
        (to.name === 'paste' ||
          to.name === 'folder' ||
          to.name === 'home' ||
          to.name === 'home-page') &&
        from.name !== undefined
      ) {
        let ev = undefined
        await new Promise<void>((res) => {
          let resolved = false
          ev = () => {
            if (!resolved) res()
            resolved = true
          }
          eventBus.on('pageLoaded', ev)
          setTimeout(ev, 300)
        })
        if (ev) eventBus.off('pageLoaded', ev)
      }
    })
    await viewTransition.captured
  }
})

export default router
