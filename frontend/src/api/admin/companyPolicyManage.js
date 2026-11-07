import request from '../../utils/request'

/** 医药公司政策 */
export function getCompanyPolicyList (pageNum, pageSize, title, sort, order) {
  return request.get(`/companyPolicies/${pageNum}/${pageSize}`, { params: { title, sort, order } })
}
export function addCompanyPolicy (data) {
  return request.post('/companyPolicies', data)
}
export function updateCompanyPolicy (id, data) {
  return request.put(`/companyPolicies/${id}`, data)
}
export function deleteCompanyPolicy (id) {
  return request.delete(`/companyPolicies/${id}`)
}
