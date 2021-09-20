<template>
    <div>
        <h1>Settings</h1><br>
        
        Fullscreen-Paste on Homepage: <input v-model="$store.state.app.fullscreenOnHomepage" type="checkbox"><br>
        Browser-notifications: <input v-model="$store.state.app.browserNotifications" type="checkbox"><br>
        <br><h3>Editor</h3>
        Disable Highlighting: <input v-model="$store.state.app.newPasteEditorDisableHighlighting" type="checkbox"><br>
        Disable Bracket-Closing: <input v-model="$store.state.app.newPasteEditorDisableBracketClosing" type="checkbox"><br>
        Disable Line-Numbering: <input v-model="$store.state.app.newPasteEditorDisableLineNumbering" type="checkbox"><br>

        <div v-if="$store.state.user.logged_in">
            <br><h3>My Account</h3>
            <router-link to="/apikeys" class="button gray">Api-Keys</router-link><br>
        </div>

        <br><h3>Data & Privacy</h3>
        <a class="button gray" style="margin-right: 15px;" @click="logout" v-if="$store.state.user.logged_in">Logout</a>
        <a class="button gray" @click="clearAll">Clear Cookies & localStorage</a><br>
        <p style="color: #FFFFFF77; margin-top: 10px; max-width: 590px;"><span style="color: #FFFFFF99">Warning!</span> Both buttons above will log you out of your account or delete your non-logged-in paste-history and settings</p>
        <br>
        
        <div v-if="isPublicPastefyServer() || true" style="max-width: 840px">
            <h3>Information</h3>
            <p>
                Privacy Information: Pastefy doesn't use any trackers or ad-services to collect data.
                <br><br>
                You love Pastefy? We don't earn any money with pastefy, so if you have some money left you can donate <a href="https://liberapay.com/JulianFun123">here</a> if you want to.
            </p>
        </div> 
        <!--
        Saved Cookies: {{privacyData.cookies}} (Don't needed for pastefy)<br>
        Saved localStorage: {{privacyData.localStorage}} (Including login-session and options)<br>-->
    </div>
</template>
<script>
import helper from "../helper";

function createWatcher(n){
    return {
        handler: function(to){
            localStorage.setItem(n, to)
        },
        immediate: false
    }
}

export default {
    data: ()=>({
        fullscreenOnHomepage: false,
        privacyData: {
            cookies: document.cookie.split(";").length-1,
            localStorage: localStorage.length,
        }
    }),
    mounted(){
    },
    methods:{
        logout(){
            localStorage.removeItem("session")
            window.location = "/"
        },
        clearAll(){
            localStorage.clear()
            sessionStorage.clear()
            
            const cookies = document.cookie.split(";");
            for (const cookie of cookies) {
                const eqPos = cookie.indexOf("=");
                const name = eqPos > -1 ? cookie.substr(0, eqPos) : cookie;
                document.cookie = name + "=;expires=Thu, 01 Jan 1970 00:00:00 GMT";
            }
        }
    },
    watch: {
        '$store.state.app.fullscreenOnHomepage': createWatcher('fullscreen_on_homepage'),
        '$store.state.app.newPasteEditorDisableHighlighting': createWatcher('new_paste_editor_disable_highlighting'),
        '$store.state.app.newPasteEditorDisableBracketClosing': createWatcher('new_paste_editor_disable_bracket_closing'),
        '$store.state.app.newPasteEditorDisableLineNumbering': createWatcher('new_paste_editor_disable_line_numbering'),
        '$store.state.app.browserNotifications': {
            handler: function(to){
                localStorage.setItem('browser_notifications', to)
                if(this.worker)
                this.worker.postMessage({action: 'updateStorage', storage: {
                    notificationsEnabled: this.$store.state.app.browserNotifications
                }})
                if (to) {
                     if (Notification.permission !== "denied") {
                         Notification.requestPermission().then((permission) => {
                            if (permission === "granted") {
                                let notification = new Notification("Pastefy settings", {
                                    image: '/assets/images/icon.png',
                                    body:"You set up pastefy-notifications!"
                                });
                                notification.onclick = ()=>{
                                    window.focus()
                                    this.$router.push("/")
                                }

                                localStorage.setItem('browser_notifications', to)

                                if(this.worker)
                                this.worker.postMessage({action: 'updateStorage', storage: {
                                    notificationsEnabled: true
                                }})
                            } else {
                                helper.showSnackBar("Couldn't set up notifications!", "#FF3232")
                                this.$store.state.app.browserNotifications = false
                            }
                        });
                     } else {
                            helper.showSnackBar("Couldn't set up notifications! Please activate it manually", "#FF3232")
                            this.$store.state.app.browserNotifications = false
                     }
                }

            },
            immediate: false
        }
    }
    
}
</script>
<style lang="scss" scoped>

</style>