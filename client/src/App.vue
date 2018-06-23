<template>
  <div id="root">
    <router-view v-if="inited"></router-view>
    <q-ajax-bar />
  </div>
</template>

<script>
export default {
  data () {
    return {
      inited: false
    }
  },
  mounted () {
    console.log('kaixin start')

    // 初始化全局数据
    this.$http.get('/api/app/bootup').then(response => {
      this.$store.commit('bootup', response.data)
      if (!response.data.login.loginUser) {
        this.$router.replace('/login')
      }
      this.inited = true
      console.log('kaixin inited')
    })
  }
}
</script>
