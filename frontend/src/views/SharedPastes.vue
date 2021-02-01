<template>
    <div>
        <div id="shared">
            <i @click="load" class="material-icons" id="copy-button" :class="{loading: loading}">cached</i>
            <h3 style="margin-top: 20px; margin-bottom: 40px;">Shared Pastes</h3>
            <div id="pastes">
                <router-link v-for="paste of pastes" :to="'/'+paste.id"  :key="paste.id" class="paste">
                    <span class="date">{{paste.created_at}}</span>
                    <h3>{{paste.title}}</h3>
                    <pre><code v-html="highlight(paste.content)"></code></pre>
                </router-link>
            </div>
            <a @click="page -= 1; load()" class="button">PREVIOUS PAGE</a>
            <a @click="page += 1; load()" style="float: right;" class="button">NEXT PAGE</a>
        </div>
    </div>
</template>

<script>
import hljs from "highlight.js";

export default {
    data: ()=>({
        pastes: [],
        loading: false,
        page: 1
    }),
    mounted(){
        this.load()
    },
    methods:{
        load(){
            this.loading = true
            this.pastefyAPI.get("/api/v2/user/sharedpastes", {
                page: this.page
            })
                .then(res=>res.json())
                .then(res=>{
                    this.pastes = res
                    this.loading = false
                })
        },
        highlight(content){
            return hljs.highlightAuto(content).value
        }
    }
}
</script>

<style lang="scss" scoped>
    #shared {
        position: relative;
        max-width: 840px;
    }
    #copy-button {
        position: absolute;
        right: 0px;
        cursor: pointer;
        font-size: 28px;
        &.loading{
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