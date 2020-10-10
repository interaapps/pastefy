<template>
    <div>
        <router-link to="/" id="logo" :class="{'mobile': $store.state.mobileVersion && this.$route.path !== '/'}">
            <img src="/assets/images/logo.png">
        </router-link>
        <div id="sidebar" :class="{'fullscreen': $store.state.app.fullscreen || (($store.state.mobileVersion || $store.state.app.fullscreenOnHomepage) && this.$route.path === '/'), 'hidden': $store.state.mobileVersion && this.$route.path !== '/'}">
            <svg v-if="!($store.state.mobileVersion || ($store.state.app.fullscreenOnHomepage && this.$route.path === '/'))" id="fullscreen-button" @click="$store.state.app.fullscreen = !$store.state.app.fullscreen" width="1em" height="1em" viewBox="0 0 16 16" class="bi bi-chevron-right" fill="currentColor" xmlns="http://www.w3.org/2000/svg"><path fill-rule="evenodd" d="M4.646 1.646a.5.5 0 0 1 .708 0l6 6a.5.5 0 0 1 0 .708l-6 6a.5.5 0 0 1-.708-.708L10.293 8 4.646 2.354a.5.5 0 0 1 0-.708z"/></svg>
            <router-link id="profile-picture" to="/home" :style="{'margin-left': $store.state.mobileVersion ? '-15px' : '14px'}">
                <img :src="$store.state.user.profilePicture" :style="{border: $store.state.user.color+' 2px solid'}" v-if="$store.state.user.loggedIn">
            </router-link>
            <div v-if="$store.state.app.sideNavTab === 'paste'">
                <input autocomplete="off" v-model="$store.state.currentPaste.title" class="input" type="text" placeholder="Title" id="title-input">
                <textarea v-model="$store.state.currentPaste.content" @keydown="editor" class="input" id="content-input" placeholder="Paste in here"></textarea>
                <div id="options" :class="{'opened': optionsOpened}">
                    <h5 class="label">Password</h5>
                    <input autocomplete="new-password" v-model="$store.state.currentPaste.password" class="input" type="password" placeholder="Password (Optional)">
                    <h5 class="label">Folder</h5>
                    <select class="input" v-if="$store.state.user.loggedIn" v-model="$store.state.currentPaste.folder">
                        <option selected value="">none</option>
                        <option v-for="(id, name) of folders" :key="id" :value="id">{{name}}</option>
                    </select>
                </div>
                <div id="buttons">
                    <a id="submit-button" @click="send">SUBMIT</a>
                    <a id="settings-button" @click="optionsOpened = !optionsOpened">
                        <svg width="1em" height="1em" viewBox="0 0 16 16" class="bi bi-sliders" fill="currentColor" xmlns="http://www.w3.org/2000/svg"><path fill-rule="evenodd" d="M11.5 2a1.5 1.5 0 1 0 0 3 1.5 1.5 0 0 0 0-3zM9.05 3a2.5 2.5 0 0 1 4.9 0H16v1h-2.05a2.5 2.5 0 0 1-4.9 0H0V3h9.05zM4.5 7a1.5 1.5 0 1 0 0 3 1.5 1.5 0 0 0 0-3zM2.05 8a2.5 2.5 0 0 1 4.9 0H16v1H6.95a2.5 2.5 0 0 1-4.9 0H0V8h2.05zm9.45 4a1.5 1.5 0 1 0 0 3 1.5 1.5 0 0 0 0-3zm-2.45 1a2.5 2.5 0 0 1 4.9 0H16v1h-2.05a2.5 2.5 0 0 1-4.9 0H0v-1h9.05z"/></svg>
                    </a>
                </div>
            </div>
        </div>
        <div id="footer">
            <a v-if="!isPWA()" href="https://interaapps.de/imprint">IMPRINT</a>
            <a v-if="!isPWA()" href="https://interaapps.de/privacy">PRIVACY</a>
            <router-link to="/settings">SETTINGS</router-link>
        </div>
    </div>
</template>
<script>
import { Prajax } from "cajaxjs";
import helper from "../helper.js";

export default {
    data: ()=>({
        optionsOpened: false,
        folders: {}
    }),
    created(){
        document.onkeyup = (e) => {
            if (e.ctrlKey && e.which == 66) {
                this.$store.state.app.fullscreen = true;
                e.preventDefault()
            }
        };

        Prajax.get("/user/folder").then(res=>{
            this.folders = res.json()
        })
    },
    methods: {
        editor(event){
            const textarea = event.target

            if (event.keyCode == 9) {
                var newCaretPosition;
                newCaretPosition = textarea.getCaretPosition() + "    ".length;
                textarea.value = textarea.value.substring(0, textarea.getCaretPosition()) + "    " + textarea.value.substring(textarea.getCaretPosition(), textarea.value.length);
                textarea.setCaretPosition(newCaretPosition);
                event.preventDefault()
            }
            if(event.keyCode == 8){
                if (textarea.value.substring(textarea.getCaretPosition() - 4, textarea.getCaretPosition()) == "    ") { //it's a tab space
                    var newCaretPosition;
                    newCaretPosition = textarea.getCaretPosition() - 3;
                    textarea.value = textarea.value.substring(0, textarea.getCaretPosition() - 3) + textarea.value.substring(textarea.getCaretPosition(), textarea.value.length);
                    textarea.setCaretPosition(newCaretPosition);
                }
            }
            if(event.keyCode == 37){
                var newCaretPosition;
                if (textarea.value.substring(textarea.getCaretPosition() - 4, textarea.getCaretPosition()) == "    ") { //it's a tab space
                    newCaretPosition = textarea.getCaretPosition() - 3;
                    textarea.setCaretPosition(newCaretPosition);
                }    
            }
            if(event.keyCode == 39){
                var newCaretPosition;
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
            if (this.$store.state.currentPaste.password !== "")
                data.password = this.$store.state.currentPaste.password
            
            if (this.$store.state.currentPaste.folder !== "")
                data.folder = this.$store.state.currentPaste.folder
            helper.showSnackBar("Sending...")
            
            Prajax.post("/create:paste", data)
                .then(res=>{
                    const paste = res.json()
                    if (paste.success) {
                        
                        let date = new Date()
                        this.$store.state.app.lastPastes.unshift({
                            id: paste.id,
                            title: data.title,
                            content: data.content.substring(0, 50)+"...",
                            date: date.getMonth()+"/"+date.getDay()+"/"+date.getFullYear()
                        })
                        console.log(this.$store.state.app.created_pastes)
                        localStorage.setItem("created_pastes", JSON.stringify(this.$store.state.app.lastPastes))

                        this.$router.push("/"+paste.id)
                        this.$store.state.app.fullscreen = false
                        this.$store.state.currentPaste.content  = ""
                        this.$store.state.currentPaste.title    = ""
                        this.$store.state.currentPaste.password = ""
                        helper.copyStringToClipboard(window.location.protocol+"//"+window.location.host+"/"+paste.id)
                        helper.showSnackBar("Copied "+window.location.protocol+"//"+window.location.host+"/"+paste.id+" to clipboard.")
                    } else 
                        helper.showSnackBar("Error during posting the paste :(", "#EE4343")
                }).catch(res=>
                        helper.showSnackBar("Error during posting the paste :(", "#EE4343"))
        },
        isPWA(){
            return window.matchMedia('(display-mode: standalone)').matches;
        }
    }
}
</script>
<style lang="scss" scoped>
    #logo {
        position: fixed;
        top: 16px;
        left: 27px;
        z-index: 1000;
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
    #sidebar {
        background: #212531;
        position: fixed;
        width: 380px;
        height: 100%;
        padding: 16px 27px;
        top: 0px;
        transition: 0.3s width ease-in-out;
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

        #content-input {
            min-height: 160px;
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
        #buttons {
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
                &:hover {
                    background: #2c5de6;
                }
            }

            #settings-button {
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
        }

        &.fullscreen {
            width: 100%;

            #fullscreen-button {
                transform: rotate(180deg);
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
            }

            #content-input {
                height: 300px;
            }
        }

        &.hidden {
            width: 0px;
            display: none;
        }
    }
    #footer {
        position: fixed;
        bottom: 4px;
        left: 6px;

        a,a:visited {
            color: #FFFFFF66;
            text-decoration: none;
        }
    }
</style>