import { Message } from 'element-ui'
import store from '../store'
import { BASE_URL } from './request'

/**
 * 下载导出文件。
 *
 * <p>不用 axios 实例：它会把响应当 JSON 解析并统一提示，二进制流走不通。
 * 这里用原生 fetch + blob，文件名从 Content-Disposition 里取。</p>
 *
 * @param {string} module 模块名，与后端 /api/export/{module} 对应
 * @param {object} params 与列表页相同的筛选/排序参数
 */
export function downloadExport (module, params) {
  const qs = new URLSearchParams()
  Object.keys(params || {}).forEach(k => {
    const v = params[k]
    if (v !== '' && v !== null && v !== undefined) qs.append(k, v)
  })
  const url = `${BASE_URL}/export/${module}${qs.toString() ? '?' + qs.toString() : ''}`

  return fetch(url, {
    method: 'GET',
    headers: { Authorization: 'Bearer ' + store.state.token }
  })
    .then(res => {
      if (!res.ok) throw new Error('导出失败（HTTP ' + res.status + '）')
      const disposition = res.headers.get('Content-Disposition') || ''
      let fileName = `${module}.xlsx`
      const match = /filename\*=UTF-8''([^;]+)/i.exec(disposition) || /filename="?([^";]+)"?/i.exec(disposition)
      if (match && match[1]) {
        try {
          fileName = decodeURIComponent(match[1])
        } catch (e) {
          fileName = match[1]
        }
      }
      return res.blob().then(blob => ({ blob, fileName }))
    })
    .then(({ blob, fileName }) => {
      const link = document.createElement('a')
      const objectUrl = window.URL.createObjectURL(blob)
      link.href = objectUrl
      link.download = fileName
      document.body.appendChild(link)
      link.click()
      document.body.removeChild(link)
      window.URL.revokeObjectURL(objectUrl)
      Message({ message: '导出成功', type: 'success', duration: 2000 })
    })
    .catch(err => {
      Message({ message: (err && err.message) || '导出失败', type: 'error', duration: 3000 })
    })
}
