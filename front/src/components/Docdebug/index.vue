<template>
  <div class="doc-debug">
    <el-row :gutter="20">
      <el-col :span="24-rightSpanSize">
        <el-input v-model="requestUrl">
          <el-select slot="prepend" v-model="currentMethod" style="width: 120px;" @change="onMethodChange">
            <el-option v-for="item in items" :key="item.method" :label="item.method" :value="item.method" :content="item"></el-option>
          </el-select>
          <el-button slot="append" style="width: 100px" @click="send"> 发 送 </el-button>
        </el-input>
        <el-collapse v-model="collapseActive" accordion style="margin-top: 10px;">
          <el-collapse-item title="Header" name="header">
            <span slot="title" class="result-header-label">
              <span>Header <span class="param-count">({{ headerData.length + globalHeaderData.length }})</span></span>
            </span>
            <div>
              <h4>全局Header</h4>
              <el-table
                :data="globalHeaderData"
                border
                :header-cell-style="cellStyleSmall()"
                :cell-style="cellStyleSmall()"
              >
                <el-table-column label="Name" prop="configKey" width="300px" />
                <el-table-column label="Value" prop="configValue">
                  <template slot-scope="scope">
                    <el-form :model="scope.row" size="mini">
                      <el-form-item label-width="0">
                        <el-input v-model="scope.row.configValue" />
                      </el-form-item>
                    </el-form>
                  </template>
                </el-table-column>
              </el-table>
            </div>
            <div v-show="headerData.length > 0">
              <h4>接口Header</h4>
              <el-table
                :data="headerData"
                border
                :header-cell-style="cellStyle"
                :cell-style="cellStyle"
              >
                <el-table-column label="Name" prop="name" width="300px" />
                <el-table-column label="Value">
                  <template slot-scope="scope">
                    <el-form :model="scope.row" size="mini">
                      <el-form-item label-width="0">
                        <el-input v-model="scope.row.example" />
                      </el-form-item>
                    </el-form>
                  </template>
                </el-table-column>
              </el-table>
            </div>
          </el-collapse-item>
        </el-collapse>
        <el-tabs v-model="requestActive" type="card" style="margin-top: 10px">
          <el-tab-pane label="Body" name="body">
            <span slot="label" class="result-header-label">
              <el-badge :is-dot="hasBody()" type="danger">
                <span>Body</span>
              </el-badge>
            </span>
            <el-radio-group v-model="postActive" size="small" style="margin-bottom: 10px;">
              <el-radio-button label="json" class="json-badge">json</el-radio-button>
              <el-radio-button label="form">x-www-form-urlencoded <span class="param-count">({{ formData.length }})</span></el-radio-button>
              <el-radio-button label="multipart">multipart <span class="param-count">({{ multipartData.length }})</span></el-radio-button>
            </el-radio-group>
            <div v-show="showBody('json')">
              <el-form>
                <el-form-item label-width="0">
                  <el-input v-model="jsonBody" type="textarea" :autosize="{ minRows: 2, maxRows: 100}" />
                </el-form-item>
              </el-form>
            </div>
            <div v-show="showBody('form')">
              <el-table
                :data="formData"
                border
                :header-cell-style="cellStyle"
                :cell-style="cellStyle"
              >
                <el-table-column
                  prop="name"
                  label="参数名"
                  width="300"
                />
                <el-table-column label="参数值" prop="paramValue">
                  <template slot-scope="scope">
                    <el-form :model="scope.row" size="mini">
                      <el-form-item label-width="0">
                        <div v-if="scope.row.type === 'enum'">
                          <el-select v-model="scope.row.example">
                            <el-option v-for="val in scope.row.enums" :key="val" :value="val" :label="val"></el-option>
                          </el-select>
                        </div>
                        <div v-else>
                          <el-input v-model="scope.row.example" />
                        </div>
                      </el-form-item>
                    </el-form>
                  </template>
                </el-table-column>
              </el-table>
            </div>
            <div v-show="showBody('multipart')">
              <el-upload
                action=""
                :multiple="true"
                :auto-upload="false"
                style="width: 500px;margin-bottom: 10px"
                :on-remove="(file, fileList) => onSelectMultiFile(file, fileList)"
                :on-change="(file, fileList) => onSelectMultiFile(file, fileList)"
              >
                <el-button slot="trigger" type="primary" size="mini">上传多个文件</el-button>
              </el-upload>
              <el-table
                v-show="showBody('multipart')"
                :data="multipartData"
                border
                :header-cell-style="cellStyle"
                :cell-style="cellStyle"
              >
                <el-table-column
                  prop="name"
                  label="参数名"
                  width="300"
                />
                <el-table-column label="参数值">
                  <template slot-scope="scope">
                    <el-form :model="scope.row" size="mini">
                      <el-form-item label-width="0" style="margin-bottom: 0">
                        <el-upload
                          v-if="scope.row.type === 'file'"
                          action=""
                          :multiple="false"
                          :auto-upload="false"
                          :on-change="(file) => onSelectFile(file, scope.row)"
                        >
                          <el-button slot="trigger" class="choose-file" type="primary">选择文件</el-button>
                        </el-upload>
                        <div v-else-if="scope.row.type === 'enum'">
                          <el-select v-model="scope.row.example">
                            <el-option v-for="val in scope.row.enums" :key="val" :value="val" :label="val"></el-option>
                          </el-select>
                        </div>
                        <div v-else>
                          <el-input v-model="scope.row.example" />
                        </div>
                      </el-form-item>
                    </el-form>
                  </template>
                </el-table-column>
              </el-table>
            </div>
          </el-tab-pane>
          <el-tab-pane label="Query" name="query">
            <span slot="label" class="result-header-label">
              <span>Query <span class="param-count">({{ queryData.length }})</span></span>
            </span>
            <el-table
              :data="queryData"
              border
              :header-cell-style="cellStyle"
              :cell-style="cellStyle"
            >
              <el-table-column
                prop="name"
                label="参数名"
                width="300"
              />
              <el-table-column label="参数值">
                <template slot-scope="scope">
                  <el-form :model="scope.row" size="mini">
                    <el-form-item label-width="0">
                      <div v-if="scope.row.type === 'enum'">
                        <el-select v-model="scope.row.example">
                          <el-option v-for="val in scope.row.enums" :key="val" :value="val" :label="val"></el-option>
                        </el-select>
                      </div>
                      <div v-else>
                        <el-input v-model="scope.row.example" />
                      </div>
                    </el-form-item>
                  </el-form>
                </template>
              </el-table-column>
            </el-table>
          </el-tab-pane>
        </el-tabs>
      </el-col>
      <el-col :span="rightSpanSize" style="border-left: 1px #E4E7ED solid;">
        <div class="result-status">
          Status: <el-tag :type="result.status === 200 ? 'success' : 'danger'">{{ result.status }}</el-tag>
        </div>
        <el-tabs v-model="resultActive" type="card">
          <el-tab-pane label="返回结果" name="body">
            <el-input v-model="result.content" type="textarea" :readonly="true" :autosize="{ minRows: 2, maxRows: 200}" />
          </el-tab-pane>
          <el-tab-pane label="Headers" name="headers">
            <span slot="label" class="result-header-label">
              <span>Header <span class="param-count">({{ result.headerData.length }})</span></span>
            </span>
            <el-table
              :data="result.headerData"
              :header-cell-style="cellStyle"
              :cell-style="cellStyle"
            >
              <el-table-column label="Name" prop="name" />
              <el-table-column label="Value" prop="value" />
            </el-table>
          </el-tab-pane>
        </el-tabs>
      </el-col>
    </el-row>
  </div>
</template>
<style>
  .table-control {margin-bottom: 18px;}
  .cell .choose-file {padding: 5px;}
  .doc-debug .cell .el-form-item {margin-bottom: 0;}
  .result-header-label {font-size: 14px;}
  .result-header-label .el-badge__content.is-fixed {top: 10px;right: 0; }
  .json-badge .el-badge__content.is-fixed {top: 0;right: -3px; }
  .param-count {color: #909399;}
  .el-radio-group .el-badge {vertical-align: baseline;}
  .el-radio-group .is-active .param-count {color: #fff;}
  .CodeMirror {
    padding: 0px;
    border: 1px solid #eee;
    height: 450px;
  }
  .result-status {margin-bottom: 12px; font-size: 13px;color: #606266;}
</style>
<script>
require('fast-text-encoding')
export default {
  name: 'Docdebug',
  props: {
    items: {
      type: Array,
      default: () => []
    }
  },
  data() {
    return {
      rightSpanSize: 0,
      currentItem: null,
      itemMap: null,
      currentMethod: 'GET',
      cellStyle: { paddingTop: '5px', paddingBottom: '5px' },
      requestUrl: '',
      jsonBody: '',
      requestActive: 'body',
      postActive: 'form',
      collapseActive: '',
      formData: [],
      multipartData: [],
      queryData: [],
      uploadFiles: [],
      fieldTypes: [
        { type: 'text', label: '文本' },
        { type: 'file', label: '文件' }
      ],
      globalHeaderData: [],
      headerData: [],
      resultActive: 'result',
      result: {
        headerData: [],
        content: '',
        status: 200
      },
      cmOptions: {
        value: '',
        indentUnit: 4,
        mode: 'application/json',
        // readOnly: true,
        theme: 'neat'
      }
    }
  },
  created() {
    const itemMap = {}
    this.items.forEach(item => {
      itemMap[item.method] = item
    })
    this.itemMap = itemMap
    this.loadItem(this.items[0])
  },
  methods: {
    loadItem(item) {
      this.currentItem = item
      this.currentMethod = item.method
      this.bindUrl(item)
      this.bindRequestParam(item)
      this.loadGlobalHeaders(resp => {
        this.globalHeaderData = resp.data
      })
    },
    bindUrl(item) {
      let path = item.basePath + item.path
      if (path.startsWith('//')) {
        path = path.substring(1)
      }
      this.requestUrl = 'http://' + item.host + path
    },
    bindRequestParam(item) {
      const queryData = []
      const formData = []
      const multipartData = []
      const headerData = []
      let jsonBody = ''
      const isQueryStringMethod = this.isQueryStringMethod(item)
      const requestParameters = item.requestParameters
      requestParameters.forEach(row => {
        const propIn = row.in
        if (propIn === 'header') {
          headerData.push(row)
        } else {
          if (isQueryStringMethod) {
            queryData.push(row)
          } else {
            formData.push(row)
            multipartData.push(row)
          }
        }
      })
      this.headerData = headerData
      this.queryData = queryData
      this.formData = formData
      this.multipartData = multipartData
      // POST方法
      if (!this.isQueryStringMethod(item)) {
        const jsonObj = this.createResponseExample(requestParameters)
        jsonBody = JSON.stringify(jsonObj, null, 4)
      }
      this.jsonBody = jsonBody
      this.requestActive = isQueryStringMethod ? 'query' : 'body'
      if (item.uploadRequest) {
        this.postActive = 'multipart'
      } else {
        this.postActive = jsonBody ? 'json' : 'form'
      }
    },
    isQueryStringMethod(item) {
      return ['get', 'head'].indexOf(item.method.toLowerCase()) > -1
    },
    hasBody() {
      return this.jsonBody.length > 0 || this.formData.length > 0 || this.multipartData.length > 0
    },
    showBody(active) {
      return this.postActive === active
    },
    onMethodChange(method) {
      const item = this.itemMap[method]
      this.loadItem(item)
    },
    onSelectFile(file, row) {
      row.filename = file.name
      const rawFile = file.raw
      const reader = new FileReader()
      reader.readAsArrayBuffer(rawFile)
      reader.onload = () => {
        row.buffer = Buffer.from(reader.result)
      }
    },
    onSelectMultiFile(file, fileList) {
      const updateFiles = []
      fileList.forEach(file => {
        const rawFile = file.raw
        const reader = new FileReader()
        reader.readAsArrayBuffer(rawFile)
        reader.onload = () => {
          updateFiles.push({
            name: `file_${file.uid}`,
            buffer: Buffer.from(reader.result),
            filename: file.name,
            content_type: 'application/octet-stream'
          })
        }
      })
      this.uploadFiles = updateFiles
    },
    send() {
      const item = this.currentItem
      const headers = this.buildRequestHeaders()
      let data
      let isJson = false; let isForm = false; let isMultipart = false
      if (this.requestActive === 'query') {
        data = this.getParamObj(this.queryData)
      } else {
        // 如果请求body
        switch (this.postActive) {
          case 'json':
            isJson = true
            data = JSON.parse(this.jsonBody || '{}')
            break
          case 'form':
            isForm = true
            data = this.getParamObj(this.formData)
            break
          case 'multipart':
            isMultipart = this.multipartData.length > 0
            data = this.getParamObj(this.multipartData)
            break
          default:
            data = {}
        }
      }
      this.request(item.method, '/doc/proxy', data, headers, isJson, isForm, isMultipart, this.doProxyResponse)
    },
    buildRequestHeaders() {
      const headers = this.getParamObj(this.headerData)
      this.globalHeaderData.forEach(row => {
        headers[row.configKey] = row.configValue
      })
      headers['target-url'] = this.requestUrl
      return headers
    },
    getParamObj(array) {
      const data = {}
      array.forEach(row => {
        // 处理文件上传
        if (row.type === 'file') {
          data[row.name] = {
            buffer: row.buffer,
            filename: row.filename,
            content_type: 'application/octet-stream'
          }
        } else {
          data[row.name] = row.example
        }
      })
      this.uploadFiles.forEach(row => {
        data[row.name] = row
      })
      return data
    },
    doProxyResponse(error, response) {
      if (error) {
        this.$message.error(error)
        return
      }
      this.buildResultHeaders(response)
      this.buildResultStatus(error, response)
      this.buildResultContent(error, response)
    },
    buildResultStatus(error, response) {
      if (!error) {
        this.result.status = response.statusCode
      }
    },
    buildResultContent(error, response) {
      const headers = response.targetHeaders
      const contentType = headers['content-type'] || ''
      const contentDisposition = headers['content-disposition'] || ''
      const produces = this.currentItem.produces && this.currentItem.produces.join('')
      // 如果是下载文件
      this.openRightPanel()
      if (contentType.indexOf('stream') > -1 ||
        produces.indexOf('stream') > -1 ||
        contentDisposition.indexOf('attachment') > -1
      ) {
        const disposition = headers['content-disposition']
        const filename = this.getDispositionFilename(disposition)
        this.downloadFile(filename, response.raw)
      } else {
        let content = ''
        if (error) {
          content = error.message
        } else {
          const uint8Array = response.raw
          if (uint8Array && uint8Array.length > 0) {
            const resp = new TextDecoder().decode(uint8Array)
            content = JSON.stringify(JSON.parse(resp), null, 4)
          }
        }
        this.result.content = content
      }
    },
    downloadFile(filename, buffer) {
      const url = window.URL.createObjectURL(new Blob([buffer]))
      const link = document.createElement('a')
      link.href = url
      link.setAttribute('download', filename)
      document.body.appendChild(link)
      link.click()
    },
    getDispositionFilename(disposition) {
      const dispositionArr = disposition.split(';')
      for (let i = 0; i < dispositionArr.length; i++) {
        const item = dispositionArr[i].trim()
        // filename="xx"
        if (item.toLowerCase().startsWith('filename')) {
          const result = item.match(new RegExp('filename="(.*?)"', 'i'))
          return result ? result[1] : ''
        }
      }
    },
    buildResultHeaders(response) {
      const headers = response.headers
      const targetHeadersString = headers['target-response-headers'] || '{}'
      const targetHeaders = JSON.parse(targetHeadersString)
      response.targetHeaders = targetHeaders
      const headersData = []
      if (targetHeaders) {
        for (const key in targetHeaders) {
          headersData.push({ name: key, value: targetHeaders[key] })
        }
      }
      this.result.headerData = headersData
    },
    openRightPanel() {
      this.resultActive = 'body'
      this.rightSpanSize = 10
    }
  }
}
</script>
