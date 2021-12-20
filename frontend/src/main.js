import Vue from 'vue'
import App from './App.vue'
import router from './router'
import store from './store'
import vuetify from './plugins/vuetify'
import elementui from 'element-ui'
import './plugins/index'
import './assets/style/main.css'
import 'element-ui/lib/theme-chalk/index.css';

Vue.config.productionTip = false
Vue.use(elementui);

new Vue({
  router,
  store,
  vuetify,
  render: h => h(App)
}).$mount('#app')
