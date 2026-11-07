import Vue from 'vue'
import VueRouter from 'vue-router'
import store from '../store'
import { getPermissions } from '../api/permission'
import { parseRoutes } from './routeParse'

Vue.use(VueRouter)

/**
 * 只有登录页与 404 是静态注册的；业务路由在登录后按 /api/permissions 动态挂载
 * （官方任务 21「动态路由渲染」）。
 */
const routes = [
  { path: '/', redirect: '/user/login' },
  {
    path: '/user/login',
    name: 'Login',
    component: () => import('../views/Login.vue'),
    meta: { title: '登录' }
  },
  {
    path: '/user/register',
    name: 'Register',
    component: () => import('../views/Register.vue'),
    meta: { title: '注册' }
  }
]

const router = new VueRouter({
  mode: 'hash',
  routes
})

router.beforeEach(async (to, from, next) => {
  const token = store.state.token

  // 免登录页面：登录页、注册页。已登录再访问就直接进首页
  if (to.path === '/user/login' || to.path === '/user/register') {
    return token ? next('/home') : next()
  }

  // 未登录：一律回登录页
  if (!token) {
    return next('/user/login')
  }

  // 已登录但路由还没挂：拉菜单 → 挂路由 → 重新进入目标地址
  if (!store.state.routesLoaded) {
    try {
      const res = await getPermissions()
      const permissions = (res.data && res.data.permissions) || []
      store.commit('SET_PERMISSIONS', permissions)
      parseRoutes(permissions).forEach(route => router.addRoute(route))
      store.commit('SET_ROUTES_LOADED', true)
      return next({ ...to, replace: true })
    } catch (e) {
      store.commit('CLEAR_LOGIN')
      return next('/user/login')
    }
  }

  next()
})

router.afterEach(to => {
  document.title = to.meta && to.meta.title
    ? `${to.meta.title} - 慧医数字医疗应用系统`
    : '慧医数字医疗应用系统'
})

export default router
