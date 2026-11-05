import Vue from 'vue'
import Vuex from 'vuex'

Vue.use(Vuex)

export default new Vuex.Store({
  state: {
    token: localStorage.getItem('token') || '',
    role: localStorage.getItem('role') || '',
    userInfo: {}
  },
  mutations: {
    SET_TOKEN (state, token) {
      state.token = token
      localStorage.setItem('token', token)
    },
    SET_ROLE (state, role) {
      state.role = role
      localStorage.setItem('role', role)
    },
    CLEAR_LOGIN (state) {
      state.token = ''
      state.role = ''
      state.userInfo = {}
      localStorage.removeItem('token')
      localStorage.removeItem('role')
    }
  },
  actions: {},
  modules: {}
})
