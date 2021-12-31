import Vue from 'vue'
import Vuex from 'vuex'

Vue.use(Vuex)

export default new Vuex.Store({
  state: {
    citations: 0
  },
  mutations: {
    incCitations (state) {
      state.citations++
    },
    decCitations (state) {
      state.citations--
    }
  },
  actions: {
  },
  modules: {
  }
})
