<template>
    <div>
        <HomeHeader />
        <WebsiteBanner paste="pb-homep" />
        <div v-if="$store.state.user.logged_in">

            <div id="folders">
                <h3 style="margin-top: 20px; margin-bottom: 20px; font-size: 24px">Folder</h3>

                <FolderCard v-for="folder of folders" :key="`folder-${folder.id}`" :folder="folder" />
                <NewFolderCard @created="loadFolders()" />
            </div>
            <div id="pastes">
                <a class="button" @click="$store.state.currentPaste.folder = null; $store.state.app.fullscreen = true"
                   style="float: right; padding: 4px 16px; margin-top: 4px">NEW</a>

                <input v-model="search" @change="loadPastes()" style="margin-top: 3px" placeholder="Search" type="text" class="small-search-input right">
                <h3 ref="pastesTitle" style="margin-top: 20px; margin-bottom: 20px; font-size: 24px">Pastes</h3>

                <PasteCard v-for="paste of pastes" :key="paste.id" :paste="paste"/>

                <a @click="changePage(-1)" class="button">PREVIOUS PAGE</a>
                <a @click="changePage(1)" style="float: right;" class="button">NEXT PAGE</a>
            </div>
        </div>
        <div v-else>
            <p>
                Pastefy is a code-paster. Just paste your code into the input-field on the left side and voilá!<br>
            </p><br><br>
            <h2 style="margin-bottom: 10px" v-if="$store.state.app.lastPastes.length">Your last created pastes</h2>
            <div id="pastes">
                <PasteCard v-for="paste of $store.state.app.lastPastes" :key="paste.id" :paste="paste" />
            </div>
        </div>
    </div>
</template>
<script>
import PasteCard from "../components/PasteCard.vue";
import FolderCard from "@/components/FolderCard.vue";
import NewFolderCard from "@/components/NewFolderCard.vue";
import HomeHeader from "@/components/HomeHeader.vue";
import WebsiteBanner from "@/components/WebsiteBanner.vue";

export default {
    data: () => ({
        pastes: [],
        folders: [],
        search: '',
        page: 1
    }),
    components: {WebsiteBanner, HomeHeader, NewFolderCard, FolderCard, PasteCard},
    created() {
        this.load()
    },
    methods: {
        async load() {
            this.loadPastes()
            this.loadFolders()
        },
        async loadPastes() {
            this.pastes = await this.pastefyAPI.get("/api/v2/user/pastes", {
                page: this.page,
                hide_children: true,
                search: this.search,
                shorten_content: true
            })
        },
        async loadFolders() {
            this.folders = await this.pastefyAPI.get("/api/v2/user/folders")
        },
        async changePage(p) {
            this.page += p;
            await this.loadPastes()
            window.scrollTo(0, this.$refs.pastesTitle.offsetTop - 30)
        }
    }
}
</script>
<style scoped lang="scss">

</style>