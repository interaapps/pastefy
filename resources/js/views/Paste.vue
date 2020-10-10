<template>
    <div v-if="!validPassword">
        <h4>Password:</h4><br><br>
        <input placeholder="Password" v-model="password" type="password" class="input">
        <a class="button" style="width: 100%;" @click="load($route.params.id)">ENTER PASTE</a>
    </div>
    <div v-else-if="!found">
        <div class="error">
            404! Paste not found
        </div>
    </div>
    <div v-else>
        
        <div id="action-buttons" :class="{mobile: $store.state.mobileVersion}">
            <a v-if="isPWA()" @click="copyURL">Copy URL</a>
            <a v-if="$store.state.user.id == userid" @click="deletePaste">DELETE</a>
            <a @click="$store.state.currentPaste.content = rawContent; $store.state.currentPaste.title = title" v-if="!$store.state.mobileVersion">FORK</a>
            <a :href="'/'+$route.params.id+'/raw'+(password===''?'':'?password='+password)">RAW</a>
            <a id="copy-contents" @click="copy">
                <i class="material-icons" >content_copy</i>
            </a>
        </div>
        <h1>{{title}}<span class="language" v-if="language !== null">{{language}}</span></h1>
        <code id="paste-contents"><pre v-html="content"></pre></code>
        <div id="preview" v-if="extraContent !== ''" v-html="extraContent"></div>
    </div>
</template>
<script>
import { Prajax } from "cajaxjs";
import hljs from "highlight.js";
import helper from "../helper.js";
export default {
    data: ()=>({
        title: "Title",
        content: "Loading...",
        rawContent: "Loading...",
        language: null,
        extraContent: "",
        password: "",
        passwordRequired: false,
        validPassword: true,
        userid: -2,
        found: true
    }),
    mounted(){
        this.load(this.$route.params.id)
        this.password = ""
    },	
    beforeRouteUpdate (to, from, next) {
        this.password = ""
        this.load(to.params.id)
        next()
    },
    methods: {
        load(id){
            let data = {}
            if (this.password !== "" && this.passwordRequired)
                data.password = this.password
            Prajax.get("/api/v1/get/"+id, data)
                .then(res=>{
                    let paste = res.json()
                    if (paste.exists) {
                        this.title = paste.title
                        this.rawContent = paste.content
                        this.passwordRequired = paste.using_password
                        this.userid = paste.userid
                        
                        this.validPassword = false

                        if (paste.using_password) {
                            if (paste.content === '') {
                                if (this.password !== "" ) {
                                    helper.showSnackBar("Invalid Password", "#EE4343")
                                }
                            } else
                                this.validPassword = true
                        } else
                            this.validPassword = true
                            
                        
                        const pasteTitleComponents = this.title.split(".");
                        let ending = pasteTitleComponents[pasteTitleComponents.length-1];
                        const replacements = {
                            "js": "javascript",
                            "md": "markdown",
                            "sh": "shell",
                            "html": "xml",
                            "htaccess": "apache",
                            "c": "objectivec",
                            "hack": "php",
                            "coffee": "coffeescript",
                            "c++": "cpp",
                            "kotlin": "java",
                            "kt": "java"
                        }

                        for (let replace  in replacements)
                            ending = ending.replace(replace, replacements[replace]);
                        
                        if (hljs.listLanguages().includes(ending)) {
                            this.language = ending;
                        }

                        if (this.language === null)
                            this.content = hljs.highlightAuto(paste.content).value
                        else {
                            this.content = hljs.highlight(this.language, paste.content).value

                            if (this.language === "markdown") {
                                const md = require('markdown-it')({
                                    html:         false,
                                    highlight: function (str, lang) {
                                        if (lang && hljs.getLanguage(lang)) {
                                            try {
                                                return hljs.highlight(lang, str).value;
                                            } catch (e) {}
                                        }

                                        return str;
                                    }
                                });
                                this.extraContent = md.render(paste.content)
                            }
                        }

                        console.log(this.language)
                    } else 
                        this.found = false
                })
        },
        copy(){
            helper.copyStringToClipboard(this.rawContent)
            helper.showSnackBar("Copied")
        },
        copyURL(){
            helper.copyStringToClipboard(window.location.href)
            helper.showSnackBar("Copied")
        },
        deletePaste(){
            helper.showSnackBar("Deleting...", "#ff9d34")
            Prajax.delete("/api/v1/paste/"+this.$route.params.id)
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
    }
}
</script>
<style lang="scss" scoped>
    h1 {
        color: #FFF;
        font-size: 30px;
        margin-bottom: 30px;
    }

    #paste-contents,
    #preview {
        display: block;
        color: #FFF;
        background: #262B39;
        border-radius: 7px;
        padding: 10px;
        overflow-x: auto;
    }

    #preview {
        margin-top: 40px;
    }

    .language {
        margin-left: 10px;
        background: #FFFFFF32;
        padding: 1px 7px;
        user-select: none;
        display: inline-block;
        border-radius: 7px;
    }

    #action-buttons.mobile {
        a {
            background: #3469FF;
            padding: 0px 26px;
            border-radius: 100px;
            position: fixed;
            bottom: 20px;
            display: inline-block;
            right: 20px;
        }

        a+a { bottom: 72px; }
        a+a+a { bottom: 124px; }
        a+a+a+a { bottom: 176px; }
        
        #copy-contents {
            padding: 13px;
            margin-top: -3px;
            position: static;
            i {
                display: block;
            }
        }
    }
</style>