import Layout from '../layout/index.vue'
import NotFound from '../views/error/404.vue'

/**
 * 把后端 /api/permissions 返回的菜单树翻译成 vue-router 路由。
 *
 * 规则：
 * - component = Layout 的根节点 → 挂 Layout，children 逐个转成子路由
 * - component = 404 的根节点 → 通配路由
 * - 其余节点 → 按组件名到 views/<组件名>/index.vue 取组件
 *
 * 用 require.context 预扫描，webpack 能把所有页面打进包里，避免动态 import 路径猜错。
 */
const viewContext = require.context('../views', true, /index\.vue$/)

function loadView (component) {
  const key = `./${component}/index.vue`
  if (viewContext.keys().includes(key)) {
    return viewContext(key).default
  }
  // 组件还没写（比如某个模块前端尚未完成）时给个提示页，不要让路由直接崩
  return {
    name: 'Missing' + component,
    render: h => h('div', { style: 'padding:40px;color:#909399' },
      `页面组件 views/${component}/index.vue 尚未实现`)
  }
}

function toRoute (node) {
  return {
    path: node.path,
    name: node.name,
    component: loadView(node.component),
    meta: {
      title: (node.meta && node.meta.title) || node.title,
      icon: (node.meta && node.meta.icon) || ''
    }
  }
}

export function parseRoutes (permissions) {
  const routes = []
  if (!Array.isArray(permissions)) return routes

  permissions.forEach(node => {
    if (node.component === 'Layout') {
      routes.push({
        path: node.path || '/',
        component: Layout,
        redirect: (node.children && node.children.length) ? node.children[0].path : '/home',
        children: (node.children || []).map(toRoute)
      })
    } else if (node.component === '404') {
      routes.push({ path: '*', name: 'NotFound', component: NotFound })
    } else {
      routes.push(toRoute(node))
    }
  })

  // 后端没给通配路由时兜底
  if (!routes.some(r => r.path === '*')) {
    routes.push({ path: '*', name: 'NotFound', component: NotFound })
  }
  return routes
}
