<template>
  <!-- input-frame有很多通用属性 -->
  <q-input-frame class="q-chips-input"
    :prefix="prefix"
    :suffix="suffix"
    :stack-label="stackLabel"
    :float-label="floatLabel"
    :error="error"
    :warning="warning"
    :disable="disable"
    :inverted="inverted"
    :inverted-light="invertedLight"
    :dark="dark"
    :hide-underline="hideUnderline"
    :before="before"
    :after="after"
    :color="color"
    :no-parent-field="noParentField"
  >
    <div class="col row items-center group q-input-chips">
      <q-chip small :closable="editable" v-for="(label, index) in selected" :key="`${label}#${index}`"
        :color="color" text-color="white"
        @hide="remove(index)">
        {{ label[field.targetField] }}
      </q-chip>
    </div>

    <q-icon v-if="editable" name="edit" slot="after" class="q-if-control" @click.native="isOpen = true" />

    <slot/>

    <q-modal v-model="isOpen">
      <model-list ref="popup" v-model="selectedInPopup"
                  :model="field.target" :model-def="refDef" :view-def="refFields"
                  :selection="multiple ? 'multiple' : 'single'" />
      <q-btn color="primary" :disable="selectedInPopup.length === 0" @click="closePopup()" label="选择"/>
      <q-btn color="primary" @click="isOpen = false" label="取消"/>
    </q-modal>
  </q-input-frame>
</template>

<script>
import FrameMixin from '../mixins/input-frame'
import ModelList from './model-list'
export default {
  name: 'RefPopup',
  components: { ModelList },
  mixins: [FrameMixin],
  props: {
    value: { required: true }, // 无论是否multiple，都需要传递数组，为了支持v-model，必须叫value
    multiple: Boolean,
    field: Object,
    chipsColor: String,
    chipsBgColor: String
  },
  data () {
    return {
      selected: this.value || [],
      selectedInPopup: this.value || [], // 考虑选择可以取消，这里用另一个变量临时保存
      refModel: null,
      refDef: null,
      refFieds: null,
      isOpen: false
    }
  },
  computed: {
  },
  methods: {
    remove (index) {
      if (this.editable && index >= 0 && index < this.selected.length) {
        this.selected.splice(index, 1)
        this.$emit('input', this.selected)
      }
    },
    closePopup () {
      // let selected = this.$refs.popup.getSelected()
      this.isOpen = false
      if (this.editable && this.selectedInPopup.length > 0) {
        if (this.multiple) {
          // 去重
          this.selectedInPopup.forEach(select => {
            if (!this.selected.find(item => item.id === select.id)) {
              this.selected.push(select)
            }
          })
        } else {
          this.selected = this.selectedInPopup
        }
        this.$emit('input', this.selected)
      }
      this.$refs.popup.clearSelected()
    }
  },
  created () {
    this.refModel = this.field.target
    this.refFields = this.$store.state.login.permissions[this.refModel].list
    this.refDef = this.$store.state.profile.modelsMap[this.refModel]
    if (this.refFields == null || this.refDef == null) {
      console.error('ref-popup parameter error')
    }
  }
}
</script>
