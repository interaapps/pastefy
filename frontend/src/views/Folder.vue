<template>
    <div>
        <div style="max-width: 1000px;">
            <div id="action-buttons">
                <a v-if="isPWA()" @click="copyURL">Copy URL</a>
                <a v-if="isMine" @click="$refs.deleteFolderConfirmation.open()" class="button delete" style="line-height: 20px">DELETE</a>
            </div>
            <h1>{{ title }}</h1>


            <h2 style="margin-top: 20px; margin-bottom: 40px" v-if="folders.length > 0 || isMine">Folder</h2>

            <FolderCard v-for="folder of folders" :key="`folder-${folder.id}`" :folder="folder" />
            <NewFolderCard v-if="isMine" :parent="id" @created="load($route.params.id)" />

            <div>

            <a  v-if="isMine" class="button" style="float: right; padding: 4px 16px" @click="$store.state.currentPaste.folder = id; $store.state.app.fullscreen = true">NEW</a>
            <h2 style="margin-top: 20px; margin-bottom: 40px;">Pastes</h2>
            <div id="pastes">
                <PasteCard v-for="paste of pastes" :key="paste.id" :paste="paste"/>
            </div>
            </div>
        </div>

        <ConfirmationModal title="Delete folder?" ref="deleteFolderConfirmation" @confirm="deleteFolder">
            Do you really want to delete this folder?
        </ConfirmationModal>
    </div>
</template>
<script>
import hljs from "highlight.js";
import helper from "../helper.js";
import PasteCard from "../components/PasteCard.vue";
import ConfirmationModal from "@/components/ConfirmationModal.vue";
import NewFolderCard from "@/components/NewFolderCard.vue";
import FolderCard from "@/components/FolderCard.vue";

export default {
    data: function () {
        return {
            id: this.$route.params.id,
            isMine: false,
            pastes: [],
            folders: [],
            title: "Loading...",
            addFolderInput: false,
            folderName: ""
        }
    },
    mounted() {
        this.load(this.$route.params.id)
    },
    components: {FolderCard, NewFolderCard, ConfirmationModal, PasteCard},
    methods: {
        load(id) {
            this.pastefyAPI.get("/api/v2/folder/" + id, {
                hide_children: true,
                shorten_content: true
            })
                .then(res => {
                    if (res.exists) {
                        this.id = res.id
                        this.pastes = res.pastes
                        this.folders = res.children
                        this.isMine = res.user_id == this.$store.state.user.id
                        this.title = res.name
                    }
                })
        },
        highlight(content) {
            return hljs.highlightAuto(content).value
        },
        copyURL() {
            helper.copyStringToClipboard(window.location.href)
            helper.showSnackBar("Copied")
        },
        deleteFolder() {
            helper.showSnackBar("Deleting...", "#ff9d34")
            this.pastefyAPI.delete("/api/v2/folder/" + this.id)
                .then(res => {
                    if (res.success) {
                        helper.showSnackBar("Deleted")
                        this.$router.push("/")
                    } else
                        helper.showSnackBar("Couldn't delete folder", "#EE4343")
                })
        },
        isPWA() {
            return window.matchMedia('(display-mode: standalone)').matches;
        },
        createFolder() {
            this.pastefyAPI.post("/api/v2/folder", {
                name: this.folderName,
                parent: this.id
            }).then(() => {
                this.load(this.id)
                this.folderName = ""
                this.addFolderInput = false
            })
        }
    },
    beforeRouteUpdate(to, from, next) {
        this.password = ""
        this.load(to.params.id)
        next()
    },
}
</script>
<style lang="scss" scoped>

</style>
