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
    <div v-else-if="$store.state.appInfo.login_required_for_read && !$store.state.user.logged_in">
        <h2>Please log in to see this paste.</h2>
    </div>
    <div v-else>

        <div id="action-buttons" :class="{mobile: $store.state.mobileVersion}">
            <a v-if="isPWA()" @click="copyURL">Copy URL</a>
            <a v-if="language == 'json'" @click="jsonPretty">{{ jsonPrettified ? 'UNPRETTY' : 'PRETTY' }}</a>
            <a href="#paste-contents" @click="readCode = true" v-if="extraContent !== ''">CODE</a>
            <a v-if="$store.state.user.logged_in && ($store.state.user.id == userid || $store.state.user.type === 'ADMIN')"
               @click="deletePaste">DELETE</a>
            <a @click="editPaste(true)" v-if="!$store.state.mobileVersion">FORK</a>
            <a v-if="$store.state.user.logged_in && $store.state.user.id == userid" @click="editPaste()">EDIT</a>
            <a :href="rawURL" v-if="!passwordRequired && !multiPastes">RAW</a>
            <a id="copy-contents" @click="copy">
                <i class="material-icons">content_copy</i>
            </a>
        </div>
        <h1>{{ title }}<span class="language"
                             v-if="language !== null && language == 'markdown' && !multiPastes">{{ language }}</span>
        </h1>
        <div id="tabs" v-if="multiPastes != null && Object.keys(multiPastes).length > 1">
            <a v-for="(tab,i) of multiPastes" :key="i" @click="changeTab(i)"
               :class="{selected: multiPastesSelected==i}">
                {{ tab.name }}
            </a>
        </div>
        <div id="preview" v-if="extraContent !== ''" v-html="extraContent"></div>
        <template v-if="extraContent == '' || readCode">
            <h1 v-if="extraContent !== ''" style="margin-top: 30px;">
                {{ language == 'markdown' ? 'Markdown ' : '' }}Code</h1>
            <code id="paste-contents">
                <div id="line-nums" v-if="showLineNums">
                    <a
                        v-for="(line, lineNum) of rawContent.split('\n')"
                        :key="lineNum"
                        :href="'#ln-'+lineNum"
                        :class='{selected: getUrlLineHash()=="#ln-"+lineNum}'>
                        {{ lineNum + 1 }}
                    </a>
                </div>
                <pre v-html="content" :style="{'white-space': this.language == 'text' ? 'break-spaces' : 'pre'}"></pre>
            </code>

        </template>
        <template v-if="htmlPreview != ''">
            <a class="button gray" v-if="!htmlPreviewEnabled" @click="htmlPreviewEnabled = !htmlPreviewEnabled">
                Show Preview
            </a>
            <div id="html-preview" :class="['lang-'+language]" v-else>
                <iframe
                    sandbox="allow-forms allow-modals allow-pointer-lock allow-popups allow-presentation allow-scripts allow-top-navigation-by-user-activation"
                    :srcdoc="htmlPreview"/>
                <div>
                    <i class="material-icons" @click="reloadHTMLPreview()">refresh</i>
                    <i class="material-icons" @click="htmlPreviewEnabled = false">close</i>
                </div>
            </div>
        </template>
    </div>
</template>
<script>
import hljs from "highlight.js";
import helper from "../helper.js";
import CryptoJS from "crypto-js";
import LANGUAGE_REPLACEMENTS from '../assets/data/langReplacements'
import {currentThemeVars} from "@/main";


let languages = [...hljs.listLanguages(), "text"];

export default {
    data: () => ({
        title: "Title",
        content: "Loading...",
        rawContent: "Loading...",
        language: null,
        extraContent: "",

        htmlPreview: "",
        htmlPreviewEnabled: false,

        password: "",
        passwordRequired: false,
        validPassword: true,
        userid: -2,
        found: true,
        rawURL: "",
        showLineNums: true,
        paste: {},

        currentPasteContents: "",

        jsonPrettified: false,

        multiPastes: null,
        multiPastesSelected: null,
        readCode: false
    }),
    mounted() {
        this.load(this.$route.params.id)
        this.password = ""

        this.eventBus.$on("reloadPaste", () => {
            this.load(this.$route.params.id)
        })
    },
    beforeRouteUpdate(to, from, next) {
        this.password = ""
        this.multiPastes = null
        this.multiPastesSelected = null
        this.load(to.params.id)
        next()
    },
    methods: {
        load(id) {
            let data = {}
            if (this.password !== "" && this.passwordRequired)
                data.password = this.password
            this.pastefyAPI.get("/api/v2/paste/" + id, data)
                .then(res => {
                    let paste = res
                    if (paste.exists) {
                        this.title = paste.title
                        this.rawContent = paste.content
                        this.rawURL = paste.raw_url;
                        this.userid = paste.user_id
                        this.paste = paste
                        this.multiPastes = null
                        this.multiPastesSelected = null

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

                            this.rawContent = CryptoJS.AES.decrypt(paste.content, key).toString(CryptoJS.enc.Utf8);
                            if (this.rawContent === "")
                                this.validPassword = false
                            else
                                this.validPassword = true
                        } else if (paste.using_password) {
                            if (this.rawContent === '') {
                                if (this.password !== "") {
                                    helper.showSnackBar("Invalid Password", "#EE4343")
                                }
                            } else
                                this.validPassword = true
                        } else
                            this.validPassword = true


                        if (paste.type == 'PASTE') {
                            this.highlight(this.title, this.rawContent)
                        } else if (paste.type == 'MULTI_PASTE') {
                            this.multiPastes = JSON.parse(this.rawContent)
                            this.changeTab(0)
                        }
                    } else
                        this.found = false
                })
        },
        changeTab(i) {
            this.multiPastesSelected = i
            const tab = this.multiPastes[i]
            this.rawContent = tab.contents
            this.highlight(tab.name, tab.contents)
        },
        highlight(title, contents) {
            const pasteTitleComponents = title.split(".");
            let ending = pasteTitleComponents[pasteTitleComponents.length - 1];
            const originalEnding = (ending || "").toLowerCase()
            this.language = null

            this.extraContent = ''
            this.htmlPreview = ""
            this.htmlPreviewEnabled = false

            for (let replace in LANGUAGE_REPLACEMENTS) {
                if (ending == replace) {
                    ending = LANGUAGE_REPLACEMENTS[replace];
                    break;
                }
            }

            if (languages.includes(ending)) {
                this.language = ending;
            }

            this.showLineNums = true

            if (this.language === null)
                this.content = hljs.highlightAuto(contents).value
            else {
                if (this.language == 'text') {
                    this.content = this.escapeHtml(contents)
                    this.showLineNums = false
                } else
                    this.content = hljs.highlight(this.language, contents).value

                if (this.language === "markdown") {
                    const md = require('markdown-it')({
                        html: false,
                        breaks: true,
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
                    const EMPTY_CHAR = "‎"
                    this.extraContent = md.render(contents.replaceAll("<br>", "\n" + EMPTY_CHAR + "\n"))
                } else if (originalEnding === "html") {
                    this.htmlPreview = `

                    <script src="${window.location.protocol}//${window.location.host}/assets/js/htmlconsole.js"><` + `/script>
                    <style>* { box-sizing: border-box; margin: 0px; }</style>
                    <script>createConsole(${JSON.stringify(currentThemeVars)}, false, '200px')<` + `/script>
                    ${contents}
                    <script>
                    document.querySelectorAll("a[href]").forEach(el => {
                        el.setAttribute("target", "_blank")
                    })
                    <` + `/script>
                    `
                } else if (this.language === "javascript") {
                    const htmlScriptElement = document.createElement("script");
                    htmlScriptElement.innerHTML = contents+"\nwindow.evalReplace = c => eval(c);\n"
                    htmlScriptElement.type = 'module'
                    const wrappingHTML = document.createElement("div")
                    wrappingHTML.appendChild(htmlScriptElement)

                    this.htmlPreview = `
                    <script src="${window.location.protocol}//${window.location.host}/assets/js/htmlconsole.js"><` + `/script>
                    <style>* { box-sizing: border-box; margin: 0px; }</style>
                    <script>createConsole(${JSON.stringify(currentThemeVars)}, true, '100%')<` + `/script>
                    ${wrappingHTML.innerHTML}
                    `
                }
            }
        },
        copy() {
            helper.copyStringToClipboard(this.rawContent)
            helper.showSnackBar("Copied")
        },
        copyURL() {
            helper.copyStringToClipboard(window.location.href)
            helper.showSnackBar("Copied")
        },
        deletePaste() {
            const toast = helper.showSnackBar("Deleting...", "#ff9d34")
            this.pastefyAPI.delete("/api/v2/paste/" + this.$route.params.id)
                .then(res => {
                    if (res.success) {
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
        isPWA() {
            return window.matchMedia('(display-mode: standalone)').matches;
        },
        getUrlLineHash() {
            return window.location.hash
        },
        editPaste(fork = false) {
            this.$store.state.currentPaste.title = this.title
            if (!fork) {
                this.$store.state.currentPaste.password = this.password
                this.$store.state.currentPaste.editId = this.paste.id
                if (this.paste.folder)
                    this.$store.state.currentPaste.folder = this.paste.folder
            }

            if (this.paste.type == 'MULTI_PASTE') {
                this.$store.state.currentPaste.multiPastes = [...this.multiPastes]
                this.eventBus.$emit("setMultiPasteTabTo0")
            } else
                this.$store.state.currentPaste.content = this.rawContent

            this.eventBus.$emit("updatePasteEditorHighlighting")
        },
        jsonPretty() {
            if (this.jsonPrettified) {
                this.jsonPrettified = false
                this.rawContent = JSON.stringify(JSON.parse(this.rawContent), null, 0)
                this.highlight(this.title, this.rawContent)
            } else {
                this.jsonPrettified = true
                this.rawContent = JSON.stringify(JSON.parse(this.rawContent), null, 2)
                this.highlight(this.title, this.rawContent)
            }
        },
        reloadHTMLPreview() {
            this.htmlPreviewEnabled = false
            setTimeout(() => {
                this.htmlPreviewEnabled = true
            }, 100)
        }
    }
}
</script>
<style lang="scss" scoped>
h1 {
    color: var(--text-color);
    font-size: 30px;
    margin-bottom: 30px;
    min-height: 30px;
}

#paste-contents,
#preview {
    display: block;
    color: var(--text-color);
    background: var(--background-color);
    border-radius: 10px;
    padding: 10px;
    overflow-x: auto;
}


#paste-contents {
    font-size: 16.8px;

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
                background: var(--obj-background-color-hover);
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
    padding: 18px 26px;
}

#html-preview {
    background: #FFF;
    border-radius: 10px;
    margin-top: 10px;
    overflow: hidden;
    position: relative;
    border: var(--background-color) solid 2px;

    iframe {
        width: 100%;
        height: 70vh;
        border: none;
        outline: none;
        margin-bottom: -6px;
    }

    div {
        position: absolute;
        top: 5px;
        right: 5px;

        i {
            color: #000;
            cursor: pointer;
            border-radius: 20px;
            padding: 2px;

            &:hover {
                background: #00000022;
            }
        }
    }

    &.lang-javascript {
        i {
            color: var(--text-color)
        }
    }
}

.language {
    margin-left: 10px;
    background: #FFFFFF32;
    padding: 1px 7px;
    user-select: none;
    display: inline-block;
    border-radius: 10px;
}

#action-buttons.mobile {
    a {
        color: #FFF;
        background: #3469FF;
        padding: 0px 26px;
        border-radius: 100px;
        position: fixed;
        bottom: 20px;
        display: inline-block;
        right: 20px;
    }

    a + a {
        bottom: 72px;
    }

    a + a + a {
        bottom: 124px;
    }

    a + a + a + a {
        bottom: 176px;
    }

    #copy-contents {
        padding: 13px;
        margin-top: -3px;
        position: static;

        i {
            display: block;
        }
    }
}

#tabs {
    margin-bottom: 10px;

    a {
        padding: 7px 10px;
        display: inline-block;
        background: var(--tab-color);
        border-radius: 10px;
        margin-right: 10px;
        cursor: pointer;

        &.selected {
            background: var(--tab-color-selected);
        }
    }
}
</style>
<style lang="scss">
#preview {

    font-size: 17.5px;

    h1, h2, h3, h4, h5 {
        margin-top: 30px;
        margin-bottom: 15px;
    }

    h1 {
        padding-bottom: 18px;
    }

    h2 {
        font-size: 28px;
    }

    h3 {
        font-size: 23px;
    }

    h4 {
        font-size: 20px;
    }

    h5 {
        font-size: 18px;
    }

    img {
        max-width: 100%;
        border-radius: 10px;
        margin: 10px 0px;
    }

    blockquote {
        padding-left: 10px;
        border-left: 4px #00000077 solid;
        background: #FFFFFF09;
        margin: 20px 0px;
    }

    code {
        background: #FFFFFF22;
        padding: 0px 6px;
        border-radius: 5px;
    }

    pre code {
        background: none;
        padding: 0px;
        border-radius: 0px;
    }

    pre {
        overflow-x: auto;
        background: #00000025;
        margin: 30px 0px;
    }

    ul, ol {
        padding-left: 20px;
    }
}
</style>