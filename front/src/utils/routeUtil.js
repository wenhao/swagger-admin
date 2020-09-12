import { setMenus, initRoutes } from '@/router'
import Vue from 'vue'

Object.assign(Vue.prototype, {
  initDoc(swaggerId, callback) {
    this.setSwaggerId(swaggerId)
    this.get(`/doc/get/${swaggerId}`, {}, resp => {
      this.initMenu(resp.data, callback)
    })
  },
  syncDoc(swaggerId, callback) {
    this.get(`/doc/sync/${swaggerId}`, {}, resp => {
      const data = resp.data
      this.initDoc(data.id, callback)
    })
  },
  initMenu(docInfo, callback) {
    const docModules = docInfo.docModules || []
    const menus = [{
      path: '/',
      component: 'Layout',
      redirect: '/dashboard',
      children: [{
        path: 'dashboard',
        name: 'Dashboard',
        component: 'dashboard/index',
        meta: { title: '首页', showOne: true }
      }]
    }, {
      path: '/setting',
      component: 'Layout',
      children: [{
        path: 'list',
        name: 'SettingList',
        component: 'setting/index',
        meta: { title: '全局配置', showOne: true }
      }]
    }]
    docModules.forEach((row, index) => {
      const parent = {
        path: `doc${index}`,
        component: 'Layout',
        name: `Doc${index}`,
        meta: { title: row.module, api: true }
      }
      // build children
      const itemsMap = this.formatItems(docInfo, row.items)
      const children = []
      for (const path in itemsMap) {
        const childData = itemsMap[path]
        childData.swaggerId = docInfo.swaggerId
        children.push({
          path: path,
          data: childData,
          component: 'doc/index',
          meta: { title: path }
        })
      }
      parent.children = children
      menus.push(parent)
    })
    setMenus(menus)
    const routes = initRoutes(menus)
    callback && callback.call(this, routes)
  },
  formatItems(docInfo, items) {
    const map = {}
    items.forEach(row => {
      let arr = map[row.path]
      if (!arr) {
        arr = []
        map[row.path] = arr
      }
      arr.push(row)
    })
    return map
  }
})

