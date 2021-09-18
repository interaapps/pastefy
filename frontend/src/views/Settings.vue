<template>
    <div>
        <h1>Settings</h1>
        
        Fullscreen-Paste on Homepage: <input v-model="$store.state.app.fullscreenOnHomepage" type="checkbox"><br>
        Browser-notifications: <input v-model="$store.state.app.browserNotifications" type="checkbox"><br>
        <br><h3>Editor</h3>
        Disable Highlighting: <input v-model="$store.state.app.newPasteEditorDisableHighlighting" type="checkbox"><br>
        Disable Bracket-Closing: <input v-model="$store.state.app.newPasteEditorDisableBracketClosing" type="checkbox"><br>
        Disable Line-Numbering: <input v-model="$store.state.app.newPasteEditorDisableLineNumbering" type="checkbox"><br>

        <div v-if="$store.state.user.logged_in">
            <router-link to="/apikeys" class="button gray">Api-Keys</router-link><br>
        </div>
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
        fullscreenOnHomepage: false
    }),
    mounted(){
    },
    methods:{
        
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