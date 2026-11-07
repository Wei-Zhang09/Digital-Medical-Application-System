import request from '../../utils/request'

/** 医药公司：分页查询 / 全部 / 新增 / 修改 / 删除 */
export function getCompanyList (pageNum, pageSize, name, sort, order) {
  return request.get(`/companies/${pageNum}/${pageSize}`, { params: { name, sort, order } })
}
export function getAllCompany () {
  return request.get('/companies/all')
}
export function addCompany (data) {
  return request.post('/companies', data)
}
export function updateCompany (companyId, data) {
  return request.put(`/companies/${companyId}`, data)
}
export function deleteCompany (companyId) {
  return request.delete(`/companies/${companyId}`)
}
