<template>
  <div class="entity-form">
    <template v-for="(item,index) in viewDef.fields" v-if="item.name !== 'id'">
      <q-field :label="item.label" :label-width="labelWidth" v-if="item.type === 'String'" :key="index">
        <q-input v-model="entity[item.name]" />
      </q-field>
      <q-field :label="item.label" :label-width="labelWidth" v-if="item.type === 'Password'" :key="index">
        <q-input type="password" v-model="entity[item.name]" />
      </q-field>
      <q-field :label="item.label" :label-width="labelWidth" v-if="item.type === 'Text'" :key="index">
        <q-input type="textarea" v-model="entity[item.name]" />
      </q-field>
      <q-field :label="item.label" :label-width="labelWidth" v-if="item.type === 'RichText'" :key="index">
        <q-editor v-model="entity[item.name]" />
      </q-field>
      <q-field :label="item.label" :label-width="labelWidth" v-if="item.type === 'Long' || item.type === 'Integer' || item.type === 'Double'" :key="index">
        <q-input type="number" v-model="entity[item.name]" />
      </q-field>
      <!-- q-uploader组件目前仅支持上传，不支持显示和编辑
      <q-field :label="item.label" :label-width="2" v-if="item.type === 'File'" :key="index">
        <q-uploader url="/api/file/upload" multiple no-thumbnails />
      </q-field> -->
      <q-field :label="item.label" :label-width="labelWidth" v-if="item.type === 'Boolean'" :key="index">
        <q-toggle v-model="entity[item.name]" />
      </q-field>
      <q-field :label="item.label" :label-width="labelWidth" v-if="item.type === 'Date'" :key="index">
        <q-input v-model="entity[item.name]" placeholder="1970-1-1" />
      </q-field>
      <q-field :label="item.label" :label-width="labelWidth" v-if="item.type === 'Datetime'" :key="index">
        <q-input v-model="entity[item.name]" placeholder="1970-1-1 00:00:00" />
      </q-field>
      <q-field :label="item.label" :label-width="labelWidth" v-if="item.type === 'Reference'" :key="index">
        <ref-popup v-model="entity[item.name]" :field="item" :multiple="item.multiple" />
      </q-field>
      <q-field :label="item.label" :label-width="labelWidth" v-if="item.type === 'Choice' && !item.multiple" :key="index">
        <q-select filter clearable v-model="entity[item.name]" :options="item.choices" />
      </q-field>
      <q-field :label="item.label" :label-width="labelWidth" v-if="item.type === 'Choice' && item.multiple" :key="index">
        <q-select filter clearable multiple v-model="entity[item.name]" :options="item.choices" />
      </q-field>
    </template>
  </div>
</template>

<script>
import RefPopup from '../components/ref-popup'
export default {
  name: 'EntityForm',
  components: { RefPopup },
  props: {
    entity: { required: true },
    model: { type: String, required: true },
    modelDef: { required: true },
    viewDef: { required: true },
    labelWidth: {default: 2}
  },
  data () {
    return {
    }
  },
  methods: {
  },
  created () {

  }
}
</script>

<style lang="stylus">
@media (min-width: 576px) {
  .q-field-label-inner {
    justify-content: flex-end;
    padding-right: 8px;
  }
}
</style>
