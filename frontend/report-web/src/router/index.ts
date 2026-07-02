import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const routes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/auth/LoginView.vue'),
    meta: { title: '登录', requiresAuth: false }
  },
  {
    path: '/',
    component: () => import('@/components/common/Layout.vue'),
    redirect: '/datasource',
    meta: { requiresAuth: true },
    children: [
      {
        path: 'datasource',
        name: 'DataSource',
        component: () => import('@/views/datasource/index.vue'),
        meta: { title: '数据源管理' }
      },
      {
        path: 'dataset',
        name: 'DataSet',
        component: () => import('@/views/dataset/index.vue'),
        meta: { title: '数据集管理' }
      },
      {
        path: 'report',
        name: 'Report',
        redirect: '/report/list',
        children: [
          {
            path: 'list',
            name: 'ReportList',
            component: () => import('@/views/report/list/index.vue'),
            meta: { title: '报表列表' }
          },
          {
            path: 'designer/:id?',
            name: 'ReportDesigner',
            component: () => import('@/views/report/designer/index.vue'),
            meta: { title: '报表设计器' }
          },
          {
            path: 'preview/:id',
            name: 'ReportPreview',
            component: () => import('@/views/report/preview/index.vue'),
            meta: { title: '报表预览' }
          }
        ]
      },
      {
        path: 'dashboard',
        name: 'Dashboard',
        redirect: '/dashboard/list',
        children: [
          {
            path: 'list',
            name: 'DashboardList',
            component: () => import('@/views/dashboard/list/index.vue'),
            meta: { title: '大屏列表' }
          },
          {
            path: 'designer/:id?',
            name: 'DashboardDesigner',
            component: () => import('@/views/dashboard/designer/index.vue'),
            meta: { title: '大屏设计器' }
          },
          {
            path: 'preview/:id',
            name: 'DashboardPreview',
            component: () => import('@/views/dashboard/preview/index.vue'),
            meta: { title: '大屏预览' }
          }
        ]
      },
      {
        path: 'schedule',
        name: 'Schedule',
        component: () => import('@/views/schedule/index.vue'),
        meta: { title: '调度管理' }
      }
    ]
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    redirect: '/'
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
    next({ name: 'DataSource' })
  } else {
    document.title = `${to.meta.title || ''} - 报表平台`
    next()
  }
})

export default router
