<template>
  <div id="app">
    <el-backtop />
    <router-view />
  </div>
</template>

<script>
export default {
  name: 'App',
  created() {
    this.loadDoc()
  },
  methods: {
    loadDoc() {
      const swaggerId = this.getSwaggerId()
      if (!swaggerId) {
        this.get('/swagger/get/first', {}, resp => {
          const data = resp.data
          data && this.initDoc(data.id)
        })
      } else {
        this.initDoc(swaggerId)
      }
    }
  }
}
</script>
