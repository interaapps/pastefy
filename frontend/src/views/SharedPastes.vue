<template>
    <div>
        <HomeHeader />
        <div id="shared">
            <i @click="load" class="material-icons" id="copy-button" :class="{loading: loading}">cached</i>
            <h2 style="margin-top: 20px; margin-bottom: 30px;">Shared Pastes</h2>
            <div id="pastes">
                <PasteCard v-for="paste of pastes" :key="paste.id" :paste="paste"/>
            </div>
            <a @click="page -= 1; load()" class="button">PREVIOUS PAGE</a>
            <a @click="page += 1; load()" style="float: right;" class="button">NEXT PAGE</a>
        </div>
    </div>
</template>

<script>
import hljs from "highlight.js";
import PasteCard from "../components/PasteCard.vue";
import HomeHeader from "@/components/HomeHeader.vue";

export default {
    data: () => ({
        pastes: [],
        loading: false,
        page: 1
    }),
    mounted() {
        this.load()
    },
    components: {HomeHeader, PasteCard},
    methods: {
        load() {
            this.loading = true
            this.pastefyAPI.get("/api/v2/user/sharedpastes", {
                page: this.page
            })
                .then(res => {
                    this.pastes = res
                    this.loading = false
                })
        },
        highlight(content) {
            return hljs.highlightAuto(content).value
        }
    }
}
</script>

<style lang="scss" scoped>
#shared {
    position: relative;
    max-width: 1000px;
}

#copy-button {
    position: absolute;
    right: 0px;
    cursor: pointer;
    font-size: 28px;

    &.loading {
        animation: rotate 1.1s infinite linear;
    }
}

@keyframes rotate {
    0% {
        transform: rotate(0deg);
    }
    50% {
        transform: rotate(180deg);
    }
    100% {
        transform: rotate(360deg);
    }
}
</style>