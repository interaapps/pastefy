<template>
    <div>
        <HomeHeader />
        <div id="public">

            <input @input="loadSearch()" v-model="search" type="text" placeholder="Search" class="input" v-animate-css="{classes: 'fadeIn', delay: 0}">

            <div v-if="search !== ''" :key="`search-${search}`">

                <h2 style="margin-top: 20px; margin-bottom: 10px;">Search Results</h2>
                <div id="pastes">
                    <PasteCard v-for="paste of searchResults" :key="`trending-${paste.id}`" :paste="paste"/>

                    <a @click="changeSearchPage(-1)" class="button">PREVIOUS PAGE</a>
                    <a @click="changeSearchPage(1)" style="float: right;" class="button">NEXT PAGE</a>
                </div>
            </div>
            <div v-else>
                <WebsiteBanner paste="pb-trend" />

                <div v-animate-css="{classes: 'fadeIn', delay: 50}">
                    <h2 style="margin-top: 20px; margin-bottom: 10px;">Trending</h2>
                    <div id="pastes">
                        <PasteCard v-for="paste of trendingPastes" :key="`trending-${paste.id}`" :paste="paste"/>
                    </div>
                </div>

                <div v-animate-css="{classes: 'fadeIn', delay: 100}">
                    <h2 style="margin-top: 40px; margin-bottom: 10px;">Latest</h2>
                    <div id="pastes">
                        <PasteCard v-for="paste of newPastes" :key="`new-${paste.id}`" :paste="paste"/>
                    </div>
                </div>

                <div v-animate-css="{classes: 'fadeIn', delay: 150}">
                    <h2 style="margin-top: 40px; margin-bottom: 10px;">Alltime Trending</h2>
                    <div id="pastes">
                        <PasteCard v-for="paste of allTimeTrendingPastes" :key="`alt-trending-${paste.id}`" :paste="paste"/>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>

<script>
import PasteCard from "../components/PasteCard.vue";
import HomeHeader from "@/components/HomeHeader.vue";
import WebsiteBanner from "@/components/WebsiteBanner.vue";

export default {
    data: () => ({
        searchResults: [],
        trendingPastes: [],
        allTimeTrendingPastes: [],
        newPastes: [],
        loading: false,
        search: '',
        searchPage: 1
    }),
    mounted() {
        this.load()
    },
    components: {WebsiteBanner, HomeHeader, PasteCard},
    methods: {
        async load() {
            this.loading = true

            this.trendingPastes = await this.pastefyAPI.get("/api/v2/public-pastes/trending", {trending: 'true', page_limit: 5, shorten_content: true})
            this.newPastes = await this.pastefyAPI.get("/api/v2/public-pastes/latest", {page_limit: 5, shorten_content: true})
            this.allTimeTrendingPastes = await this.pastefyAPI.get("/api/v2/public-pastes/trending", {page_limit: 5, shorten_content: true})
        },
        async loadSearch() {
            this.searchResults = await this.pastefyAPI.get("/api/v2/public-pastes/latest", {
                search: this.search,
                page: this.searchPage,
                page_limit: 10,
                shorten_content: true
            })
        },
        async changeSearchPage(p) {
            this.searchPage += p;
            await this.loadSearch()
            window.scrollTo(0, 0)
        }
    }
}
</script>

<style lang="scss" scoped>
#public {
    position: relative;
    max-width: 1000px;
}
</style>