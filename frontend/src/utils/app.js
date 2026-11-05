import { Message, MessageBox } from 'element-ui'

/**
 * 统一提示与确认框，页面里不要各写各的。
 */
export default {
  /** 成功提示 */
  success (msg) {
    Message({ message: msg || '操作成功', type: 'success', duration: 2000 })
  },
  /** 失败提示（后端返回的 mess 直接透出） */
  error (msg) {
    Message({ message: msg || '操作失败', type: 'error', duration: 3000 })
  },
  /** 普通提示 */
  info (msg) {
    Message({ message: msg, type: 'info', duration: 2000 })
  },
  /** 二次确认，返回 Promise<boolean> */
  confirm (msg, title) {
    return MessageBox.confirm(msg, title || '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }).then(() => true).catch(() => false)
  }
}
