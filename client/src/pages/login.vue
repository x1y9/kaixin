<template>
  <div class="layout-padding docs-input row justify-center">
   <div style="width: 400px; max-width: 90vw;">
    <q-input v-model="account" @keyup.enter="login" float-label="Account" />
    <q-input v-model="password" type="password" @keyup.enter="login" float-label="Password" />
    <q-btn type="submit" color="primary" @click="login">login</q-btn> <span>{{error}}</span>
   </div>
  </div>
</template>

<script>
export default {
  data () {
    return {
      account: '',
      password: '',
      error: ''
    }
  },
  methods: {
    login: function () {
      this.$http.post('/api/auth/login', {account: this.account, password: this.password}).then(response => {
        // 非app模式，不需要记录token，这里直接重新加载
        // 如果工作在router的hash mode也可以用window.location.pathname抛弃#回到根
        window.location.replace(window.location.pathname)
      }).catch(error => {
        this.error = error.response.data.message
      })
    }
  }
}
</script>

<style lang="stylus">
</style>
