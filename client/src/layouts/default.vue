<template>
  <q-layout ref="layout" view="hHh Lpr fff">
    <q-layout-header class="no-shadow">
      <q-toolbar>
        <!-- $refs 是 -->
        <q-btn flat @click="drawer = !drawer"><q-icon name="menu" /></q-btn>
        <!-- <q-btn flat @click="$router.replace('/')"><q-icon name="ion-ios-flower-outline" /></q-btn> -->
        <q-toolbar-title @click="$router.replace('/')">
          Kaixin
          <div slot="subtitle">Kaixin framework</div>
        </q-toolbar-title>
        <q-btn flat v-if="isLogin" @click="logout"><q-icon name="exit to app" /></q-btn>
      </q-toolbar>
    </q-layout-header>

    <q-layout-drawer side="left" v-model="drawer" content-class="no-shadow">
      <q-list>
        <template v-for="(item,index) in profile.admin.nav">
          <q-collapsible :icon="item.icon" :label="item.label" :key="index" v-if="item.show">
            <!-- quasar 0.16中用q-item代替q-side-link -->
            <q-item v-for="(sub) in item.children" :key="sub.model" :to="'/list/' + sub.model" v-if="sub.show">
              <q-item-side :icon="sub.icon" />
              <q-item-main :sublabel="profile.modelsMap[sub.model].label" />
            </q-item>
          </q-collapsible>
        </template>
      </q-list>
    </q-layout-drawer>

    <!-- fullPath让/list/:model这种route能重新加载，不必watch -->
    <q-page-container>
      <q-ajax-bar />
      <router-view :key="$route.fullPath" />
    </q-page-container>
  </q-layout>
</template>

<script>
import { mapState, mapGetters } from 'vuex'

export default {
  data () {
    return {
      drawer: true
    }
  },
  computed: {
    // ... 是展开运算符
    ...mapState([ 'login', 'profile' ]),
    ...mapGetters([ 'isLogin' ])
  },
  methods: {
    logout: function () {
      this.$http.get('/api/auth/logout').then(response => {
        this.$store.commit('logout')
        this.$router.replace('/login')
      }).catch((e) => {
        console.error(e)
        this.$router.replace('/error')
      })
    }
  },
  mounted () {
  }
}
</script>

<style lang="stylus">
// 左侧导航展开后不要缩进
.q-collapsible-sub-item {
  padding: 0
}
// 左侧导航宽度
.q-layout-drawer-left {
    width: 200px;
}
</style>
