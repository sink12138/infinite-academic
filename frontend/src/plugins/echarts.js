import Vue from 'vue'
import * as echarts from 'echarts'
import theme from '../assets/style/infinite.json'

echarts.registerTheme('infinite', theme)
Vue.prototype.$echarts = echarts