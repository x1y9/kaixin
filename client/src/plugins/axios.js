import axios from 'axios'

export default ({ Vue }) => {
  Vue.prototype.$http = axios.create({
    // 如果是app，通常就需要设置baseURL
    // baseURL: 'http://localhost:8000/api'

    // 后台如果使用cookie传递认证信息，这里就不需要设置header
    // headers: {'token': store.state.token}
  })
}
