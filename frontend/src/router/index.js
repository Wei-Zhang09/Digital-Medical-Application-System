import Vue from 'vue'
import VueRouter from 'vue-router'

Vue.use(VueRouter)

/**
 * 只注册登录页与 404；登录成功后的业务路由由 routeParse.js 按 /api/permissions 动态挂载。
 * 路由用 hash 模式（官方一致），所以地址形如 http://localhost:9092/#/user/login
 */
const routes = [
  { path: '/', redirect: '/user/login' },
  {
    path: '/user/login',
    name: 'Login',
    component: () => import('../views/Login.vue')
  },
  { path: '*', redirect: '/user/login' }
]

const router = new VueRouter({
  mode: 'hash',
  routes
})

export default router
