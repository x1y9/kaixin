import { Notify } from 'quasar'

export default ({ app, Vue }) => {
  Vue.prototype.$kx = {
    pcolor: 'secondary',

    notify: function (message, type) {
      Notify.create({
        message,
        timeout: 1000,
        type: type || 'info',
        position: 'bottom-right'
      })
    }
  }
}
