import lodash from 'lodash'

export default ({ Vue }) => {
  Vue.prototype.$_ = lodash
}
