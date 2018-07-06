// kaixin: no modules here, one file is ok
import Vue from 'vue'
import Vuex from 'vuex'

Vue.use(Vuex)

// 根据用户权限决定是否显示左侧导航
function setNavShow (node, permissions) {
  if (node.children) {
    let parentShow = false
    node.children.forEach(item => {
      item.show = setNavShow(item, permissions)
      if (item.show) {
        parentShow = true
      }
    })
    node.show = parentShow
    return parentShow
  } else {
    return permissions[node.model] && permissions[node.model].list
  }
}

export default new Vuex.Store({
  state: {
    token: localStorage.getItem('token'),
    login: null, // loginUser, isAdmin, permissions
    profile: null, // 完整定义admin,models,modelsMap,
    filter: {} // 列表界面的一些状态记录在这里，以便返回时保留参数
  },
  getters: {
    hasToken (state) {
      return state.token != null
    },
    isLogin (state) {
      return state.login != null && state.login.loginUser != null
    }
  },
  mutations: {
    // 暂时不会设置token，只在app模式有意义
    setToken (state, token) {
      state.token = token
      localStorage.setItem('token', token)
    },
    logout (state) {
      state.token = null
      state.login = null
      localStorage.removeItem('token')
    },
    setFilter (state, para) {
      state.filter[para.model] = Object.assign(state.filter[para.model] || {}, para)
    },
    bootup (state, info) {
      state.profile = info.profile
      state.login = info.login
      // 根据permissions控制nav的show
      if (info.login.permissions) {
        state.profile.admin.nav.forEach(item => {
          setNavShow(item, info.login.permissions)
        })
      }
    }
  }
})
