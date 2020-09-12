<template>
  <div class="app-container">
    <h4>全局Header</h4>
    <el-button type="primary" size="mini" style="margin-bottom: 10px" @click="onHeaderAdd">添加Header</el-button>
    <el-table
      :data="headerData"
      border
      :header-cell-style="cellStyleSmall()"
      :cell-style="cellStyleSmall()"
    >
      <el-table-column label="Name" prop="configKey" width="300px" />
      <el-table-column label="Value" prop="configValue" />
      <el-table-column
        label="操作"
        width="150"
      >
        <template slot-scope="scope">
          <el-button type="text" size="mini" @click="onHeaderUpdate(scope.row)">修改</el-button>
          <el-button type="text" size="mini" @click="onHeaderDelete(scope.row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <!--dialog-->
    <el-dialog
      :title="dialogHeaderTitle"
      :visible.sync="dialogHeaderVisible"
      :close-on-click-modal="false"
      @close="resetForm('dialogHeaderForm')"
    >
      <el-form
        ref="dialogHeaderForm"
        :rules="dialogFormRules"
        :model="dialogHeaderFormData"
        label-width="120px"
        size="mini"
      >
        <el-form-item
          prop="configKey"
          label="Name"
        >
          <el-input v-model="dialogHeaderFormData.configKey" placeholder="支持大小写英文、-、下划线" />
        </el-form-item>
        <el-form-item
          prop="configValue"
          label="Value"
        >
          <el-input v-model="dialogHeaderFormData.configValue" placeholder="value" />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="dialogHeaderVisible = false">取 消</el-button>
        <el-button type="primary" @click="onDialogHeaderSave">保 存</el-button>
      </div>
    </el-dialog>
  </div>
</template>
<script>
export default {
  name: 'Setting',
  data() {
    return {
      headerData: [],
      dialogHeaderVisible: false,
      dialogHeaderTitle: '',
      dialogHeaderFormData: {
        id: 0,
        swaggerId: '',
        configKey: '',
        configValue: ''
      },
      dialogFormRules: {
        configKey: [
          { required: true, message: '不能为空', trigger: 'blur' },
          { validator: (rule, value, callback) => {
            if (value && !/^[a-zA-Z0-9\-_]+$/.test(value)) {
              callback(new Error('name不合法'))
            } else {
              callback()
            }
          }, trigger: 'blur' }
        ], configValue: [
          { required: true, message: '不能为空', trigger: 'blur' }
        ]
      }
    }
  },
  created() {
    this.loadHeaders()
  },
  methods: {
    loadHeaders() {
      this.loadGlobalHeaders(resp => {
        this.headerData = resp.data
      })
    },
    onHeaderAdd() {
      this.dialogHeaderTitle = '新增Header'
      this.dialogHeaderVisible = true
      this.dialogHeaderFormData.id = 0
    },
    onHeaderUpdate(row) {
      this.dialogHeaderTitle = '修改Header'
      this.dialogHeaderVisible = true
      this.$nextTick(() => {
        Object.assign(this.dialogHeaderFormData, row)
      })
    },
    onHeaderDelete(row) {
      this.confirm(`确认要删除该记录吗？`, function(done) {
        const data = {
          id: row.id
        }
        this.post('/systemconfig/globalHeader/delete', data, () => {
          done()
          this.tip('删除成功')
          this.loadHeaders()
        })
      })
    },
    onDialogHeaderSave() {
      this.$refs.dialogHeaderForm.validate((valid) => {
        if (valid) {
          const uri = this.dialogHeaderFormData.id ? '/systemconfig/globalHeader/update' : '/systemconfig/globalHeader/add'
          this.dialogHeaderFormData.swaggerId = this.getSwaggerId()
          this.post(uri, this.dialogHeaderFormData, () => {
            this.dialogHeaderVisible = false
            this.loadHeaders()
          })
        }
      })
    }
  }
}
</script>
