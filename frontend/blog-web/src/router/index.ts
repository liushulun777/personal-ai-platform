import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/',
      component: () => import('@/layouts/PublicLayout.vue'),
      children: [
        {
          path: '',
          name: 'Home',
          component: () => import('@/views/HomeView.vue'),
          meta: { title: '首页' }
        },
        {
          path: 'article/:id',
          name: 'Article',
          component: () => import('@/views/ArticleView.vue'),
          meta: { title: '文章' }
        },
        {
          path: 'category/:id',
          name: 'Category',
          component: () => import('@/views/CategoryView.vue'),
          meta: { title: '分类' }
        },
        {
          path: 'tag/:id',
          name: 'Tag',
          component: () => import('@/views/TagView.vue'),
          meta: { title: '标签' }
        }
      ]
    }
  ]
})

router.beforeEach((to) => {
  document.title = `${to.meta.title || '博客'} - Personal AI Platform`
})

export default router
