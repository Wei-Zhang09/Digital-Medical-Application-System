import request from '../../utils/request'

/** 医生信息：分页 / 下拉数据 / 增删改 / 重置口令 */
export function getDoctorList (pageNum, pageSize, name, levelId, typeId, sort, order) {
  return request.get(`/doctors/${pageNum}/${pageSize}`, { params: { name, levelId, typeId, sort, order } })
}
export function getDoctorLevels () {
  return request.get('/doctors/levels')
}
export function getDoctorTypes () {
  return request.get('/doctors/types')
}
export function addDoctor (data) {
  return request.post('/doctors', data)
}
export function updateDoctor (id, data) {
  return request.put(`/doctors/${id}`, data)
}
export function deleteDoctor (id) {
  return request.delete(`/doctors/${id}`)
}
export function resetDoctorPassword (id) {
  return request.put(`/doctors/pwd/${id}`)
}
