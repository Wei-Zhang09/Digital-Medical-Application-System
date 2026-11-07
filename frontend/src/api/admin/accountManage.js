import request from '../../utils/request'

/** 账号分页列表（管理员） */
export function getAccountList (pageNum, pageSize, keyword, sort, order) {
  return request.get(`/accounts/${pageNum}/${pageSize}`, {
    params: { keyword, sort, order }
  })
}

/** 新增账号（初始口令 123456） */
export function addAccount (data) {
  return request.post('/accounts', data)
}

/** 修改账号资料与角色 */
export function updateAccount (id, data) {
  return request.put(`/accounts/${id}`, data)
}

/** 重置口令为 123456 */
export function resetAccountPassword (id) {
  return request.put(`/accounts/${id}/password`)
}

/** 删除账号 */
export function deleteAccount (id) {
  return request.delete(`/accounts/${id}`)
}
