<template>
    <div>
        <router-link to="/" id="logo" :class="{'mobile': $store.state.mobileVersion && this.$route.path !== '/'}">
            <img :src="$store.state.appInfo.custom_logo ? $store.state.appInfo.custom_logo : require('../../assets/logo.png')">
        </router-link>
        <div id="sidebar"
             :class="{'input-scrolled': contentInputScrolled, 'fullscreen': $store.state.app.fullscreen || (($store.state.mobileVersion || $store.state.app.fullscreenOnHomepage) && this.$route.path === '/'), 'hidden': $store.state.mobileVersion && this.$route.path !== '/'}">
            <svg
                v-if="!($store.state.mobileVersion || ($store.state.app.fullscreenOnHomepage && this.$route.path === '/')) && !($store.state.appInfo.login_required_for_create && !$store.state.user.logged_in)"
                id="fullscreen-button" @click="$store.state.app.fullscreen = !$store.state.app.fullscreen" width="1em"
                height="1em" viewBox="0 0 16 16" class="bi bi-chevron-right" fill="currentColor"
                xmlns="http://www.w3.org/2000/svg">
                <path fill-rule="evenodd"
                      d="M4.646 1.646a.5.5 0 0 1 .708 0l6 6a.5.5 0 0 1 0 .708l-6 6a.5.5 0 0 1-.708-.708L10.293 8 4.646 2.354a.5.5 0 0 1 0-.708z"/>
            </svg>

            <router-link v-if="$store.state.user.logged_in" id="profile-picture" to="/home"
                         :style="{'margin-right': $store.state.mobileVersion || ($store.state.app.fullscreenOnHomepage && this.$route.path === '/') ? '0px' : '14px'}">
                <img :src="$store.state.user.profile_picture" :style="{border: $store.state.user.color+' 2px solid'}">
            </router-link>
            <LoadingSpinner width="32px" height="32px" style="margin-top: 6.4px" id="profile-picture"
                            v-else-if="$store.state.app.loadingUser"/>
            <a :href="$store.state.user.auth_types.length == 1 ? loginBaseURL+$store.state.user.auth_types[0] : '/login-with'"
               id="profile-picture" class="login" v-else-if="$store.state.user.auth_types.length > 0">LOGIN</a>

            <CreatePaste :input-fullscreen="inputFullscreen" />

            <div @click="$router.push('/login-with')" id="login-required-info"
                 v-show="($store.state.appInfo.login_required_for_create && !$store.state.user.logged_in)">
                <h1>Log in to paste</h1>
            </div>

            <LoadingSpinner v-if="loading" width="50px" height="50px" id="loading"/>
        </div>
        <div id="footer">
            <a target="_blank" href="https://github.com/interaapps/pastefy">
                <svg fill="currentColor" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24">
                    <defs></defs>
                    <title>github</title>
                    <g id="Layer_2" data-name="Layer 2">
                        <g id="github">
                            <path class="cls-2"
                                  d="M16.24,22a1,1,0,0,1-1-1V18.4a2.15,2.15,0,0,0-.54-1.66,1,1,0,0,1,.61-1.67C17.75,14.78,20,14,20,9.77a4,4,0,0,0-.67-2.22,2.75,2.75,0,0,1-.41-2.06,3.71,3.71,0,0,0,0-1.41,7.65,7.65,0,0,0-2.09,1.09,1,1,0,0,1-.84.15,10.15,10.15,0,0,0-5.52,0,1,1,0,0,1-.84-.15A7.4,7.4,0,0,0,7.52,4.08a3.52,3.52,0,0,0,0,1.41,2.84,2.84,0,0,1-.43,2.08A4.07,4.07,0,0,0,6.42,9.8c0,3.89,1.88,4.93,4.7,5.29a1,1,0,0,1,.82.66,1,1,0,0,1-.21,1,2.06,2.06,0,0,0-.55,1.56V21a1,1,0,0,1-2,0v-.57a6,6,0,0,1-5.27-2.09,3.9,3.9,0,0,0-1.16-.88,1,1,0,1,1,.5-1.94,4.93,4.93,0,0,1,2,1.36c1,1,2,1.88,3.9,1.52h0a3.89,3.89,0,0,1,.23-1.58c-2.06-.52-5-2-5-7a6,6,0,0,1,1-3.33.85.85,0,0,0,.13-.62,5.69,5.69,0,0,1,.33-3.21,1,1,0,0,1,.63-.57c.34-.1,1.56-.3,3.87,1.2a12.16,12.16,0,0,1,5.69,0c2.31-1.5,3.53-1.31,3.86-1.2a1,1,0,0,1,.63.57,5.71,5.71,0,0,1,.33,3.22.75.75,0,0,0,.11.57,6,6,0,0,1,1,3.34c0,5.07-2.92,6.54-5,7a4.28,4.28,0,0,1,.22,1.67V21A1,1,0,0,1,16.24,22Z"/>
                        </g>
                    </g>
                </svg>
            </a>
            <a v-for="(item, key) of $store.state.appInfo.custom_footer" :href="item" :key="key"
               :style="{display: !isPWA() ? 'inline-block' : 'none'}"><span v-if="!isPWA() && key">{{ key }}</span></a>
            <router-link to="/settings">SETTINGS</router-link>
        </div>
        <div style="display: none">{{ r }}</div>
    </div>
</template>
<script>
import LoadingSpinner from "../LoadingSpinner.vue";
import eventBus from '../../eventBus.js';
import CreatePaste from '@/components/CreatePaste.vue'

export default {
    data: () => ({
        loading: false,
        loginBaseURL: process.env.VUE_APP_API_BASE + "/api/v2/auth/oauth2/",
        inputFullscreen: false,
        r: 0,
        contentInputScrolled: 0,

        currentLanguage: ""
    }),
    mounted() {
        eventBus.$on("appInfoLoaded", () => {
            if (this.$store.state.appInfo.encryption_is_default) {
                this.clientEncrypted = true
            }
        })
    },
    created() {
        document.onkeyup = (e) => {
            if (e.ctrlKey && e.which == 66) {
                this.$store.state.app.fullscreen = true;
                e.preventDefault()
            }
        };

        try {
            if (localStorage["saved_contacts"] != null)
                this.friendList = JSON.parse(localStorage["saved_contacts"])
        } catch (e) {
            this.friendList = []
        }
    },
    components: {
        CreatePaste,
        LoadingSpinner
    },
    watch: {
        '$route'(to) {
            if (this.$store.state.app.fullscreenOnHomepage && to.path !== '/')
                this.$store.state.app.fullscreen = false
        }
    },
    methods: {
        isPWA() {
            return window.matchMedia('(display-mode: standalone)').matches;
        },
        addFriendToList(name) {
            if (!this.$store.state.currentPaste.friends.includes(name)) {
                this.$store.state.currentPaste.friends += name + ", "
            }
        }
    }
}
</script>