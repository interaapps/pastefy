import Vue from 'vue'
import Vuex from 'vuex'

Vue.use(Vuex)

if (typeof localStorage.getItem('created_pastes') === 'undefined')
    localStorage.setItem('created_pastes', JSON.stringify([]))

let store = new Vuex.Store({
    state: {
        user: {
            logged_in: false,
            id: -1,
            auth_types: [],
            type: 'USER'
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
            newPasteEditorDisableAutocompletion: localStorage.getItem('new_paste_editor_disable_autocompletion') == 'true',
            lastPastes: JSON.parse(localStorage['created_pastes'] || "[]").splice(0, 15),
            loadingUser: false
        },
        appInfo: {
            custom_logo: null,
            custom_name: null,
            custom_footer: {},
            login_required_for_read: false,
            login_required_for_create: false,
            encryption_is_default: false
        }
    },
    mutations: {},
    actions: {},
    modules: {}
})

export default store