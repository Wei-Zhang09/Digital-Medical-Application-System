import request from '../../utils/request'

/** 必备材料：分页查询 / 新增 / 修改 / 删除 */
export function getMaterialList (pageNum, pageSize, title, sort, order) {
  return request.get(`/materials/${pageNum}/${pageSize}`, { params: { title, sort, order } })
}
export function addMaterial (data) {
  return request.post('/materials', data)
}
export function updateMaterial (id, data) {
  return request.put(`/materials/${id}`, data)
}
export function deleteMaterial (id) {
  return request.delete(`/materials/${id}`)
}
