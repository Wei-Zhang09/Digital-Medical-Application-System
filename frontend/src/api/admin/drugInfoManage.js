import request from '../../utils/request'

/** 药品信息：分页 / 详情 / 增删改 / 图片上传 */
export function getDrugList (pageNum, pageSize, name) {
  return request.get(`/drugs/${pageNum}/${pageSize}`, { params: { name } })
}
export function getDrugDetail (drugId) {
  return request.get(`/drugs/${drugId}`)
}
export function addDrug (data) {
  return request.post('/drugs', data)
}
export function updateDrug (drugId, data) {
  return request.put(`/drugs/${drugId}`, data)
}
export function deleteDrug (drugId) {
  return request.delete(`/drugs/${drugId}`)
}
/** 上传图片，返回 { code, data: { url } } */
export function uploadDrugImage (file) {
  const form = new FormData()
  form.append('file', file)
  return request.post('/upload', form, { headers: { 'Content-Type': 'multipart/form-data' } })
}
