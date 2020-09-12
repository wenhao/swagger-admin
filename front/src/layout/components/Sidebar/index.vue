<template>
  <div :class="{'has-logo':showLogo}">
    <logo v-if="showLogo" :collapse="isCollapse" />
    <el-scrollbar wrap-class="scrollbar-wrapper">
      <div v-show="getRoutes().length > 0" style="padding: 5px">
        <el-input
          v-model="filterText"
          :clearable="true"
          prefix-icon="el-icon-search"
          size="mini"
          placeholder="搜索，支持路径、名称、描述"
        />
      </div>
      <el-menu
        class="menu-wrapper"
        :default-active="activeMenu"
        :collapse="isCollapse"
        :background-color="variables.menuBg"
        :text-color="variables.menuText"
        :default-openeds="defaultOpeneds"
        :unique-opened="false"
        :active-text-color="variables.menuActiveText"
        :collapse-transition="false"
        mode="vertical"
      >
        <div v-for="(item) in getRoutes()" v-show="!item.hidden" :key="item.path">
          <!-- 只有一个子节点 -->
          <div v-if="hasOneChild(item.children)">
            <router-link :to="resolvePath(item, item.children[0].path)">
              <el-menu-item :index="resolvePath(item, item.children[0].path)">
                <span slot="title">{{ item.children[0].meta.title }}</span>
              </el-menu-item>
            </router-link>
          </div>
          <div v-else>
            <el-submenu :index="item.path">
              <template v-show="!item.hidden" slot="title">
                <i :class="item.icon"></i>
                <span slot="title">{{ item.meta && item.meta.title }}</span>
              </template>
              <router-link v-for="(child) in item.children" v-show="!child.hidden" :key="child.path" :to="child.path">
                <el-menu-item :index="child.path">
                  {{ child.meta.title }}
                </el-menu-item>
              </router-link>
            </el-submenu>
          </div>
        </div>
      </el-menu>
    </el-scrollbar>
  </div>
</template>

<script>
import { mapGetters } from 'vuex'
import Logo from './Logo'
import variables from '@/styles/variables.scss'
import path from 'path'
import { isExternal } from '@/utils/validate'
import FixiOSBug from './FixiOSBug'

export default {
  components: { Logo },
  mixins: [FixiOSBug],
  data() {
    return {
      routes: '',
      filterText: ''
    }
  },
  computed: {
    ...mapGetters([
      'sidebar'
    ]),
    activeMenu() {
      const route = this.$route
      const { meta, path } = route
      // if set path, the sidebar will highlight the path you set
      if (meta.activeMenu) {
        return meta.activeMenu
      }
      return path
    },
    /**
     * 默认展开
     * @returns {[]}
     */
    defaultOpeneds() {
      // const indexArr = []
      // this.routes.forEach(route => {
      //   if (route.children && route.children.length > 0) {
      //     indexArr.push(route.path)
      //   }
      // })
      // return indexArr
      return []
    },
    showLogo() {
      return true
    },
    variables() {
      return variables
    },
    isCollapse() {
      return false
    }
  },
  watch: {
    filterText(searchText) {
      this.getRoutes().forEach(parent => {
        if (parent.meta && parent.meta.api) {
          const children = parent.children || []
          let findChildCount = 0
          children.forEach(child => {
            let find = false
            for (let i = 0; i < child.data.length; i++) {
              const item = child.data[0]
              find = (this.isMatch(item.path, searchText) ||
                this.isMatch(item.description, searchText) ||
                this.isMatch(item.summary, searchText) ||
                this.isMatch(item.module, searchText)
              )
              if (find) {
                findChildCount++
                break
              }
            }
            // 找到了不应该隐藏
            child.hidden = !find
          })
          // 子节点都没找到，父节点也隐藏
          parent.hidden = findChildCount === 0
        }
      })
    }
  },
  methods: {
    getRoutes() {
      return global.antRouter
    },
    isMatch(target, searchText) {
      if (searchText.length === 0) {
        return true
      }
      return target && target.toLowerCase().indexOf(searchText.toLowerCase()) > -1
    },
    hasOneChild(children) {
      if (children && children.length === 1 && children[0].meta.showOne) {
        return true
      }
    },
    hasOneShowingChild(children = [], parent) {
      const showingChildren = children.filter(item => {
        if (item.hidden) {
          return false
        } else {
          // Temp set(will be used if only has one showing child)
          this.onlyOneChild = item
          return true
        }
      })

      // 首页
      if (showingChildren.length === 1 && showingChildren[0].meta.showOne) {
        return true
      }

      // When there is only one child router, the child router is displayed by default
      if (showingChildren.length === 1) {
        return false
      }

      // Show parent if there are no child router to display
      if (showingChildren.length === 0) {
        this.onlyOneChild = { ... parent, path: '', noShowingChildren: true }
        return true
      }

      return false
    },
    resolvePath(item, routePath) {
      if (isExternal(routePath)) {
        return routePath
      }
      const basePath = item.path
      if (isExternal(basePath)) {
        return basePath
      }
      return path.resolve(basePath, routePath)
    },
    resolveSubmenuPath(routePath) {
      return routePath
    }
  }
}
</script>
