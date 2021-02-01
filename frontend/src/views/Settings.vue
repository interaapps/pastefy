<template>
    <div>
        <h1>Settings</h1>
        
        Fullscreen-Paste on Homepage: <input v-model="$store.state.app.fullscreenOnHomepage" type="checkbox"><br>
        Browser-notifications: <input v-model="$store.state.app.browserNotifications" type="checkbox">
    </div>
</template>
<script>
import helper from "../helper";
export default {
    data: ()=>({
        fullscreenOnHomepage: false
    }),
    mounted(){
    },
    methods:{
        
    },
    watch: {
        '$store.state.app.fullscreenOnHomepage': {
            handler: function(to){
                console.log(to)
                localStorage.setItem('fullscreen_on_homepage', to)
            },
            immediate: false
        },
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