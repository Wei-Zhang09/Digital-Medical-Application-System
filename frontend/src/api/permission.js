import request from '../utils/request'

/** 当前登录用户的菜单树 */
export function getPermissions () {
  return request.get('/permissions')
}
