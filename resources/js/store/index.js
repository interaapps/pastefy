import Vue from 'vue'
import Vuex from 'vuex'

Vue.use(Vuex)

export default new Vuex.Store({
  state: {
    user: {
        loggedIn: false
    },
    mobileVersion: window.innerWidth <= 720,
    currentPaste: {
        title: "",
        content: "",
        password: "",
        folder: ""
    },
    app: {
        fullscreen: false,
        sideNavTab: "paste"
    }
  },
  mutations: {
  },
  actions: {
  },
  modules: {
  }
})