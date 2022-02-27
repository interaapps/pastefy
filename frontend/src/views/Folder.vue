<template>
    <div>
        <div style="max-width: 840px;">
            <div id="action-buttons">
                <a v-if="isPWA()" @click="copyURL">Copy URL</a>
                <a @click="deleteFolder">DELETE</a>
            </div>
            <h1>{{title}}</h1>
            <a class="button" style="float: right; padding: 4px 16px" @click="addFolderInput = !addFolderInput">NEW</a>
            <h3 style="margin-top: 20px; margin-bottom: 40px;">Folder</h3>
            <div v-if="addFolderInput" style="margin-bottom: 20px">
                <input type="text" v-model="folderName" class="input" placeholder="name">
                <a class="button" style="width: 49%; margin-right: 1%; background: var(--obj-background-color-hover)" @click="addFolderInput = false">CANCEL</a>
                <a class="button" style="width: 50%" @click="createFolder">ADD</a>
            </div>
            <div id="folders">
                <router-link v-for="folder of folders" :to="'/folder/'+folder.id"  :key="folder.id" class="paste">
                    <span class="date">{{folder.crated}}</span>
                    <h3>{{folder.name}}</h3>
                </router-link>
            </div>
            <a class="button" style="float: right; padding: 4px 16px" @click="$store.state.currentPaste.folder = id; $store.state.app.fullscreen = true">NEW</a>
            <h3 style="margin-top: 20px; margin-bottom: 40px;">Pastes</h3>
            <div id="pastes">
                <PasteCard v-for="paste of pastes" :key="paste.id" :paste="paste" />

            </div>
        </div>
    </div>
</template>
<script>
import hljs from "highlight.js";
import helper from "../helper.js";
import PasteCard from "../components/PasteCard.vue";

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
    components: {PasteCard},
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
