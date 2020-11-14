<template>
  <q-table :title="modelDef.label" :data="table" :columns="columns" :visible-columns="visibleColumns"
            row-key="id" :pagination.sync="page" :filter="search" @request="request"
            :selection="selection" :selected.sync="selected" dense>

    <template slot="top-left" slot-scope="props">
      <span>{{modelDef.label}}</span>
      <q-btn v-if="enableCreate" sm color="primary" class="no-shadow" icon="add" @click="$router.push('/create/' + model + '/-1')">新建</q-btn>
    </template>

    <template slot="top-right" slot-scope="props">
      <q-search hide-underline v-model="search" />
      <q-table-columns  v-model="visibleColumns" :columns="columns" @input="onVisibleColumnsChange"/>
      <!-- 全屏模式有时不能正常退出 -->
      <!-- <q-btn flat round dense :icon="props.inFullscreen ? 'fullscreen_exit' : 'fullscreen'" @click="props.toggleFullscreen"/> -->
    </template>

    <!-- 如果用户想，可以按住shift在新标签页打开以保持列表位置, slot-scope必须要有 -->
    <!-- 试过在这里对其他单元格做格式化，放在v-for里，总是不成功 -->
    <q-td slot="body-cell-id" slot-scope='props' :props="props">
      <router-link v-if="enableLink" :to="'/update/' + model + '/' + props.value">{{props.value}}</router-link>
      <span v-if="!enableLink">{{props.value}}</span>
    </q-td>

    <div slot="top-selection" slot-scope='props'>
      <q-btn v-if="enableCopy && selected.length === 1" color="primary" class="no-shadow" icon="file_copy" @click="copy">复制</q-btn>
      <q-btn v-if="enableDelete" color="negative" class="no-shadow" icon="delete" @click="remove">删除</q-btn>
      <!-- v-if加在slot上面不好用，所以要在下面多写一个span -->
      <span v-if="!enableLink">{{modelDef.label}}</span>
    </div>
  </q-table>
</template>

<script>
import Consts from '../consts'
export default {
  name: 'ModelList',
  props: {
    value: { required: true },
    model: { type: String, required: true },
    modelDef: { required: true },
    viewDef: { required: true },
    selection: String,
    enableLink: Boolean,
    enableCopy: Boolean,
    enableDelete: Boolean,
    enableCreate: Boolean
  },
  data () {
    return {
      table: [],
      columns: [],
      page: {
        page: 1,
        rowsPerPage: 10,
        rowsNumber: 10 // specifying this determines pagination is server-side
      },
      search: '',
      visibleColumns: [],
      selected: this.value || []
    }
  },
  watch: {
    selected: function (value) {
      this.$emit('input', value)
    }
  },
  methods: {
    clearSelected: function () {
      this.selected = []
    },
    onVisibleColumnsChange: function (val) {
      this.$store.commit('setFilter', {model: this.model, columns: val})
    },
    request: function ({ pagination }) {
      // 传进来的pagination不等于this.page, 传进来的filter等于this.search, 所以不用
      let start = (pagination.page - 1) * pagination.rowsPerPage
      let sort = (pagination.sortBy == null) ? '' : ('&_sort=' + pagination.sortBy)
      let reverse = (pagination.sortBy == null) ? '' : ('&_reverse=' + (pagination.descending || false))
      let filters = (this.search == null) ? '' : ('&_filters=' + encodeURIComponent(this.search))
      this.$http.get(`/api/admin/${this.model}?_number=${pagination.rowsPerPage}&_start=${start}&_retTotal=true${sort}${reverse}${filters}`).then(response => {
        this.page = pagination
        this.page.rowsNumber = Number.parseInt(response.headers['x-total-count'])
        this.table = this.processGridData(response.data)
        // 记录当前的filter和page
        this.$store.commit('setFilter', {model: this.model, page: this.page, search: this.search})
      }).catch((e) => {
        console.error(e)
        this.$kx.notify('出错了', 'warning')
      })
    },
    copy: function () {
      if (this.selected.length > 0) {
        let copyId = this.selected[0].id
        this.$router.push('/create/' + this.model + '/' + copyId)
      }
    },
    remove: function () {
      if (this.selected.length > 0) {
        this.$q.dialog({title: '确认', message: '确认删除吗？', ok: '删除', cancel: '取消'}).then(() => {
          let ids = this.selected.map((item) => item.id)
          this.$http.delete('/api/admin/' + this.model + '/?ids=' + ids.join(',')).then(response => {
            this.selected = []
            this.$kx.notify('删除成功')
            this.request({pagination: this.page})
          }).catch((e) => {
            console.error(e)
            this.$kx.notify('删除出错了', 'warning')
          })
        }).catch((e) => {
          console.error(e)
        })
      }
    },
    processGridData: function (data) {
      for (let i = 0; i < data.length; i++) {
        this.viewDef.forEach(f => {
          // 把Reference类型的显示值提上来，去除id
          if (f.type === Consts.REFERENCE && !f.multiple && data[i][f.name] != null) {
            data[i][f.name] = data[i][f.name][f.targetField]
          }
          if (f.type === Consts.REFERENCE && f.multiple && data[i][f.name] != null) {
            data[i][f.name] = data[i][f.name].map(item => item[f.targetField]).join(Consts.MULTIPLE_SEPERATOR)
          }
          // 去除富文本格式
          if (f.type === Consts.RICH_TEXT && data[i][f.name] != null) {
            data[i][f.name] = data[i][f.name].replace(/<[^>]*>/g, '')
          }
          if (f.type === Consts.CHOICE && !f.multiple && data[i][f.name] != null) {
            data[i][f.name] = data[i][f.name][Consts.CHOICE_LABEL]
          }
          if (f.type === Consts.CHOICE && f.multiple && data[i][f.name] != null) {
            data[i][f.name] = data[i][f.name].map(item => item[Consts.CHOICE_LABEL]).join(Consts.MULTIPLE_SEPERATOR)
          }
        })
      }
      return data
    }
  },
  created () {
    this.columns = []
    for (let i = 0; i < this.viewDef.length; i++) {
      let item = this.viewDef[i]
      this.columns.push({label: item.label, name: item.name, field: item.name, align: 'left', sortable: true})
      if (i < 5) {
        this.visibleColumns.push(item.name)
      }
    }
    if (this.$store.state.filter[this.model]) {
      this.search = this.$store.state.filter[this.model].search || this.search
      this.page = this.$store.state.filter[this.model].page || this.page
      this.visibleColumns = this.$store.state.filter[this.model].columns || this.visibleColumns
    }
    // 请求第一页数据
    this.request({pagination: this.page})
  }
}
</script>
