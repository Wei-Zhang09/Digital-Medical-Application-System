import Vue from 'vue'
import Vuex from 'vuex'

Vue.use(Vuex)

/**
 * 全局状态：登录信息 + 菜单 + 路由是否已挂载。
 *
 * token/role 落 localStorage，刷新页面不丢；permissions 只在内存里，刷新后由路由守卫重新拉取。
 */
export default new Vuex.Store({
  state: {
    token: localStorage.getItem('token') || '',
    role: localStorage.getItem('role') || '',
    user: JSON.parse(localStorage.getItem('user') || 'null'),
    permissions: [],
    routesLoaded: false
  },
  mutations: {
    SET_LOGIN (state, payload) {
      state.token = payload.token
      state.role = payload.role
      state.user = payload.user
      localStorage.setItem('token', payload.token)
      localStorage.setItem('role', payload.role)
      localStorage.setItem('user', JSON.stringify(payload.user))
    },
    SET_PERMISSIONS (state, permissions) {
      state.permissions = permissions
    },
    SET_ROUTES_LOADED (state, loaded) {
      state.routesLoaded = loaded
    },
    CLEAR_LOGIN (state) {
      state.token = ''
      state.role = ''
      state.user = null
      state.permissions = []
      state.routesLoaded = false
      localStorage.removeItem('token')
      localStorage.removeItem('role')
      localStorage.removeItem('user')
    }
  },
  actions: {},
  modules: {}
})
