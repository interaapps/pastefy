<template>
    <div v-if="!validPassword">
        <h4>Password:</h4><br><br>
        <input placeholder="Password" v-model="password" type="password" class="input">
        <a class="button" style="width: 100%;" @click="load($route.params.id)">ENTER PASTE</a><br>
        <a v-if="$store.state.user.id == userid" @click="deletePaste">DELETE</a>
    </div>
    <div v-else-if="!found">
        <div class="error">
            404! Paste not found
        </div>
    </div>
    <div v-else>
        
        <div id="action-buttons" :class="{mobile: $store.state.mobileVersion}">
            <a v-if="isPWA()" @click="copyURL">Copy URL</a>
            <a href="#preview" v-if="extraContent !== ''">PREVIEW</a>
            <a v-if="$store.state.user.id == userid" @click="deletePaste">DELETE</a>
            <a @click="$store.state.currentPaste.content = rawContent; $store.state.currentPaste.title = title" v-if="!$store.state.mobileVersion">FORK</a>
            <a :href="rawURL">RAW</a>
            <a id="copy-contents" @click="copy">
                <i class="material-icons" >content_copy</i>
            </a>
        </div>
        <h1>{{title}}<span class="language" v-if="language !== null">{{language}}</span></h1>
        <code id="paste-contents">
            <div id="line-nums">
                <a 
                    v-for="(line, lineNum) of rawContent.split('\n')" 
                    :key="lineNum" 
                    :href="'#ln-'+lineNum" 
                    :class='{selected: getUrlLineHash()=="#ln-"+lineNum}'>
                    {{lineNum+1}}
                </a>
            </div>
            <pre v-html="content" :style="{'white-space': this.language == 'text' ? 'break-spaces' : 'pre'}"></pre></code>
        <div id="preview" v-if="extraContent !== ''" v-html="extraContent"></div>
    </div>
</template>
<script>
import hljs from "highlight.js";
import helper from "../helper.js";
import CryptoJS from "crypto-js";

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
        found: true,
        rawURL: ""
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
            this.pastefyAPI.get("/api/v2/paste/"+id, data)
                .then(res=>{
                    let paste = res.json()
                    if (paste.exists) {
                        this.title = paste.title
                        this.rawContent = paste.content
                        this.rawURL = paste.raw_url;
                        this.userid = paste.user_id
                        
                        this.validPassword = false

                        if (paste.encrypted) {
                            let key = this.password;
                            if (window.location.hash != "") {
                                key = window.location.hash.split("#")[1];
                            }

                            if (key == "") {
                                this.passwordRequired = true
                                return;
                            }

                            this.title = CryptoJS.AES.decrypt(paste.title, key).toString(CryptoJS.enc.Utf8);
                            console.log(this.title)
                            this.rawContent = CryptoJS.AES.decrypt(paste.content, key).toString(CryptoJS.enc.Utf8);
                            if (this.rawContent === "")
                                this.validPassword = false
                            else
                                this.validPassword = true
                        } else if (paste.using_password) {
                            if (this.rawContent === '') {
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
                            "md": "markdown",
                            "txt": "text"
                        }

                        for (let replace  in replacements)
                            ending = ending.replace(replace, replacements[replace]);
                        
                        let languages = hljs.listLanguages();
                        languages.push("text")

                        if (languages.includes(ending)) {
                            this.language = ending;
                        }

                        if (this.language === null)
                            this.content = hljs.highlightAuto(this.rawContent).value
                        else {
                            if (this.language == 'text')
                                this.content = this.escapeHtml(this.rawContent)
                            else
                                this.content = hljs.highlight(this.language, this.rawContent).value

                            if (this.language === "markdown") {
                                const md = require('markdown-it')({
                                    html:         false,
                                    highlight: function (str, lang) {
                                        if (lang && hljs.getLanguage(lang)) {
                                            try {
                                                return hljs.highlight(lang, str).value;
                                            } catch (e) {
                                                //
                                            }
                                        }

                                        return str;
                                    }
                                });
                                this.extraContent = md.render(this.rawContent)
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
            const toast = helper.showSnackBar("Deleting...", "#ff9d34")
            this.pastefyAPI.delete("/api/v2/paste/"+this.$route.params.id)
                .then(res=>{
                    if (res.json().success) {
                        toast.close()
                        helper.showSnackBar("Deleted")
                        this.$router.push("/")
                    } else {
                        toast.close()
                        helper.showSnackBar("Couldn't delete paste", "#EE4343")
                    }
                })
        },
        escapeHtml(unsafe) {
            return unsafe
                .replace(/&/g, "&amp;")
                .replace(/</g, "&lt;")
                .replace(/>/g, "&gt;")
                .replace(/"/g, "&quot;")
                .replace(/'/g, "&#039;");
        },
        isPWA(){
            return window.matchMedia('(display-mode: standalone)').matches;
        },
        getUrlLineHash(){
            return window.location.hash
        }
    }
}
</script>
<style lang="scss" scoped>
    h1 {
        color: #FFF;
        font-size: 30px;
        margin-bottom: 30px;
        min-height: 30px;
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


    #paste-contents {
        font-size:16.8px;
        
        #line-nums {
            float: left;
            user-select: none;
            margin-right: 9px;

            a {
                display: block;
                text-decoration: none;
                color: #AAA;
                font-family: 'DM Mono', "Space Mono", monospace;
                &.selected {
                    color: #66d9ef;
                    background: #FFFFFF11;
                    padding: 0px 8px;
                    border-radius: 20px;
                    margin-left: -8px;
                    margin-right: -8px;
                    text-align: center;
                }
            }
        }
    }

    #preview {
        margin-top: 40px;
        padding: 26px;
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
<style lang="scss">
    #preview {

        font-size: 17px;
        h1, h2, h3, h4, h5 {
            margin-top: 19px;
            margin-bottom: 17px;
        }
        h1 {
            border-bottom: 2px solid #FFFFFF33;
            padding-bottom: 7px;
            padding-top: 7px;
        }
        img {
            max-width: 100%;
            border-radius: 7px;
            margin: 10px 0px;
        }

        blockquote {
            padding-left: 10px;
            border-left: 4px #00000077 solid;
            background: #FFFFFF09;
            margin: 20px 0px;
        }

        code {
            background: #FFFFFF11;
            padding: 0px 6px;
            border-radius: 5px;
        }

        pre code {
            background: none;
            padding: 0px;
            border-radius: 0px;
        }

        ul, ol {
            padding-left: 20px;
        }
    }
</style>