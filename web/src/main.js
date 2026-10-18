import { createApp } from 'vue'

import App from './App.vue'
import router from './router'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import zhCn from 'element-plus/es/locale/lang/zh-cn'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'

// 引入 TDesign
import TDesign from 'tdesign-vue-next'
import TDesignChat from '@tdesign-vue-next/chat'
import 'tdesign-vue-next/es/style/index.css'

import './assets/main.css'

const app = createApp(App)

app.use(router)
app.use(ElementPlus, {locale: zhCn})
app.use(TDesign).use(TDesignChat)
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

app.mount('#app')