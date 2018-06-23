<template>
  <model-list :model="model" :model-def="modelDef" :viewDef="viewDef" v-model="selected"
    selection="multiple" enableLink enableCopy enableCreate enableDelete />
</template>

<script>
import ModelList from '../components/model-list'

export default {
  components: { ModelList },
  data () {
    return {
      model: null,
      modelDef: null,
      viewDef: null,
      selected: []
    }
  },
  created () {
    // 检查model参数
    this.model = this.$route.params['model']
    // this.page.rowsPerPage = this.profile.properties
    this.viewDef = this.$store.state.login.permissions[this.model].list
    this.modelDef = this.$store.state.profile.modelsMap[this.model]
    if (this.viewDef == null || this.modelDef == null) {
      this.$router.replace('/error')
    }
  }
}
</script>
