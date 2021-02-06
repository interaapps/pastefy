<template>
    <div>
        <router-link to="/" id="logo" :class="{'mobile': $store.state.mobileVersion && this.$route.path !== '/'}">
            <img src="../assets/logo.png">
        </router-link>
        <div id="sidebar" :class="{'fullscreen': $store.state.app.fullscreen || (($store.state.mobileVersion || $store.state.app.fullscreenOnHomepage) && this.$route.path === '/'), 'hidden': $store.state.mobileVersion && this.$route.path !== '/'}">
            <svg v-if="!($store.state.mobileVersion || ($store.state.app.fullscreenOnHomepage && this.$route.path === '/'))" id="fullscreen-button" @click="$store.state.app.fullscreen = !$store.state.app.fullscreen" width="1em" height="1em" viewBox="0 0 16 16" class="bi bi-chevron-right" fill="currentColor" xmlns="http://www.w3.org/2000/svg"><path fill-rule="evenodd" d="M4.646 1.646a.5.5 0 0 1 .708 0l6 6a.5.5 0 0 1 0 .708l-6 6a.5.5 0 0 1-.708-.708L10.293 8 4.646 2.354a.5.5 0 0 1 0-.708z"/></svg>
            
            <router-link v-if="$store.state.user.logged_in" id="profile-picture" to="/home" :style="{'margin-right': $store.state.mobileVersion || ($store.state.app.fullscreenOnHomepage && this.$route.path === '/') ? '0px' : '14px'}">
                <img :src="$store.state.user.profile_picture" :style="{border: $store.state.user.color+' 2px solid'}">
            </router-link>
            <LoadingSpinner width="32px" height="32px" style="margin-top: 6.4px" id="profile-picture" v-else-if="$store.state.app.loadingUser" />
            <a :href="loginURL" id="profile-picture" class="login" v-else>LOGIN</a>
            
            <div v-if="$store.state.app.sideNavTab === 'paste'" id="create-paste" :class="{'input-fullscreen': inputFullscreen}" style="height: 70%">
                <input autocomplete="off" v-model="$store.state.currentPaste.title" class="input" type="text" placeholder="Title" id="title-input">
                
                <svg id="input-fullscreen-button" @click="inputFullscreen = false" v-if="inputFullscreen && !$store.state.mobileVersion" xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="currentColor" class="bi bi-fullscreen-exit" viewBox="0 0 16 16"><path d="M5.5 0a.5.5 0 0 1 .5.5v4A1.5 1.5 0 0 1 4.5 6h-4a.5.5 0 0 1 0-1h4a.5.5 0 0 0 .5-.5v-4a.5.5 0 0 1 .5-.5zm5 0a.5.5 0 0 1 .5.5v4a.5.5 0 0 0 .5.5h4a.5.5 0 0 1 0 1h-4A1.5 1.5 0 0 1 10 4.5v-4a.5.5 0 0 1 .5-.5zM0 10.5a.5.5 0 0 1 .5-.5h4A1.5 1.5 0 0 1 6 11.5v4a.5.5 0 0 1-1 0v-4a.5.5 0 0 0-.5-.5h-4a.5.5 0 0 1-.5-.5zm10 1a1.5 1.5 0 0 1 1.5-1.5h4a.5.5 0 0 1 0 1h-4a.5.5 0 0 0-.5.5v4a.5.5 0 0 1-1 0v-4z"/></svg>
                <svg id="input-fullscreen-button" @click="inputFullscreen = true" v-else-if="!$store.state.mobileVersion" xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="currentColor" class="bi bi-arrows-fullscreen" viewBox="0 0 16 16"><path fill-rule="evenodd" d="M5.828 10.172a.5.5 0 0 0-.707 0l-4.096 4.096V11.5a.5.5 0 0 0-1 0v3.975a.5.5 0 0 0 .5.5H4.5a.5.5 0 0 0 0-1H1.732l4.096-4.096a.5.5 0 0 0 0-.707zm4.344 0a.5.5 0 0 1 .707 0l4.096 4.096V11.5a.5.5 0 1 1 1 0v3.975a.5.5 0 0 1-.5.5H11.5a.5.5 0 0 1 0-1h2.768l-4.096-4.096a.5.5 0 0 1 0-.707zm0-4.344a.5.5 0 0 0 .707 0l4.096-4.096V4.5a.5.5 0 1 0 1 0V.525a.5.5 0 0 0-.5-.5H11.5a.5.5 0 0 0 0 1h2.768l-4.096 4.096a.5.5 0 0 0 0 .707zm-4.344 0a.5.5 0 0 1-.707 0L1.025 1.732V4.5a.5.5 0 0 1-1 0V.525a.5.5 0 0 1 .5-.5H4.5a.5.5 0 0 1 0 1H1.732l4.096 4.096a.5.5 0 0 1 0 .707z"/></svg>

                <textarea v-model="$store.state.currentPaste.content" @keydown="editor" class="input" id="content-input" placeholder="Paste in here"></textarea>
                <div id="options" :class="{'opened': optionsOpened}">
                    <h5 class="label">Password</h5>
                    <input autocomplete="new-password" v-model="$store.state.currentPaste.password" class="input" type="password" placeholder="Password (Optional)">
                    
                    <h5 class="label">CLIENT-ENCRYPTED</h5>
                    <label style="color: #FFF;" for="clientencrypted">Client-Encrypted</label>
                    <input type="checkbox" v-model="clientEncrypted" :readonly="$store.state.currentPaste.password != ''" name="clientencrypted">
                    <br><span style="color: #FFFFFF88" v-if="clientEncrypted">Client-Encryption deactivates the RAW function and some more. You can't open an encrypted paste without the password (If you set one) or the link.</span><br>

                    <h5 v-if="$store.state.user.logged_in" class="label">Folder</h5>
                    <select class="input" v-if="$store.state.user.logged_in" v-model="$store.state.currentPaste.folder">
                        <option selected value="">none</option>
                        <option v-for="(id, name) of folders" :key="id" :value="id">{{name}}</option>
                    </select>

                    <h5 v-if="$store.state.user.logged_in" class="label">Share to friend</h5>
                    <input v-if="$store.state.user.logged_in" autocomplete="off" v-model="$store.state.currentPaste.friends" class="input" type="text" placeholder="Friends (By username, split with ,)">
                    <div id="friend-list">
                        <a v-for="friend of friendList" :key="friend" @click="addFriendToList(friend)" :class='{selected: $store.state.currentPaste.friends.includes(friend)}'>{{friend}}</a>
                    </div>
                </div>
                <div id="buttons" :class="{mobile: $store.state.mobileVersion}">
                    <a id="submit-button" @click="send">SUBMIT</a>
                    <a id="settings-button" @click="optionsOpened = !optionsOpened">
                        <svg width="1em" height="1em" viewBox="0 0 16 16" class="bi bi-sliders" fill="currentColor" xmlns="http://www.w3.org/2000/svg"><path fill-rule="evenodd" d="M11.5 2a1.5 1.5 0 1 0 0 3 1.5 1.5 0 0 0 0-3zM9.05 3a2.5 2.5 0 0 1 4.9 0H16v1h-2.05a2.5 2.5 0 0 1-4.9 0H0V3h9.05zM4.5 7a1.5 1.5 0 1 0 0 3 1.5 1.5 0 0 0 0-3zM2.05 8a2.5 2.5 0 0 1 4.9 0H16v1H6.95a2.5 2.5 0 0 1-4.9 0H0V8h2.05zm9.45 4a1.5 1.5 0 1 0 0 3 1.5 1.5 0 0 0 0-3zm-2.45 1a2.5 2.5 0 0 1 4.9 0H16v1h-2.05a2.5 2.5 0 0 1-4.9 0H0v-1h9.05z"/></svg>
                    </a>
                </div>
                <LoadingSpinner v-if="loading" width="50px" height="50px" id="loading" />
            </div>
        </div>
        <div id="footer">
            <a target="_blank" href="https://github.com/interaapps/pastefy"><svg fill="currentColor" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24"><defs></defs><title>github</title><g id="Layer_2" data-name="Layer 2"><g id="github"><path class="cls-2" d="M16.24,22a1,1,0,0,1-1-1V18.4a2.15,2.15,0,0,0-.54-1.66,1,1,0,0,1,.61-1.67C17.75,14.78,20,14,20,9.77a4,4,0,0,0-.67-2.22,2.75,2.75,0,0,1-.41-2.06,3.71,3.71,0,0,0,0-1.41,7.65,7.65,0,0,0-2.09,1.09,1,1,0,0,1-.84.15,10.15,10.15,0,0,0-5.52,0,1,1,0,0,1-.84-.15A7.4,7.4,0,0,0,7.52,4.08a3.52,3.52,0,0,0,0,1.41,2.84,2.84,0,0,1-.43,2.08A4.07,4.07,0,0,0,6.42,9.8c0,3.89,1.88,4.93,4.7,5.29a1,1,0,0,1,.82.66,1,1,0,0,1-.21,1,2.06,2.06,0,0,0-.55,1.56V21a1,1,0,0,1-2,0v-.57a6,6,0,0,1-5.27-2.09,3.9,3.9,0,0,0-1.16-.88,1,1,0,1,1,.5-1.94,4.93,4.93,0,0,1,2,1.36c1,1,2,1.88,3.9,1.52h0a3.89,3.89,0,0,1,.23-1.58c-2.06-.52-5-2-5-7a6,6,0,0,1,1-3.33.85.85,0,0,0,.13-.62,5.69,5.69,0,0,1,.33-3.21,1,1,0,0,1,.63-.57c.34-.1,1.56-.3,3.87,1.2a12.16,12.16,0,0,1,5.69,0c2.31-1.5,3.53-1.31,3.86-1.2a1,1,0,0,1,.63.57,5.71,5.71,0,0,1,.33,3.22.75.75,0,0,0,.11.57,6,6,0,0,1,1,3.34c0,5.07-2.92,6.54-5,7a4.28,4.28,0,0,1,.22,1.67V21A1,1,0,0,1,16.24,22Z"/></g></g></svg></a>
            <a v-for="(item, key) of footer" :href="item" :key="key" ><span v-if="!isPWA()">{{key}}</span></a>
            <router-link to="/settings">SETTINGS</router-link>
        </div>
    </div>
</template>
<script>
import helper from "../helper.js";
import LoadingSpinner from "./LoadingSpinner.vue";
import CryptoJS from "crypto-js";

export default {
    data: ()=>({
        optionsOpened: false,
        folders: {},
        loading: false,
        clientEncrypted: false,
        friendList: [],
        footer: {
            IMPRINT: process.env.VUE_APP_API_IMPRINT_URL,
            PRIVACY: process.env.VUE_APP_API_PRIVACY_URL,
        },
        loginURL: process.env.VUE_APP_API_BASE + "/api/authentication/login",
        inputFullscreen: false
    }),
    created(){
        document.onkeyup = (e) => {
            if (e.ctrlKey && e.which == 66) {
                this.$store.state.app.fullscreen = true;
                e.preventDefault()
            }
        };

        this.pastefyAPI.get("/api/v2/user/folders", {show_children: false}).then(res=>{
            const folders = res.json()
            for (let folder of folders) {
                this.folders[folder.name] = folder.id
            }
        })

        try {
        if (localStorage["saved_contacts"] != null)
            this.friendList = JSON.parse(localStorage["saved_contacts"])
        } catch(e){
            this.friendList = []
        }
    },
    components: {
        LoadingSpinner
    },
    methods: {
        editor(event){
            const textarea = event.target
            
            if (event.keyCode == 9) {
                let newCaretPosition;
                newCaretPosition = textarea.getCaretPosition() + "    ".length;
                textarea.value = textarea.value.substring(0, textarea.getCaretPosition()) + "    " + textarea.value.substring(textarea.getCaretPosition(), textarea.value.length);
                textarea.setCaretPosition(newCaretPosition);
                event.preventDefault()
            }
            if(event.keyCode == 8){
                if (textarea.value.substring(textarea.getCaretPosition() - 4, textarea.getCaretPosition()) == "    ") { //it's a tab space
                    let newCaretPosition;
                    newCaretPosition = textarea.getCaretPosition() - 3;
                    textarea.value = textarea.value.substring(0, textarea.getCaretPosition() - 3) + textarea.value.substring(textarea.getCaretPosition(), textarea.value.length);
                    textarea.setCaretPosition(newCaretPosition);
                }
            }
            if(event.keyCode == 37){
                let newCaretPosition;
                if (textarea.value.substring(textarea.getCaretPosition() - 4, textarea.getCaretPosition()) == "    ") { //it's a tab space
                    newCaretPosition = textarea.getCaretPosition() - 3;
                    textarea.setCaretPosition(newCaretPosition);
                }    
            }
            if(event.keyCode == 39){
                let newCaretPosition;
                if (textarea.value.substring(textarea.getCaretPosition() + 4, textarea.getCaretPosition()) == "    ") { //it's a tab space
                    newCaretPosition = textarea.getCaretPosition() + 3;
                    textarea.setCaretPosition(newCaretPosition);
                }
            }
        },
        send(){
            let data = {
                content: this.$store.state.currentPaste.content,
                title: this.$store.state.currentPaste.title
            }
            
            if (this.$store.state.currentPaste.folder !== "")
                data.folder = this.$store.state.currentPaste.folder

            data.encrypted = false;
            let key;

            if (this.clientEncrypted || this.$store.state.currentPaste.password !== "") {
                key = this.$store.state.currentPaste.password === "" ? Math.random().toString(36).substring(5) : this.$store.state.currentPaste.password;

                data.content = CryptoJS.AES.encrypt(data.content, key).toString();
                data.title = CryptoJS.AES.encrypt(data.title, key).toString();
                data.encrypted = true;
            }

            const toast = helper.showSnackBar("Sending...")
            this.loading = true
            this.pastefyAPI.post("/api/v2/paste", data)
                .then(res=>{
                    const response = res.json()
                    if (response.success) {
                        const paste = response.paste
                        let date = new Date()
                        this.$store.state.app.lastPastes.unshift({
                            id: paste.id,
                            title: this.$store.state.currentPaste.title,
                            content: this.$store.state.currentPaste.content.substring(0, 50)+"...",
                            date: date.getMonth()+"/"+date.getDay()+"/"+date.getFullYear()
                        })
                        console.log(this.$store.state.app.created_pastes)
                        localStorage.setItem("created_pastes", JSON.stringify(this.$store.state.app.lastPastes))

                        let hash = "";
                        if (this.$store.state.currentPaste.password === "" && this.clientEncrypted)
                            hash = "#"+key


                        this.$router.push("/"+paste.id+hash)
                        this.$store.state.app.fullscreen = false
                        this.inputFullscreen = false
                        this.$store.state.currentPaste.content  = ""
                        this.$store.state.currentPaste.title    = ""
                        this.$store.state.currentPaste.password = ""
                        
                        helper.copyStringToClipboard(window.location.protocol+"//"+window.location.host+"/"+paste.id+hash)
                        toast.close()
                        helper.showSnackBar("Copied "+window.location.protocol+"//"+window.location.host+"/"+paste.id+hash+" to clipboard.")

                        this.shareToFriends(paste.id, this.$store.state.currentPaste.friends.split(","))
                        this.$store.state.currentPaste.friends = ""
                    } else  {
                        toast.close()
                        helper.showSnackBar("Error during posting the paste :(", "#EE4343")
                    }
                    this.loading = false
                }).catch(res=>{
                        toast.close()
                        helper.showSnackBar("Error during posting the paste :(", "#EE4343")
                        console.log(res)
                        this.loading = false
                })
        },
        isPWA(){
            return window.matchMedia('(display-mode: standalone)').matches;
        },
        addFriendToList(name){
            if (!this.$store.state.currentPaste.friends.includes(name)) {
                this.$store.state.currentPaste.friends += name+", "
            }
        },
        async shareToFriends(paste, friends){
            for (let friend of friends) {
                friend = friend.trim()
                if (friend.length > 3) {
                    const toast = helper.showSnackBar("Adding friend ("+friend+") to paste...")
                    let res = await this.pastefyAPI.post("/api/v2/paste/"+paste+"/friend", {
                        friend
                    }).then(res=>res.json())
                    toast.close()
                    if (res.success) {
                        helper.showSnackBar("Added friend ("+friend+") to paste!")
                        if (!this.friendList.includes(friend)) {
                            this.friendList.push(friend)
                            localStorage["saved_contacts"] = JSON.stringify(this.friendList)
                        }
                    } else
                        helper.showSnackBar("Couldn't add friend ("+friend+") to paste!", "#FF3232")
                }
            }
        }
    }
}
</script>
<style lang="scss" scoped>
    #sidenav{
        color: #FFF;
    }
    #logo {
        position: fixed;
        top: 16px;
        left: 27px;
        z-index: 1000;
        transition: 0.3s;
        img {
            height: 46px;
        }
        &.mobile {
            position: relative;
            left: 0px;
            img {
                margin: auto;
                display: block;
            }
        }
    }

    #profile-picture {
        float: right;
        margin-right: 14px;
        margin-top: 2px;
        border-radius: 50px;

        img {
            width: 40px;
            height: 40px;
            border-radius: 50px;
        }
    }

    #profile-picture.login {
        color: #FFFFFF;
        font-size: 17px;
        margin-top: 10.3px;
        text-decoration: none;
    }

    #sidebar {
        background: #212531;
        position: fixed;
        width: 380px;
        height: 100%;
        padding: 16px 27px;
        top: 0px;
        transition: 0.3s width ease-in-out;
        overflow-x: hidden;
        overflow-y: auto;
        #fullscreen-button {
            float: right;
            color: #ffffffce;
            height: 27px;
            width: 27px;
            margin-top: 10px;
            cursor: pointer;
            padding: 4px;
            transition: 0.5s;
            &:hover {
                background: #00000033;
                border-radius: 100px;
            }
        }

        #create-paste {
            position: relative;

            #content-input {
                height: 220px;
                font-size: 16px;
                white-space: pre;
                overflow-wrap: normal;
                overflow-x: scroll;
                font-family: "Roboto Sans", monospace;
                resize: vertical;
            }

            #title-input {
                font-size: 17px;
                margin-top: 65px;
            }
            
            #input-fullscreen-button {
                color: #FFFFFF53;
                width:  18px;
                height: 18px;
                position: absolute;
                right: 10px;
                top: 70px;
                cursor: pointer;
                transition: 0.3s color;
                &:hover {
                    color: #FFFFFFBB;    
                }
            }

            #buttons {
                margin-bottom: 31px;
                a {
                    display: inline-block;
                    border-radius: 7px;
                    padding: 10px;
                    color: #FFF;
                    text-align: center;
                    cursor: pointer;
                    transition: 0.3s background;
                }
                #submit-button {
                    background: #3469FF;
                    width: calc(100% - 54.8px);
                    margin-top: 10px;
                    margin-bottom: 60px;
                    &:hover {
                        background: #2c5de6;
                    }
                }

                #settings-button {
                    margin-left: 9.6px;
                    width: 45px;
                    background: #262B39;
                    svg {
                        vertical-align: middle;
                        margin-top: -4.2px;
                    }
                    &:hover {
                        background: #1c202b;
                    }
                }
            }

            #options {
                max-height: 0px;
                overflow: hidden;
                transition: 0.3s;
                .label {
                    font-size: 0px;
                    transition: 0.3s;
                }
                input {
                    max-height: 0px;
                    overflow: hidden;
                    transition: 0.3s;
                }
                &.opened {
                    max-height: 1000px;
                    input {
                        max-height: 100px;
                    }

                    .label {
                        font-size: 14px;
                    }
                }

                #friend-list {
                    background: #262B39;
                    border-radius: 7px;
                    padding: 2px;
                    max-height: 150px;
                    overflow: auto;
                    a {
                        display: block;
                        padding: 4px 7px;
                        color: #FFF;
                        border-radius: 5px;
                        &:hover {
                            background: #FFFFFF44;
                        }
                        cursor: pointer;
                        &.selected {
                            color: #FFFC;
                        }
                    }
                }
            }

            &.input-fullscreen {
                #content-input {
                    position: fixed;
                    left: 0px;
                    top: 0px;
                    height: 100%;
                    width: 100%;
                    padding-top: 75px;
                    padding-left: 26px;
                }
                #buttons #submit-button {
                    position: fixed;
                    bottom: 60px;
                    right: 60px;
                    margin-bottom: 0px;
                    width: 200px;
                }
                #input-fullscreen-button {
                    position: fixed;
                    right: 30px;
                    top: 30px;
                    z-index: 1000;
                    width:  23px;
                    height: 23px;
                }
            }
        }

        &.fullscreen {
            width: 100%;

            #fullscreen-button {
                transform: rotate(180deg);
            }

            #create-paste {
                #title-input {
                    margin-top: 100px;
                }

                #buttons {
                    width: 300px;
                    max-width: 100%;
                    float: right;
                    margin-right: -45px;
                    #submit-button {
                        width: 200px;
                        max-width: 100%;
                    }

                    &.mobile {
                        width: 100%;
                        float: none;
                        margin-right: 0px;
                        #submit-button {
                            width: calc(100% - 54.8px);
                        }

                    }
                }

                #content-input {
                    height: 70%;
                }

                &.input-fullscreen {
                    #content-input {
                        height: 100%;
                    }
                }
            }
        }

        &.hidden {
            width: 0px;
            display: none;
        }
    }
    #footer {
        position: fixed;
        bottom: 7px;
        left: 6px;

        a,a:visited {
            display: inline-block;
            color: #FFFFFF66;
            text-decoration: none;
            vertical-align: middle;
            margin: 5px 7px;
            svg {
                display: block;
                width: 29px;
                margin-right: 6px;
                margin-left: 3px;
                color: #FFFFFF88;
            }
        }
    }
</style>