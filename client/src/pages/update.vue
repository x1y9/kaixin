<template>
  <div v-if="entity">
    <h5>{{modelDef.label}}
      <q-btn flat v-if="entityId >= 0" color="red" class="float-right" @click="remove()"><q-icon name="delete"/>删除</q-btn>
    </h5>
    <entity-form :entity="entity" :model="model" :model-def="modelDef" :view-def="viewDef" />
    <p/>
    <q-field label=" " :label-width="2">
      <q-btn color="primary" @click="save()"><q-icon name="save"/>保存</q-btn>
    </q-field>
  </div>
</template>

<script>
import { mapState } from 'vuex'
import EntityForm from '../components/entity-form'
import Consts from '../consts'

export default {
  components: { EntityForm },
  data () {
    return {
      model: null,
      modelDef: null,
      viewDef: null,
      entityId: null,
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
      this.$http.put('/api/admin/' + this.model + '/' + this.entityId, this.entity).then(response => {
        // 可能后台有自动处理修改某些字段，这里重新加载一次
        this.processEntityData(response.data)
        this.$kx.notify('修改成功')
      }).catch((e) => {
        console.error(e)
        this.$kx.notify('修改出错了')
      })
    },
    remove: function () {
      console.log(this.$q.Dialog)
      this.$q.dialog({title: '确认', message: '确认删除吗？', ok: '删除', cancel: '取消'}).then(() => {
        this.$http.delete('/api/admin/' + this.model + '/' + this.entityId).then(response => {
          this.$router.go(-1)
        }).catch((e) => {
          console.error(e)
          this.$kx.notify('删除出错了')
        })
      }).catch((e) => {
        console.error(e)
      })
    },

    // 处理entity数据, data肯定不空
    processEntityData: function (data) {
      this.entity = data
      this.entityId = data ? data.id : -1
      this.options = {}
      this.modelDef.fields.forEach(f => {
        // ref-popup组件需要传数组
        if (f.type === Consts.REFERENCE && !f.multiple) {
          this.entity[f.name] = data[f.name] == null ? [] : [data[f.name]]
        }

        // Choice要将值替换
        if (f.type === Consts.CHOICE && this.entity[f.name] != null) {
          this.entity[f.name] = f.multiple ? this.entity[f.name].map(item => item[Consts.CHOICE_VALUE]) : this.entity[f.name][Consts.CHOICE_VALUE]
        }

        // 多值choice不能传空,
        if (f.type === Consts.CHOICE && f.multiple && this.entity[f.name] == null) {
          this.$set(this.entity, f.name, [])
        }

        // 富文本控件不允许空值
        if (f.type === Consts.RICH_TEXT && data[f.name] == null) {
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
    this.entityId = this.$route.params['entity']
    this.modelDef = this.$store.state.profile.modelsMap[this.model]
    this.viewDef = this.$store.state.login.permissions[this.model].edit
    if (this.modelDef == null) {
      this.$router.replace('/error')
    }

    this.$http.get('/api/admin/' + this.model + '/' + this.entityId).then(response => {
      this.processEntityData(response.data)
    }).catch((e) => {
      console.error(e)
      this.$router.replace('/error')
    })
  }
}
</script>
