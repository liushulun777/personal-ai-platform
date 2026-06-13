import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const routes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/modules/auth/views/LoginView.vue'),
    meta: { title: '登录', requiresAuth: false }
  },
  {
    path: '/',
    component: () => import('@/layouts/MainLayout.vue'),
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/modules/system/views/DashboardView.vue'),
        meta: { title: '仪表盘', requiresAuth: true }
      },
      {
        path: 'system',
        name: 'System',
        redirect: '/system/users',
        children: [
          {
            path: 'users',
            name: 'UserList',
            component: () => import('@/modules/system/views/UserListView.vue'),
            meta: { title: '用户管理', requiresAuth: true }
          },
          {
            path: 'roles',
            name: 'RoleList',
            component: () => import('@/modules/system/views/RoleListView.vue'),
            meta: { title: '角色管理', requiresAuth: true }
          },
          {
            path: 'menus',
            name: 'MenuList',
            component: () => import('@/modules/system/views/MenuListView.vue'),
            meta: { title: '菜单管理', requiresAuth: true }
          }
        ]
      },
      {
        path: 'blog',
        name: 'Blog',
        redirect: '/blog/articles',
        children: [
          {
            path: 'articles',
            name: 'ArticleList',
            component: () => import('@/modules/blog/views/ArticleListView.vue'),
            meta: { title: '文章管理', requiresAuth: true }
          },
          {
            path: 'articles/:id',
            name: 'ArticleEdit',
            component: () => import('@/modules/blog/views/ArticleDetailView.vue'),
            meta: { title: '文章编辑', requiresAuth: true }
          },
          {
            path: 'articles/:id/preview',
            name: 'ArticlePreview',
            component: () => import('@/modules/blog/views/ArticlePreview.vue'),
            meta: { title: '文章预览', requiresAuth: true }
          },
          {
            path: 'categories',
            name: 'CategoryList',
            component: () => import('@/modules/blog/views/CategoryListView.vue'),
            meta: { title: '分类管理', requiresAuth: true }
          },
          {
            path: 'tags',
            name: 'TagList',
            component: () => import('@/modules/blog/views/TagListView.vue'),
            meta: { title: '标签管理', requiresAuth: true }
          }
        ]
      },
      {
        path: 'ai',
        name: 'AI',
        redirect: '/ai/chat',
        children: [
          {
            path: 'chat',
            name: 'AiChat',
            component: () => import('@/modules/ai/views/AiChatView.vue'),
            meta: { title: 'AI 对话', requiresAuth: true }
          },
          {
            path: 'prompts',
            name: 'PromptList',
            component: () => import('@/modules/ai/views/PromptListView.vue'),
            meta: { title: 'Prompt 模板', requiresAuth: true }
          }
        ]
      }
    ]
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('@/components/common/NotFound.vue')
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
router.beforeEach((to, _from, next) => {
  const authStore = useAuthStore()

  if (to.meta.requiresAuth !== false && !authStore.isAuthenticated) {
    next({ name: 'Login', query: { redirect: to.fullPath } })
  } else if (to.name === 'Login' && authStore.isAuthenticated) {
    next({ name: 'Dashboard' })
  } else {
    document.title = `${to.meta.title || ''} - Personal AI Platform`
    next()
  }
})

export default router
