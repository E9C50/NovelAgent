import { createRouter, createWebHistory } from 'vue-router'
import Home from '@/views/Home.vue'
import NovelDetail from '@/views/NovelDetail.vue'
import CharacterManagement from '@/views/CharacterManagement.vue'
import ChapterManagement from '@/views/ChapterManagement.vue'
import ChapterEditor from '@/views/ChapterEditor.vue'

const routes = [
  {
    path: '/',
    name: 'Home',
    component: Home,
    meta: {
      title: '小说管理'
    }
  },
  {
    path: '/novel/:id',
    name: 'NovelDetail',
    component: NovelDetail,
    meta: {
      title: '小说详情'
    }
  },
  {
    path: '/novel/:id/characters',
    name: 'CharacterManagement',
    component: CharacterManagement,
    meta: {
      title: '角色管理'
    }
  },
  {
    path: '/novel/:id/chapters',
    name: 'ChapterManagement',
    component: ChapterManagement,
    meta: {
      title: '章节管理'
    }
  },
  {
    path: '/novel/:id/chapter/:chapterId',
    name: 'ChapterEditor',
    component: ChapterEditor,
    meta: {
      title: '章节编辑'
    }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  document.title = to.meta.title ? `${to.meta.title} - AI小说写作平台` : 'AI小说写作平台'
  next()
})

export default router
