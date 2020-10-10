<template>
    <div>
            <div id="action-buttons">
                <a v-if="isPWA()" @click="copyURL">Copy URL</a>
                <a @click="deleteFolder">DELETE</a>
            </div>
            <h1>{{title}}</h1>
            <h3 style="margin-top: 20px; margin-bottom: 40px;">Folder</h3>
            <div id="folders">
                <router-link v-for="folder of folders" :to="'/folder/'+folder.id"  :key="folder" class="paste">
                    <span class="date">{{folder.crated}}</span>
                    <h3>{{folder.name}}</h3>
                </router-link>
            </div>
            <h3 style="margin-top: 20px; margin-bottom: 40px;">Pastes</h3>
            <div id="pastes">
                <router-link v-for="paste of pastes" :to="'/'+paste.id"  :key="paste" class="paste">
                    <span class="date">{{paste.created}}</span>
                    <h3>{{paste.title}}</h3>
                    <pre><code v-html="highlight(paste.content)"></code></pre>
                </router-link>
            </div>
    </div>
</template>
<script>
import hljs from "highlight.js";
import { Prajax } from "cajaxjs";
import helper from "../helper.js";

export default {
    data: function(){return{
        id: this.$route.params.id,
        isMine: false,
        pastes: [],
        folders: [],
        title: "Loading..."
    }},
    mounted(){
        this.load(this.$route.params.id)
    },
    methods: {
        load(id){
            Prajax.get("/api/v1/folder/"+id)
                .then(res=>res.json())
                .then(res=>{
                    if (res.done) {
                        this.id = res.id
                        this.pastes = res.pastes
                        this.folders = res.folder
                        this.isMine = res.myfolder
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
            Prajax.delete("/api/v1/folder/"+this.id)
                .then(res=>{
                    if (res.json().done) {
                        helper.showSnackBar("Deleted")
                        this.$router.push("/")
                    } else
                        helper.showSnackBar("Couldn't delete paste", "#EE4343")
                })
        },
        isPWA(){
            return window.matchMedia('(display-mode: standalone)').matches;
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