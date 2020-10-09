<template>
    <div v-if="!validPassword">
        <h4>Password:</h4><br><br>
        <input placeholder="Password" v-model="password" type="password" class="input">
        <a class="button" @click="load($route.params.id)">ENTER PASTE</a>
    </div>
    <div v-else>
        
        <div id="action-buttons">
            <a @click="$store.state.currentPaste.content = rawContent; $store.state.currentPaste.title = title">FORK</a>
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
        validPassword: true
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
                    }
                })
        },
        copy(){
            helper.copyStringToClipboard(this.rawContent)
            helper.showSnackBar("Copied")
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

    #action-buttons {
        float: right;
        cursor: default;
        a,
        a:visited {
            margin-left: 20px;
            color: #FFF;
            text-decoration: none;
            vertical-align: middle;
            margin-top: 10px;
            line-height: 45px;
            font-size: 18px;
            cursor: pointer;
            i {
                vertical-align: middle;
            }
        }
    }

    .language {
        margin-left: 10px;
        background: #FFFFFF32;
        padding: 1px 7px;
        user-select: none;
        display: inline-block;
        border-radius: 7px;
    }
</style>