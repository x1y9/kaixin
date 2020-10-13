<template>
  <div v-if="entity">
    <h5>{{modelDef.label}}</h5>
    <entity-form :entity="entity" :model="model" :model-def="modelDef" :view-def="modelDef" />
    <p/>
    <q-field label=" " :label-width="2">
      <q-btn color="primary" @click="save()"><q-icon name="save"/>新建</q-btn>
    </q-field>
  </div>
</template>

<script>
import { mapState } from 'vuex'
import EntityForm from '../components/entity-form'
import Consts from '../consts'

export default {
  components: {EntityForm},
  data () {
    return {
      model: null,
      modelDef: null,
      copyId: null,
      entity: null,
      options: null
    }
  },
  computed: {
    // ... 是展开运算符
    ...mapState([ 'profile' ])
  },
  methods: {
    save: function () {
      this.$http.post('/api/admin/' + this.model, this.entity).then(response => {
        this.$kx.notify('创建成功')
        this.$router.replace('/update/' + this.model + '/' + response.data.id)
      }).catch((e) => {
        console.error(e)
        this.$kx.notify('创建出错了')
      })
    },

    // 处理entity数据，如果data为空，则为新建
    processEntityData: function (data) {
      this.entity = data || {}
      this.entityId = data ? data.id : -1
      this.options = {}
      this.modelDef.fields.forEach(f => {
        // 新建的对象要处理缺省值, 但${now}是后台用的，无需处理，用$set防止vue的reactivity不工作
        if (data == null && f.default != null && f.default !== '${now}') {
          this.$set(this.entity, f.name, f.default)
        }

        // ref-popup组件需要传数组
        if (f.type === Consts.REFERENCE && !f.multiple) {
          this.entity[f.name] = this.entity[f.name] == null ? [] : [this.entity[f.name]]
        }

        // Choice要将值替换
        if (f.type === Consts.CHOICE && this.entity[f.name] != null) {
          this.entity[f.name] = f.multiple ? this.entity[f.name].map(item => item[Consts.CHOICE_VALUE]) : this.entity[f.name][Consts.CHOICE_VALUE]
        }

        // 多值choice不能传空
        if (f.type === Consts.CHOICE && f.multiple && this.entity[f.name] == null) {
          this.$set(this.entity, f.name, [])
        }

        // 富文本控件不允许空值
        if (f.type === Consts.RICH_TEXT && this.entity[f.name] == null) {
          this.$set(this.entity, f.name, '')
        }
        // 布尔控件不允许空值，否则第一次点击无效
        if (f.type === Consts.BOOLEAN && this.entity[f.name] == null) {
          this.$set(this.entity, f.name, false)
        }
      })
    }
  },

  created () {
    // 检查model参数
    this.model = this.$route.params['model']
    this.copyId = this.$route.params['entity']
    this.modelDef = this.$store.state.profile.modelsMap[this.model]
    this.showFields = this.$store.state.login.permissions[this.model].create
    if (this.modelDef == null) {
      this.$router.replace('/error')
    }

    if (this.copyId >= 0) {
      this.$http.get('/api/admin/' + this.model + '/' + this.copyId).then(response => {
        this.processEntityData(response.data)
      }).catch((e) => {
        console.error(e)
        this.$router.replace('/error')
      })
    } else {
      this.processEntityData(null)
    }
  }
}
</script>
