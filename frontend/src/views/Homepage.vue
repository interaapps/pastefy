<template>
    <div>
        <div v-if="$store.state.user.logged_in">
            <h1>Welcome, {{$store.state.user.name}}</h1>
            <h2>Your pastes & folders. <router-link to="/shared" v-if="$store.state.user.auth_type == 'interaapps'">Shared pastes</router-link></h2>
            <div id="folders">
                <a class="button" style="float: right; padding: 4px 16px" @click="addFolderInput = !addFolderInput">NEW</a>
                <h3 style="margin-top: 20px; margin-bottom: 40px; font-size: 24px">Folder</h3>
                <div v-if="addFolderInput" style="margin-bottom: 20px">
                    <input type="text" v-model="folderName" class="input" placeholder="name">
                    <a class="button" style="width: 49%; margin-right: 1%; background: #FFFFFF09" @click="addFolderInput = false">CANCLE</a>
                    <a class="button" style="width: 50%" @click="createFolder">ADD</a>
                </div>
                <router-link v-for="folder of folders" :to="'/folder/'+folder.id"  :key="folder.id" class="paste">
                    <span class="date">{{folder.crated}}</span>
                    <h3>{{folder.name}}</h3>
                </router-link>
            </div>
            <div id="pastes">
                <a class="button" @click="$store.state.currentPaste.folder = null; $store.state.app.fullscreen = true" style="float: right; padding: 4px 16px">NEW</a>
                <h3 style="margin-top: 20px; margin-bottom: 40px; font-size: 24px">Pastes</h3>
                
                <PasteCard v-for="paste of pastes" :key="paste.id" :paste="paste" />
                
                <a @click="page -= 1; load()" class="button">PREVIOUS PAGE</a>
                <a @click="page += 1; load()" style="float: right;" class="button">NEXT PAGE</a>
            </div>
        </div>
        <div v-else>
            <h1>Welcome to Pastefy!</h1><br>
            <p>
                Pastefy is a code-paster. Just paste your code into the input-field on the left side and voilá!<br>
            </p><br><br>
            <h2 style="margin-bottom: 10px">Your last created pastes</h2>
            <div id="pastes">
                <router-link v-for="paste of $store.state.app.lastPastes" :to="'/'+paste.id"  :key="paste.id" class="paste">
                    <span class="date">{{paste.date}}</span>
                    <h3>{{paste.title}}</h3>
                    <pre><code>{{paste.content}}</code></pre>
                </router-link>
            </div>
        </div>
    </div>
</template>
<script>
import PasteCard from "../components/PasteCard.vue";
export default {
    data: ()=>({
        pastes: [],
        folders: [],
        page: 1,
        addFolderInput: false,
        folderName: ""
    }),
    components: {PasteCard},
    created(){
        this.load()
    },
    methods: {
        load(){
            this.pastefyAPI.get("/api/v2/user/overview", {page: this.page, hide_children: true})
                .then(res=>{
                    this.pastes = res.pastes
                    this.folders = res.folder
                })
        },
        createFolder(){
            this.pastefyAPI.post("/api/v2/folder", {
                name: this.folderName
            }).then(()=>{
                this.load()
                this.folderName = ""
                this.addFolderInput = false
            })
        }
    }
}
</script>
<style scoped lang="scss">
    
</style>