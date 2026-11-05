import request from '../../utils/request'

/** 医保政策 */
export function getMedicalPolicyList (pageNum, pageSize, title) {
  return request.get(`/medicalPolicies/${pageNum}/${pageSize}`, { params: { title } })
}
export function addMedicalPolicy (data) {
  return request.post('/medicalPolicies', data)
}
export function updateMedicalPolicy (id, data) {
  return request.put(`/medicalPolicies/${id}`, data)
}
export function deleteMedicalPolicy (id) {
  return request.delete(`/medicalPolicies/${id}`)
}
