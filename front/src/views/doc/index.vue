<template>
  <div class="doc-container">
    <el-tabs type="card">
      <el-tab-pane>
        <span slot="label"><i class="el-icon-document"></i> 接口信息</span>
        <el-tabs tab-position="left">
          <el-tab-pane v-for="item in items" :key="item.id" :label="item.method" style="margin-left: 10px;">
            <h2 v-if="item.summary && item.summary.length > 0">{{ item.summary }}</h2>
            <div>
              <el-input v-model="item.path" :readonly="true" size="mini" style="width: 500px;">
                <template slot="prepend">{{ item.method }}</template>
              </el-input>
            </div>
            <div class="consumes-produces">
              <span class="label">consumes:</span><span class="text">{{ (item.consumes || []).join(', ') }}</span>
            </div>
            <div class="consumes-produces">
              <span class="label">produces:</span><span class="text">{{ (item.produces || []).join(', ') }}</span>
            </div>
            <h3>接口描述</h3>
            <div class="api-description">{{ item.description }}</div>
            <div v-show="formatHeaders(item.requestParameters).length > 0">
              <h3>请求Header</h3>
              <parameter-table :data="formatHeaders(item.requestParameters)" />
            </div>
            <h3>请求参数</h3>
            <parameter-table :data="formatParameters(item.requestParameters)" />
            <h3>返回参数</h3>
            <parameter-table :data="formatParameters(item.responseParameters)" />
            <h3>返回示例</h3>
            <pre class="normal-text">{{ JSON.stringify(createResponseExample(item.responseParameters), null, 4) }}</pre>
          </el-tab-pane>
        </el-tabs>
      </el-tab-pane>
      <el-tab-pane>
        <span slot="label"><i class="el-icon-s-promotion"></i> 调试接口</span>
        <docdebug :items="items" />
      </el-tab-pane>
    </el-tabs>
  </div>
</template>
<style>
  .doc-container {padding: 10px;}
  .api-description {color: #606266;font-size: 13px;}
  .consumes-produces {margin-top: 10px;}
  .consumes-produces span {margin-right: 10px;}
  .consumes-produces .label {}
  .consumes-produces .text {color: #606266;}

</style>
<script>
import Docdebug from '@/components/Docdebug'
import ParameterTable from '@/components/ParameterTable'

export default {
  components: { Docdebug, ParameterTable },
  data() {
    return {
      items: [],
      requestUrl: '',
      currentMethod: ''
    }
  },
  created() {
    const path = this.$route.path
    this.items = global.itemsMap[path]
    const first = this.items[0]
    this.currentMethod = first.method
    this.requestUrl = first.path
  },
  methods: {
    formatParameters(params) {
      return params.filter(row => {
        return row.in !== 'header'
      })
    },
    formatHeaders(params) {
      return params.filter(row => {
        return row.in === 'header'
      })
    }
  }
}
</script>
