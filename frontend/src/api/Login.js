import request from '../utils/request'

/**
 * 登录。后端用 formLogin 处理，所以必须提交表单（application/x-www-form-urlencoded）。
 */
export function login (username, password) {
  const data = new URLSearchParams()
  data.append('username', username)
  data.append('password', password)
  return request.post('/login', data)
}
