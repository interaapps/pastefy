<template>
    <div>
        <div id="action-buttons">
            <a v-if="isPWA()" @click="copyURL">Copy URL</a>
            <a @click="deleteFolder">DELETE</a>
        </div>
        <h1>{{title}}</h1>
        <a class="button" style="float: right; padding: 4px 16px" @click="addFolderInput = !addFolderInput">NEW</a>
        <h3 style="margin-top: 20px; margin-bottom: 40px;">Folder</h3>
        <div v-if="addFolderInput" style="margin-bottom: 20px">
            <input type="text" v-model="folderName" class="input" placeholder="name">
            <a class="button" style="width: 49%; margin-right: 1%; background: #FFFFFF09" @click="addFolderInput = false">CANCLE</a>
            <a class="button" style="width: 50%" @click="createFolder">ADD</a>
        </div>
        <div id="folders">
            <router-link v-for="folder of folders" :to="'/folder/'+folder.id"  :key="folder" class="paste">
                <span class="date">{{folder.crated}}</span>
                <h3>{{folder.name}}</h3>
            </router-link>
        </div>
        <h3 style="margin-top: 20px; margin-bottom: 40px;">Pastes</h3>
        <div id="pastes">
            <router-link v-for="paste of pastes" :to="'/'+paste.id"  :key="paste.id" class="paste">
                <span class="date">{{paste.created_at}}</span>
                <h4 v-if="paste.type == 'MULTI_PASTE'" style="float: right; font-weight: 500; padding: 0px 10px; margin-bottom: 10px;background: #FFFFFF11; border-radius: 100px; display: inline-block">MULTI</h4>
                <h3 v-if="!paste.encrypted">{{paste.title}}</h3>
                <pre><code v-if="paste.encrypted">This paste can't be previewed. It's client-encrypted.</code><code v-else-if="paste.type != 'MULTI_PASTE'" v-html="highlight(paste.content)"></code></pre>
            </router-link>
        </div>
    </div>
</template>
<script>
import hljs from "highlight.js";
import helper from "../helper.js";

export default {
    data: function(){return{
        id: this.$route.params.id,
        isMine: false,
        pastes: [],
        folders: [],
        title: "Loading...",
        addFolderInput: false,
        folderName: ""
    }},
    mounted(){
        this.load(this.$route.params.id)
    },
    methods: {
        load(id){
            this.pastefyAPI.get("/api/v2/folder/"+id, {hide_children: true})
                .then(res=>{
                    if (res.exists) {
                        this.id = res.id
                        this.pastes = res.pastes
                        this.folders = res.children
                        this.isMine = res.user_id == this.$store.state.user.id
                        this.title = res.name
                    }
                })
        },
        highlight(content){
            return hljs.highlightAuto(content).value
        },
        copyURL(){
            helper.copyStringToClipboard(window.location.href)
            helper.showSnackBar("Copied")
        },
        deleteFolder(){
            helper.showSnackBar("Deleting...", "#ff9d34")
            this.pastefyAPI.delete("/api/v2/folder/"+this.id)
                .then(res=>{
                    if (res.success) {
                        helper.showSnackBar("Deleted")
                        this.$router.push("/")
                    } else
                        helper.showSnackBar("Couldn't delete folder", "#EE4343")
                })
        },
        isPWA(){
            return window.matchMedia('(display-mode: standalone)').matches;
        },
        createFolder(){
            this.pastefyAPI.post("/api/v2/folder", {
                name: this.folderName,
                parent: this.id
            }).then(()=>{
                this.load(this.id)
                this.folderName = ""
                this.addFolderInput = false
            })
        }
    },
    beforeRouteUpdate (to, from, next) {
        this.password = ""
        this.load(to.params.id)
        next()
    },
}
</script>
<style lang="scss" scoped>
    
</style>