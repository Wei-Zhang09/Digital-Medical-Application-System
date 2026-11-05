import axios from 'axios'
import { Message } from 'element-ui'
import router from '../router'
import store from '../store'

/**
 * axios 实例。
 *
 * - 开发态直连后端 8080（部署时把 baseURL 改成 "/api"，由 Nginx 反代）
 * - 请求拦截：自动带上 Authorization: Bearer <token>
 * - 响应拦截：统一判断 code；10006（未登录/token 失效）清本地状态回登录页
 */
export const BASE_URL = 'http://localhost:8080/api'

const service = axios.create({
  baseURL: BASE_URL,
  timeout: 15000
})

service.interceptors.request.use(
  config => {
    const token = store.state.token
    if (token) {
      config.headers['Authorization'] = 'Bearer ' + token
    }
    return config
  },
  error => Promise.reject(error)
)

service.interceptors.response.use(
  response => {
    const res = response.data
    // 图片上传等场景后端也可能直接返回 Msg
    if (res && typeof res.code !== 'undefined') {
      if (res.code === 20000) {
        return res
      }
      if (res.code === 10006) {
        // token 失效：清状态并回登录页
        store.commit('CLEAR_LOGIN')
        if (router.currentRoute.path !== '/user/login') {
          router.replace('/user/login')
        }
        Message({ message: res.mess || '登录已过期，请重新登录', type: 'warning' })
        return Promise.reject(res)
      }
      Message({ message: res.mess || '操作失败', type: 'error' })
      return Promise.reject(res)
    }
    return res
  },
  error => {
    const status = error.response && error.response.status
    let msg = '网络异常，请稍后重试'
    if (status === 401) {
      msg = '未登录或登录已过期'
      store.commit('CLEAR_LOGIN')
      router.replace('/user/login')
    } else if (status === 403) {
      msg = '权限不足'
    } else if (status === 404) {
      msg = '接口不存在'
    } else if (error.response && error.response.data && error.response.data.mess) {
      msg = error.response.data.mess
    }
    Message({ message: msg, type: 'error' })
    return Promise.reject(error)
  }
)

export default service
