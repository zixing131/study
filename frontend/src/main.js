import { createApp } from 'vue'
import { createPinia } from 'pinia'
import App from './App.vue'
import router from './router'

// 本地字体，避免 Google Fonts 跨域
import '@fontsource/nunito/latin-500.css'
import '@fontsource/nunito/latin-700.css'
import '@fontsource/nunito/latin-800.css'
import '@fontsource/nunito/latin-900.css'
import '@fontsource/zcool-kuaile/chinese-simplified-400.css'
import '@fontsource/zcool-kuaile/latin-400.css'

import './styles/main.css'

createApp(App).use(createPinia()).use(router).mount('#app')

