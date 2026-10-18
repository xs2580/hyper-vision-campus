import { createRouter, createWebHistory } from 'vue-router'
import homeview from '@/views/layout/home.vue'
import VR_Mapview from '@/views/layout/VR_Map.vue'
import navigate_view from '@/views/layout/navigate.vue'
import HomeView from '@/views/AI-chat/Homeview.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      redirect: '/home'
    },
    {
      path: '/home',
      name: 'home',
      component: homeview 
    },
    {
      path: '/VR_Map',
      name: 'VR_Map',
      component: VR_Mapview 
    },
    {
      path: '/navigate',
      name: 'navigate',
      component: navigate_view 
    },
    {
      path: '/ai-chat',
      name: 'ai-chat',
      component: HomeView 
    },
   
  ]
})

export default router