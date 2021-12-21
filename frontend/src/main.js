import Vue from 'vue'
import App from './App.vue'
import router from './router'
import store from './store'
import vuetify from './plugins/vuetify'
import elementui from 'element-ui'
import './plugins/index'
import './assets/style/main.css'
import 'element-ui/lib/theme-chalk/index.css';
import VueClipboard from 'vue-clipboard2';

Vue.config.productionTip = false
Vue.use(elementui);
Vue.use(VueClipboard)

new Vue({
  router,
  store,
  vuetify,
  render: h => h(App)
}).$mount('#app')
