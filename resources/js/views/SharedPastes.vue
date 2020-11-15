<template>
    <div>
        <div id="shared">
            <i @click="load" class="material-icons" id="copy-button" :class="{loading: loading}">cached</i>
            <h3 style="margin-top: 20px; margin-bottom: 40px;">Shared Pastes</h3>
            <div id="pastes">
                <router-link v-for="paste of pastes" :to="'/'+paste.id"  :key="paste" class="paste">
                    <span class="date">{{paste.created}}</span>
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
import { Prajax } from "cajaxjs";
import helper from "../helper";
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
            Prajax.get("/sharedPasteList", {
                page: this.page
            })
                .then(res=>res.json())
                .then(res=>{
                    console.log(res);
                    if (res.done)
                        this.pastes = res.pastes
                    else
                        helper.showSnackBar("Error while loading.", "#FF3232")
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