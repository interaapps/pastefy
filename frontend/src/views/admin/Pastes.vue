<template>
    <div>
        <Header />
        <input v-model="search" class="input" placeholder="Search" @change="page = 1; loadPastes()" @keydown.enter="page = 1; loadPastes()" style="max-width: 840px">
        <div>
            <div id="pastes">
                <PasteCard v-for="paste of pastes" :key="paste.id" :paste="paste"/>

                <a @click="page -= 1; loadPastes()" class="button">PREVIOUS PAGE</a>
                <a @click="page += 1; loadPastes()" style="float: right;" class="button">NEXT PAGE</a>
            </div>
        </div>
    </div>
</template>

<script>
import Header from "@/components/admin/Header";
import PasteCard from "@/components/PasteCard";
import {buildSearchAndFilterQuery} from "@/helper";
export default {
    name: "Users",
    components: {PasteCard, Header},
    data: () => ({
        pastes: [],
        search: '',
        page: 1
    }),
    mounted() {
        this.search = this.$route.query.q || ''
        this.loadPastes()
    },
    methods: {
        async loadPastes() {
            this.pastes = await this.pastefyAPI.get("/api/v2/paste", {page: this.page, ...buildSearchAndFilterQuery(this.search)})
        }
    }
}
</script>