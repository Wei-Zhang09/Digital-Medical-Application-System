import request from '../../utils/request'

/** 操作日志分页列表（管理员） */
export function getLogList (pageNum, pageSize, keyword) {
  return request.get(`/logs/${pageNum}/${pageSize}`, {
    params: keyword ? { keyword } : {}
  })
}
