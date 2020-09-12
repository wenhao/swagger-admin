<template>
  <div class="navbar">
    <div class="project-select">
      <el-select v-model="swaggerId" placeholder="选择项目" size="medium" @change="onProjectSelect">
        <div class="swagger-select-panel">
          <ul v-for="item in options" :key="item.id" class="el-select-group__wrap">
            <li class="el-select-group__title">
              <span style="float: left;margin-right: 20px;">{{ item.name || item.host }} </span>
              <span style="float: right; color: #8492a6; font-size: 13px;padding-right: 20px;">
                <el-link type="primary" icon="el-icon-edit" style="margin-right: 20px;" @click.stop="onProjectUpdate(item)"></el-link>
                <el-link type="danger" icon="el-icon-delete" @click.stop="onProjectDelete(item)"></el-link>
              </span>
            </li>
            <el-option
              v-for="group in item.groups"
              :key="group.id"
              :label="group.name"
              :value="group.id"
            >
            </el-option>
          </ul>
        </div>
      </el-select>
      <el-button type="text" @click="onProjectAdd">添加项目</el-button>
      <div v-show="options.length > 0" class="right-menu">
        <el-tooltip placement="left" content="同步远程文档">
          <el-button type="primary" icon="el-icon-refresh" size="mini" style="margin-right: 10px" @click="refreshDoc"></el-button>
        </el-tooltip>
      </div>
    </div>
    <el-dialog
      :title="projectTitle"
      :visible.sync="projectDlgShow"
    >
      <el-form
        ref="projectForm"
        :model="projectFormData"
        :rules="projectRule"
        size="mini"
        label-width="150px"
      >
        <el-form-item label="项目地址" prop="host">
          <el-input
            v-model="projectFormData.host"
            placeholder="到contextPath，如：http://localhost:8080，http://localhost:8081/cms"
            show-word-limit
            maxlength="100"
          />
        </el-form-item>
        <el-form-item label="项目名称">
          <el-input v-model="projectFormData.name" placeholder="选填" show-word-limit maxlength="100" />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="projectDlgShow = false">取 消</el-button>
        <el-button type="primary" @click="onProjectSave">保 存</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { mapGetters } from 'vuex'
export default {
  data() {
    return {
      options: [],
      currentSwaggerId: '',
      // add project
      projectTitle: '添加项目',
      projectDlgShow: false,
      projectFormData: {
        id: 0,
        name: '',
        host: ''
      },
      dbTypeConfig: [],
      projectRule: {
        host: [
          { required: true, message: '不能为空', trigger: 'blur' }
        ]
      }
    }
  },
  computed: {
    ...mapGetters([
      'sidebar',
      'avatar'
    ]),
    swaggerId: {
      get() {
        return (this.currentSwaggerId || this.getSwaggerId()) || ''
      },
      set(val) {
      }
    }
  },
  created() {
    this.loadProjectInfo()
  },
  methods: {
    toggleSideBar() {
      this.$store.dispatch('app/toggleSideBar')
    },
    loadProjectInfo() {
      this.get('/project/list/all', {}, resp => {
        this.options = resp.data
        if (this.options.length === 0) {
          this.onProjectAdd()
        }
      })
    },
    onProjectSelect(swaggerId) {
      this.currentSwaggerId = swaggerId
      this.initDoc(swaggerId, () => {
        this.reloadHome()
      })
    },
    onProjectAdd() {
      this.projectTitle = '添加项目'
      this.projectFormData.id = 0
      this.projectDlgShow = true
    },
    onProjectUpdate(item) {
      this.projectTitle = '修改项目'
      Object.assign(this.projectFormData, item)
      this.projectDlgShow = true
    },
    onProjectDelete(item) {
      this.confirm(`确认要删除 ${item.host} 吗？`, function(done) {
        const data = {
          id: item.id
        }
        this.post('/project/delete', data, function() {
          done()
          this.reloadHome()
        })
      })
    },
    refreshDoc() {
      this.syncDoc(this.swaggerId, () => {
        this.alert('同步成功', '提示', () => {
          this.reloadHome()
        })
      })
    },
    reloadHome() {
      location.href = '/'
    },
    onProjectSave() {
      this.$refs.projectForm.validate((valid) => {
        if (valid) {
          if (this.projectFormData.id) {
            this.post('/project/update', this.projectFormData, resp => {
              this.projectDlgShow = false
            })
          } else {
            this.post('/project/add', this.projectFormData, resp => {
              this.tip('添加成功')
              this.loadProjectInfo()
              this.projectDlgShow = false
            })
          }
        }
      })
    }
  }
}
</script>

<style lang="scss" scoped>
  .swagger-select-panel .el-select-group__wrap {list-style: initial;}
  .navbar-div {float: right;margin-top: 5px;margin-right: 10px;}
.user-head {
  cursor: pointer;
  margin-top: 6px;margin-right: 10px;
}
.navbar {
  height: 50px;
  overflow: hidden;
  position: relative;
  background: #fff;
  box-shadow: 0 1px 4px rgba(0,21,41,.08);
  .project-select {
    padding: 5px;
  }

  .hamburger-container {
    line-height: 46px;
    height: 100%;
    float: left;
    cursor: pointer;
    transition: background .3s;
    -webkit-tap-highlight-color:transparent;

    &:hover {
      background: rgba(0, 0, 0, .025)
    }
  }

  .breadcrumb-container {
    float: left;
  }

  .right-menu {
    float: right;
    height: 100%;
    line-height: 40px;

    &:focus {
      outline: none;
    }

    .right-menu-item {
      display: inline-block;
      padding: 0 8px;
      height: 100%;
      font-size: 18px;
      color: #5a5e66;
      vertical-align: text-bottom;

      &.hover-effect {
        cursor: pointer;
        transition: background .3s;

        &:hover {
          background: rgba(0, 0, 0, .025)
        }
      }
    }

    .avatar-container {

      .avatar-wrapper {
        margin-top: 5px;
        position: relative;

        .user-avatar {
          cursor: pointer;
          width: 40px;
          height: 40px;
          border-radius: 10px;
        }

        .el-icon-caret-bottom {
          cursor: pointer;
          position: absolute;
          right: -20px;
          top: 25px;
          font-size: 12px;
        }
      }
    }
  }
}
</style>
