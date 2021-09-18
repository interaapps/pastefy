import Vue from 'vue'
import Vuex from 'vuex'

Vue.use(Vuex)

if (typeof localStorage.getItem('created_pastes') === 'undefined')
    localStorage.setItem('created_pastes', JSON.stringify([]))

let store = new Vuex.Store({
  state: {
    user: {
        loggedIn: false,
        id: -1,
        authType: "NONE"
    },
    mobileVersion: window.innerWidth <= 720,
    currentPaste: {
        title: "",
        content: "",
        password: "",
        folder: "",
        friends: "",
        editId: null,
        multiPastes: []
    },
    app: {
        fullscreen: false,
        sideNavTab: "paste",
        fullscreenOnHomepage: localStorage.getItem('fullscreen_on_homepage') == 'true',
        browserNotifications: localStorage.getItem('browser_notifications') == 'true',
        newPasteEditorDisableHighlighting: localStorage.getItem('new_paste_editor_disable_highlighting') == 'true',
        newPasteEditorDisableBracketClosing: localStorage.getItem('new_paste_editor_disable_bracket_closing') == 'true',
        newPasteEditorDisableLineNumbering: localStorage.getItem('new_paste_editor_disable_line_numbering') == 'true',
        lastPastes: JSON.parse(localStorage['created_pastes'] || "[]").splice(0, 15),
        loadingUser: false
    }
  },
  mutations: {
  },
  actions: {
  },
  modules: {
  }
})

export default store