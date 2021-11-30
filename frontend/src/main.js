import Vue from 'vue'
import App from './App.vue'
import router from './router'
import store from './store'
import vuetify from './plugins/vuetify'
import './plugins/index'
import './assets/style/main.css'

import datePicker from 'element-ui/lib/date-picker';
import 'element-ui/lib/theme-chalk/index.css';
Vue.use(datePicker);
Vue.config.productionTip = false

new Vue({
  router,
  store,
  vuetify,
  render: h => h(App)
}).$mount('#app')
