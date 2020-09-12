import Vue from 'vue'
import Router from 'vue-router'

Vue.use(Router)

/* Layout */
import Layout from '@/layout'
const _import = require('@/router/_import_' + process.env.NODE_ENV)

const menuKey = 'swagger-admin-menus'

/**
 * Note: sub-menus only appear when route children.length >= 1
 * Detail see: https://panjiachen.github.io/vue-element-admin-site/guide/essentials/router-and-nav.html
 *
 * hidden: true                   if set true, item will not show in the sidebar(default is false)
 * alwaysShow: true               if set true, will always show the root menus
 *                                if not set alwaysShow, when item has more than one children route,
 *                                it will becomes nested mode, otherwise not show the root menus
 * redirect: noRedirect           if set noRedirect will no redirect in the breadcrumb
 * name:'router-name'             the name is used by <keep-alive> (must set!!!)
 * meta : {
    roles: ['admin','editor']    control the page roles (you can set multiple roles)
    title: 'title'               the name show in sidebar and breadcrumb (recommend set)
    icon: 'svg-name'             the icon show in the sidebar
    breadcrumb: false            if set false, the item will hidden in breadcrumb(default is true)
    activeMenu: '/example/list'  if set path, the sidebar will highlight the path you set
  }
 */

/**
 * constantRoutes
 * a base page that does not have permission requirements
 * all roles can be accessed
 */
export const constantRoutes = [
  {
    path: '/',
    component: Layout,
    redirect: '/dashboard',
    children: [{
      path: 'dashboard',
      name: 'Dashboard',
      component: () => import('@/views/dashboard/index'),
      meta: { title: '首页' }
    }]
  },
  {
    path: '/404',
    component: () => import('@/views/404'),
    hidden: true
  }
  //
  // {
  //   path: '/service',
  //   component: Layout,
  //   name: 'Service',
  //   meta: { title: '服务管理', icon: 'example' },
  //   children: [
  //     {
  //       path: 'list',
  //       name: 'ServiceList',
  //       component: () => import('@/views/service/serviceList'),
  //       meta: { title: '服务列表' }
  //     },
  //     {
  //       path: 'route',
  //       name: 'Route',
  //       component: () => import('@/views/service/route'),
  //       meta: { title: '路由管理' }
  //     },
  //     {
  //       path: 'monitor',
  //       name: 'Monitor',
  //       component: () => import('@/views/service/monitor'),
  //       meta: { title: '路由监控' }
  //     },
  //     {
  //       path: 'limit',
  //       name: 'Limit',
  //       component: () => import('@/views/service/limit'),
  //       meta: { title: '限流管理' }
  //     },
  //     {
  //       path: 'blacklist',
  //       name: 'Blacklist',
  //       component: () => import('@/views/service/ipBlacklist'),
  //       meta: { title: 'IP黑名单' }
  //     }
  //   ]
  // },
  //
  // {
  //   path: '/isv',
  //   component: Layout,
  //   name: 'Isv',
  //   meta: { title: 'ISV管理', icon: 'user' },
  //   children: [
  //     {
  //       path: 'list',
  //       name: 'IsvList',
  //       component: () => import('@/views/isv/index'),
  //       meta: { title: 'ISV列表' }
  //     },
  //     {
  //       path: 'role',
  //       name: 'Role',
  //       component: () => import('@/views/isv/role'),
  //       meta: { title: '角色管理' }
  //     },
  //     {
  //       path: 'keys',
  //       name: 'Keys',
  //       component: () => import('@/views/isv/keys'),
  //       hidden: true,
  //       meta: { title: '秘钥管理' }
  //     }
  //   ]
  // },
  // // 404 page must be placed at the end !!!
  // { path: '*', redirect: '/404', hidden: true }
]

const createRouter = () => new Router({
  // mode: 'history', // require service support
  scrollBehavior: () => ({ y: 0 }),
  routes: constantRoutes
})

const router = createRouter()
let init = false

router.beforeEach((to, from, next) => {
  if (!init) {
    const menus = getMenus()
    if (menus) {
      init = true
      initRoutes(menus)
      next({ ...to, replace: true })
    } else {
      next()
    }
  } else {
    next()
  }
})

function storeItems(menus) {
  const itemsMap = {}
  menus.forEach(row => {
    if (row.path !== '/' && row.children) {
      row.children.forEach(child => {
        itemsMap[child.path] = child.data
      })
    }
  })
  global.itemsMap = itemsMap
}

// Detail see: https://github.com/vuejs/vue-router/issues/1234#issuecomment-357941465
export function resetRouter() {
  const newRouter = createRouter()
  router.matcher = newRouter.matcher // reset router
}

export function setMenus(menus) {
  localStorage.setItem(menuKey, JSON.stringify(menus))
}

function getMenus() {
  const menus = localStorage.getItem(menuKey)
  return menus ? JSON.parse(menus) : null
}

export function removeMenus() {
  resetRouter()
  localStorage.removeItem(menuKey)
}

export function initRoutes(menus) {
  storeItems(menus)
  // 重置路由
  resetRouter()
  const routes = buildRouters(menus)
  // 动态添加路由
  router.addRoutes(routes)
  // 将路由数据传递给全局变量，做侧边栏菜单渲染工作
  global.antRouter = routes
  return routes
}

export function buildRouters(menus) {
  return menus.filter(route => {
    if (route.component) {
      // Layout组件特殊处理
      if (route.component) {
        if (route.component === 'Layout') {
          route.component = Layout
        } else {
          route.component = _import(route.component)
        }
      }
    }
    if (route.children && route.children.length) {
      route.children = buildRouters(route.children)
    }
    return true
  })
}

export default router
