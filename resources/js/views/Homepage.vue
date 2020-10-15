<template>
    <div>
        <div v-if="$store.state.user.loggedIn">
            <h1>Welcome, {{$store.state.user.name}}</h1>
            <h2>Your pastes & folders</h2>
            <div id="folders">
                <a class="button" style="float: right; padding: 4px 16px">NEW</a>
                <h3 style="margin-top: 20px; margin-bottom: 40px;">Folder</h3>
                <router-link v-for="folder of folders" :to="'/folder/'+folder.id"  :key="folder" class="paste">
                    <span class="date">{{folder.crated}}</span>
                    <h3>{{folder.name}}</h3>
                </router-link>
            </div>
            <div id="pastes">
                <router-link to="/" class="button" style="float: right; padding: 4px 16px">NEW</router-link>
                <h3 style="margin-top: 20px; margin-bottom: 40px;">Pastes</h3>

                <router-link v-for="paste of pastes" :to="'/'+paste.id"  :key="paste" class="paste">
                    <span class="date">{{paste.created}}</span>
                    <h3>{{paste.title}}</h3>
                    <pre><code v-if="paste.encrypted==2">This paste can't be previewed. It's client-encrypted.</code><code v-else v-html="highlight(paste.content)"></code></pre>
                </router-link>
                <a @click="page -= 1; load()" class="button">PREVIOUS PAGE</a>
                <a @click="page += 1; load()" style="float: right;" class="button">NEXT PAGE</a>
            </div>
        </div>
        <div v-else>
            <h1>Welcome to Pastefy!</h1><br>
            <p>
                Pastefy is a code-paster. Just paste your code into the input-field on the left side and voilá!<br>
                If you want to see your created pastes <a href="/user/login" style="color: #FFF">Log in</a>.
            </p><br><br>
            <h2 style="margin-bottom: 10px">Your last created pastes</h2>
            <div id="pastes">
                <router-link v-for="paste of $store.state.app.lastPastes" :to="'/'+paste.id"  :key="paste" class="paste">
                    <span class="date">{{paste.date}}</span>
                    <h3>{{paste.title}}</h3>
                    <pre><code>{{paste.content}}</code></pre>
                </router-link>
            </div>
        </div>
    </div>
</template>
<script>
import { Prajax } from "cajaxjs";
import hljs from "highlight.js";
export default {
    data: ()=>({
        pastes: [],
        folders: [],
        page: 1
    }),
    created(){
        this.load()
    },
    methods: {
        highlight(content){
            return hljs.highlightAuto(content).value
        },
        load(){
            Prajax.get("/user/pastes", {page: this.page})
                .then(res=>res.json())
                .then(res=>{
                    if (res.done) {
                        this.pastes = res.pastes
                        this.folders = res.folder
                    }
                })
        }
    }
}
</script>
<style scoped lang="scss">
    
</style>