import request from '../../utils/request'

/** 城市：分页查询 / 全部 / 新增 / 删除（官方需求不含修改） */
export function getCityList (pageNum, pageSize, cityNumber, sort, order) {
  return request.get(`/cities/${pageNum}/${pageSize}`, { params: { cityNumber, sort, order } })
}
export function getAllCity () {
  return request.get('/cities/all')
}
export function addCity (data) {
  return request.post('/cities', data)
}
export function deleteCity (cityId) {
  return request.delete(`/cities/${cityId}`)
}
