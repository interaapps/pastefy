<template>
    <div id="create-paste" :class="{'input-fullscreen': inputFullscreen}"
         style="height: calc(100% - 300px); min-height: 300px">
        <input @input="updateEditorLang" spellcheck="false" autocomplete="off"
               v-model="currentPaste.title" class="input" type="text" placeholder="Title"
               id="title-input">

        <svg @click="
                    Object.keys(currentPaste.multiPastes).length == 0 ? addTab(currentPaste.title ? currentPaste.title : 'new') : null;
                    addTab()
                " :style="{right: $store.state.mobileVersion ? '15px' : ''}" id="create-new-tab-button"
             xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="currentColor" class="bi bi-plus-lg"
             viewBox="0 0 16 16">
            <path
                d="M8 0a1 1 0 0 1 1 1v6h6a1 1 0 1 1 0 2H9v6a1 1 0 1 1-2 0V9H1a1 1 0 0 1 0-2h6V1a1 1 0 0 1 1-1z"/>
        </svg>

        <svg id="input-fullscreen-button" @click="inputFullscreen = false"
             v-if="inputFullscreen && !$store.state.mobileVersion" xmlns="http://www.w3.org/2000/svg" width="16"
             height="16" fill="currentColor" class="bi bi-fullscreen-exit" viewBox="0 0 16 16">
            <path
                d="M5.5 0a.5.5 0 0 1 .5.5v4A1.5 1.5 0 0 1 4.5 6h-4a.5.5 0 0 1 0-1h4a.5.5 0 0 0 .5-.5v-4a.5.5 0 0 1 .5-.5zm5 0a.5.5 0 0 1 .5.5v4a.5.5 0 0 0 .5.5h4a.5.5 0 0 1 0 1h-4A1.5 1.5 0 0 1 10 4.5v-4a.5.5 0 0 1 .5-.5zM0 10.5a.5.5 0 0 1 .5-.5h4A1.5 1.5 0 0 1 6 11.5v4a.5.5 0 0 1-1 0v-4a.5.5 0 0 0-.5-.5h-4a.5.5 0 0 1-.5-.5zm10 1a1.5 1.5 0 0 1 1.5-1.5h4a.5.5 0 0 1 0 1h-4a.5.5 0 0 0-.5.5v4a.5.5 0 0 1-1 0v-4z"/>
        </svg>
        <svg id="input-fullscreen-button" @click="inputFullscreen = true"
             v-else-if="!$store.state.mobileVersion" xmlns="http://www.w3.org/2000/svg" width="16" height="16"
             fill="currentColor" class="bi bi-arrows-fullscreen" viewBox="0 0 16 16">
            <path fill-rule="evenodd"
                  d="M5.828 10.172a.5.5 0 0 0-.707 0l-4.096 4.096V11.5a.5.5 0 0 0-1 0v3.975a.5.5 0 0 0 .5.5H4.5a.5.5 0 0 0 0-1H1.732l4.096-4.096a.5.5 0 0 0 0-.707zm4.344 0a.5.5 0 0 1 .707 0l4.096 4.096V11.5a.5.5 0 1 1 1 0v3.975a.5.5 0 0 1-.5.5H11.5a.5.5 0 0 1 0-1h2.768l-4.096-4.096a.5.5 0 0 1 0-.707zm0-4.344a.5.5 0 0 0 .707 0l4.096-4.096V4.5a.5.5 0 1 0 1 0V.525a.5.5 0 0 0-.5-.5H11.5a.5.5 0 0 0 0 1h2.768l-4.096 4.096a.5.5 0 0 0 0 .707zm-4.344 0a.5.5 0 0 1-.707 0L1.025 1.732V4.5a.5.5 0 0 1-1 0V.525a.5.5 0 0 1 .5-.5H4.5a.5.5 0 0 1 0 1H1.732l4.096 4.096a.5.5 0 0 1 0 .707z"/>
        </svg>
        <div>
            <div id="tabs" v-if="currentPaste.multiPastes.length > 0">
                <div v-for="(contents, i) of currentPaste.multiPastes" :key="i"
                     @click="selectTab(i, multiPastesSelected)" :class="{selected:i==multiPastesSelected}">
                    <input @input="updateEditorLang" v-model="currentPaste.multiPastes[i].name"
                           type="text" placeholder="Name">
                    <svg @click="closeTab(i)" xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="currentColor"
                         class="bi bi-x" viewBox="0 0 16 16">
                        <path
                            d="M4.646 4.646a.5.5 0 0 1 .708 0L8 7.293l2.646-2.647a.5.5 0 0 1 .708.708L8.707 8l2.647 2.646a.5.5 0 0 1-.708.708L8 8.707l-2.646 2.647a.5.5 0 0 1-.708-.708L7.293 8 4.646 5.354a.5.5 0 0 1 0-.708z"/>
                    </svg>
                </div>
            </div>
        </div>

        <div ref="editor" id="editor"></div>

        <div id="options" :class="{'opened': optionsOpened}">

            <h5 class="label">VISIBILITY</h5>
            <select v-model="currentPaste.visibility" class="input" style="margin-top: 10px; margin-bottom: 4px">
                <option value="UNLISTED">Unlisted</option>
                <option v-if="$store.state.appInfo.public_pastes_enabled" value="PUBLIC">Public</option>
                <option v-if="$store.state.user.logged_in" value="PRIVATE">Private</option>
            </select>
            <p style="opacity: 0.5; color: var(--text-color); margin-bottom: 8px">
                <template v-if="currentPaste.visibility === 'PUBLIC'">Public Pastes can be viewed in the public section of {{ $store.state.appInfo.custom_name || 'Pastefy' }}. Users can search, interact or fork them. Encryption and password protection is not allowed.</template>
                <template v-else-if="currentPaste.visibility === 'PRIVATE'">Only you can view the paste.</template>
                <template v-else>You & everyone with the paste url</template>
            </p>

            <template v-if="currentPaste.visibility !== 'PUBLIC'">
                <h5 class="label">PASSWORD</h5>
                <input autocomplete="new-password" v-model="currentPaste.password" class="input"
                       type="password" placeholder="Password (Optional)">
            </template>

            <h5 class="label">SETTINGS</h5>

            <template v-if="currentPaste.visibility !== 'PUBLIC'">
                <label for="clientencrypted">Client-Encrypted</label>
                <input type="checkbox" v-model="clientEncrypted"
                       :readonly="currentPaste.password != ''" name="clientencrypted">

                <span style="opacity: 0.5; color: var(--text-color)" v-if="clientEncrypted"><br>Client-Encryption deactivates the RAW function and some more. You can't open an encrypted paste without the password (If you set one) or the link.</span><br>
            </template>

            <label for="expiry">Paste expires</label>
            <input type="checkbox" v-model="expiry" name="expiry">
            <input v-if="expiry" :min="new Date().toTimeString()" v-model="currentPaste.expire_at" step="1" class="input" type="datetime-local">

            <br>

            <h5 v-if="$store.state.user.logged_in" class="label">Folder</h5>
            <select class="input" v-if="$store.state.user.logged_in" v-model="currentPaste.folder">
                <option selected value="">none</option>
                <option v-for="(id, name) of folders" :key="id" :value="id">{{ name }}</option>
            </select>


            <div v-if="$store.state.user.logged_in && $store.state.user.auth_type == 'interaapps'">
                <h5 class="label">SHARE TO FRIEND</h5>
                <input autocomplete="off" v-model="currentPaste.friends" class="input" type="text"
                       placeholder="Friends (By username, split with ,)">
                <div id="friend-list" v-if="friendList.length > 0">
                    <a v-for="friend of friendList" :key="friend" @click="addFriendToList(friend)"
                       :class='{selected: currentPaste.friends.includes(friend)}'>{{ friend }}</a>
                </div>
            </div>
        </div>

        <div id="edit-indicator" v-if="currentPaste.editId">
            <span>EDITING {{ windowHost }}/{{ currentPaste.editId }}</span>
            <svg @click="clearInputs()" xmlns="http://www.w3.org/2000/svg" width="16" height="16"
                 fill="currentColor" class="bi bi-x" viewBox="0 0 16 16">
                <path
                    d="M4.646 4.646a.5.5 0 0 1 .708 0L8 7.293l2.646-2.647a.5.5 0 0 1 .708.708L8.707 8l2.647 2.646a.5.5 0 0 1-.708.708L8 8.707l-2.646 2.647a.5.5 0 0 1-.708-.708L7.293 8 4.646 5.354a.5.5 0 0 1 0-.708z"/>
            </svg>
        </div>


        <div id="edit-indicator" v-if="currentPaste.forked_from">
            <span>FORKING {{ windowHost }}/{{ currentPaste.forked_from }}</span>
            <svg @click="currentPaste.forked_from = null" xmlns="http://www.w3.org/2000/svg" width="16" height="16"
                 fill="currentColor" class="bi bi-x" viewBox="0 0 16 16">
                <path
                    d="M4.646 4.646a.5.5 0 0 1 .708 0L8 7.293l2.646-2.647a.5.5 0 0 1 .708.708L8.707 8l2.647 2.646a.5.5 0 0 1-.708.708L8 8.707l-2.646 2.647a.5.5 0 0 1-.708-.708L7.293 8 4.646 5.354a.5.5 0 0 1 0-.708z"/>
            </svg>
        </div>

        <div id="buttons" :class="{mobile: $store.state.mobileVersion}">
            <a
                id="submit-button"
                @click="send"
            >
                <span v-if="currentPaste.editId">SAVE</span>
                <span v-else-if="currentPaste.folder">SUBMIT TO FOLDER</span>
                <span v-else-if="currentPaste.visibility === 'PUBLIC'">SUBMIT (PUBLIC)</span>
                <span v-else>SUBMIT</span>
            </a>
            <a id="settings-button" @click="optionsOpened = !optionsOpened">
                <svg width="1em" height="1em" viewBox="0 0 16 16" class="bi bi-sliders" fill="currentColor"
                     xmlns="http://www.w3.org/2000/svg">
                    <path fill-rule="evenodd"
                          d="M11.5 2a1.5 1.5 0 1 0 0 3 1.5 1.5 0 0 0 0-3zM9.05 3a2.5 2.5 0 0 1 4.9 0H16v1h-2.05a2.5 2.5 0 0 1-4.9 0H0V3h9.05zM4.5 7a1.5 1.5 0 1 0 0 3 1.5 1.5 0 0 0 0-3zM2.05 8a2.5 2.5 0 0 1 4.9 0H16v1H6.95a2.5 2.5 0 0 1-4.9 0H0V8h2.05zm9.45 4a1.5 1.5 0 1 0 0 3 1.5 1.5 0 0 0 0-3zm-2.45 1a2.5 2.5 0 0 1 4.9 0H16v1h-2.05a2.5 2.5 0 0 1-4.9 0H0v-1h9.05z"/>
                </svg>
            </a>
        </div>
    </div>
</template>
<script>
import { CodeEditor } from 'petrel'
import CryptoJS from "crypto-js";
import hljs from "highlight.js";
import LANGUAGE_REPLACEMENTS from '../assets/data/langReplacements'
import helper, { estimateTitle } from '@/helper'
import eventBus from '@/eventBus'


const LANGUAGES = hljs.listLanguages()

const AUTOCOMPLETIONS = [
    {language: "javascript", file: "JavaScriptAutoComplete"},
    {language: "dockerfile", file: "DockerfileAutoComplete"},
    {language: "html", file: "HTMLAutoComplete"},
    {language: "json", file: "JSONAutoComplete"},
    {language: "java", file: "JavaAutoComplete"},
    {language: "markdown", file: "MarkdownAutoComplete"},
    {language: "php", file: "PHPAutoComplete"},
    {language: "sql", file: "SQLAutoComplete"},
    {language: "xml", file: "XMLAutoComplete"},
    {language: "yaml", file: "YAMLAutoComplete"},
]

let codeEditor = null;
let codeEditorInputListener;

const DEFAULT_HIGHLIGHTER = v => v.replace(/&/g, "&amp;").replace(/</g, "&lt;").replace(/>/g, "&gt;").replace(/"/g, "&quot;").replace(/'/g, "&#039;")

export default {
    data: () => ({
        windowHost: window.location.host,
        clientEncrypted: false,
        expiry: false,
        multiPastesSelected: null,
        friendList: [],
        optionsOpened: false,
        folders: {},
    }),
    props: {
        inputFullscreen: {
            type: Boolean
        }
    },
    watch: {
        'currentPaste.editId'() {
            this.r = Math.random()
        },
        'currentPaste.title'() {
            this.updateEditorLang()
        },
        'currentPaste.expire_at'() {
            if (this.currentPaste.expire_at) {
                this.expiry = true
            }
        },
        'expiry'() {
            if (this.expiry) {
                const date = new Date()
                date.setTime(date.getTime() + 1000 * 60 * 60 * 48)
                console.log(this.currentPaste.expire_at)
                this.currentPaste.expire_at = date.toISOString().slice(0, 19).replace('T', ' ')
                console.log(this.currentPaste.expire_at)

                if (this.currentPaste.expire_at.length === 16) {
                    this.currentPaste.expire_at += ':00'
                }
                console.log(this.currentPaste.expire_at)
            } else {
                this.currentPaste.expire_at = null
            }
        },
        'currentPaste.content'(to) {
            codeEditor.value = to
            codeEditor.update()
        },
        'currentPaste.visibility'(to) {
            if (to === 'PUBLIC') {
                this.clientEncrypted = false
            }
        }
    },
    mounted() {
        this.eventBus.$on("setMultiPasteTabTo0", () => {
            this.selectTab(0)
        })

        codeEditor = new CodeEditor(this.$refs.editor)

        let lastLength = 0
        codeEditorInputListener = () => {
            this.currentPaste.content = codeEditor.value
            const length = this.currentPaste.content.length
            if (lastLength != length && length > 9000) {
                this.updateEditorLang()
            }
            lastLength = length
        }
        codeEditorInputListener;
        //codeEditor.textAreaElement.addEventListener("keydown", codeEditorInputListener)
        //codeEditor.textAreaElement.addEventListener("paste", codeEditorInputListener)
        codeEditor.textAreaElement.placeholder = "Paste in here"
        codeEditor.textAreaElement.addEventListener('paste', e => {
            const data = (event.clipboardData || window.clipboardData).getData('text')
            if (data && e.target.value === '' && this.currentPaste.title === '') {
                this.currentPaste.title = estimateTitle(data.trim())
            }
        })
        codeEditor.create()
        this.clearInputs()

        this.loadFolders()
        eventBus.$on('loadFolders', this.loadFolders)
    },
    computed: {
        currentPaste() {
            return this.$store.state.currentPaste
        }
    },
    methods: {
        loadFolders() {
            this.folders = {}
            this.pastefyAPI.get("/api/v2/user/folders", {hide_pastes: true}).then(folders => {
                const recursiveAddFolder = (folder, parentName = "") => {
                    this.folders = {[parentName + folder.name]: folder.id, ...this.folders}

                    folder.children.forEach(child => {
                        recursiveAddFolder(child, folder.name + '/')
                    })
                }

                for (let folder of folders) {
                    recursiveAddFolder(folder)
                }
            })
        },
        closeTab(i) {
            if (i === this.multiPastesSelected)
                this.selectTab(0)
            this.currentPaste.multiPastes.splice(i, 1);
            if (this.multiPastesSelected > i)
                this.selectTab(this.multiPastesSelected-1)
        },
        clearInputs() {
            codeEditor.value = ""
            this.$store.state.app.fullscreen = false
            this.inputFullscreen = false
            this.currentPaste.content = ""
            this.currentPaste.title = ""
            this.currentPaste.password = ""
            this.currentPaste.folder = ""
            this.currentPaste.editId = null
            this.currentPaste.multiPastes = []
            this.currentPaste.expire_at = null
            this.currentPaste.forked_from = null
            this.multiPastesSelected = null
            this.clientEncrypted = !!this.$store.state.appInfo.encryption_is_default
            this.expiry = false
            this.currentPaste.visibility = 'UNLISTED'
            codeEditor.update()
        },
        updateEditorLang() {
            let language;
            const split = (Object.keys(this.currentPaste.multiPastes).length == 0 ? this.currentPaste.title : this.currentPaste.multiPastes[this.multiPastesSelected].name).split(".")
            let isHTML = false
            if (split.length > 1) {
                language = split[split.length - 1]
                if (language == "html" || language == "htm")
                    isHTML = true

                for (const name in LANGUAGE_REPLACEMENTS) {
                    if (language == name) {
                        language = LANGUAGE_REPLACEMENTS[name]
                        break;
                    }
                }
            } else if (split[0] == 'Dockerfile')
                language = 'dockerfile'


            if (this.currentLanguage != language) {
                this.currentLanguage = language
                if (LANGUAGES.includes(language)) {
                    if (!this.$store.state.app.newPasteEditorDisableHighlighting) {
                        codeEditor.setHighlighter(c => hljs.highlight(language, c).value)
                    } else {
                        codeEditor.setHighlighter(DEFAULT_HIGHLIGHTER)
                    }
                    if (!this.$store.state.app.newPasteEditorDisableAutocompletion) {
                        codeEditor.setAutoCompleteHandler(null)
                        for (const autocompletion of AUTOCOMPLETIONS) {
                            if (autocompletion.language == language || (autocompletion.language == "html" && isHTML)) {
                                (async () => {
                                    codeEditor.setAutoCompleteHandler(new (await import(`petrel/src/languages/${autocompletion.file}.js`)).default())
                                })()
                            }
                        }
                    }
                }
                codeEditor.update()
            }


            if (codeEditor.value.length > 7000) {
                codeEditor.setAutoCompleteHandler(null)
                if (codeEditor.value.length > 11000) {
                    codeEditor.setHighlighter(DEFAULT_HIGHLIGHTER)
                }
            }
        },
        async send() {
            let data = {
                content: codeEditor.value,
                title: this.currentPaste.title,
                forked_from: this.currentPaste.forked_from || undefined,
                visibility: this.currentPaste.visibility
            }

            if (this.currentPaste.folder !== "")
                data.folder = this.currentPaste.folder

            if (this.expiry) {
                data.expire_at = this.currentPaste.expire_at

                data.expire_at = this.currentPaste.expire_at.slice(0, 19).replace('T', ' ')

                if (data.expire_at.length === 16) {
                    data.expire_at += ':00'
                }
            }

            data.encrypted = false;
            let key;

            data.type = 'PASTE'

            if (Object.keys(this.currentPaste.multiPastes).length > 0) {
                this.currentPaste.multiPastes[this.multiPastesSelected].contents = codeEditor.value

                data.content = JSON.stringify(this.currentPaste.multiPastes)
                data.type = 'MULTI_PASTE'
            }

            if (this.clientEncrypted || this.currentPaste.password !== "") {
                key = this.currentPaste.password === "" ? Math.random().toString(36).substring(3) + Math.random().toString(36).substring(3) : this.currentPaste.password;

                data.content = CryptoJS.AES.encrypt(data.content, key).toString();
                data.title = CryptoJS.AES.encrypt(data.title, key).toString();
                data.encrypted = true;
            }

            const toast = helper.showSnackBar("Sending...")
            this.loading = true

            if (this.currentPaste.editId) {
                this.pastefyAPI.editPaste(this.currentPaste.editId, data)
                    .then(() => {
                        this.loading = false

                        let hash = "";
                        if (this.currentPaste.password === "" && this.clientEncrypted)
                            hash = "#" + key
                        this.$router.push("/" + this.currentPaste.editId + hash)
                        this.eventBus.$emit("reloadPaste")
                        this.clearInputs()
                        toast.close()
                        this.loading = false
                        helper.showSnackBar("Done!")
                    })
                    .catch(() => {
                        toast.close()
                        helper.showSnackBar("Error during posting the paste :(", "#EE4343")
                        this.loading = false
                    })
            } else {
                if (data.content === '' && data.content === '') {
                    helper.showSnackBar("Can't create empty pastes", "#EE4343")
                    this.loading = false
                    return;
                }
                this.pastefyAPI.createPaste(data)
                    .then(paste => {
                        let date = new Date()
                        this.$store.state.app.lastPastes.unshift({
                            id: paste.id,
                            title: this.currentPaste.title,
                            content: codeEditor.value.substring(0, 50) + "...",
                            date: date.getMonth() + "/" + date.getDate() + "/" + date.getFullYear()
                        })
                        localStorage.setItem("created_pastes", JSON.stringify(this.$store.state.app.lastPastes))

                        let hash = "";
                        if (this.currentPaste.password === "" && this.clientEncrypted)
                            hash = "#" + key

                        this.$router.push("/" + paste.id + hash)
                        this.clearInputs()

                        helper.copyStringToClipboard(window.location.protocol + "//" + window.location.host + "/" + paste.id + hash)
                        toast.close()
                        helper.showSnackBar("Copied " + window.location.protocol + "//" + window.location.host + "/" + paste.id + hash + " to clipboard.")

                        this.shareToFriends(paste.id, this.currentPaste.friends.split(","))
                        this.currentPaste.friends = ""

                        this.loading = false
                    })
                    .catch(() => {
                        toast.close()
                        helper.showSnackBar("Error during posting the paste :(", "#EE4343")
                        this.loading = false
                    })
            }
        },
        async shareToFriends(paste, friends) {
            for (let friend of friends) {
                friend = friend.trim()
                if (friend.length > 3) {
                    const toast = helper.showSnackBar("Adding friend (" + friend + ") to paste...")
                    let res = await this.pastefyAPI.post(`/api/v2/paste/${paste}/friend`, {
                        friend
                    })
                    toast.close()
                    if (res.success) {
                        helper.showSnackBar("Added friend (" + friend + ") to paste!")
                        if (!this.friendList.includes(friend)) {
                            this.friendList.push(friend)
                            localStorage["saved_contacts"] = JSON.stringify(this.friendList)
                        }
                    } else
                        helper.showSnackBar("Couldn't add friend (" + friend + ") to paste!", "#FF3232")
                }
            }
        },
        addTab(title = "new") {
            const first = this.currentPaste.multiPastes.length == 0
            const index = this.currentPaste.multiPastes.push({
                name: title,
                contents: first ? codeEditor.value : ''
            })
            this.selectTab(index - 1, this.multiPastesSelected)
        },
        selectTab(i, current = null) {
            if (current !== null && this.currentPaste.multiPastes[current]) {
                this.currentPaste.multiPastes[current].contents = codeEditor.value
            }
            if (this.currentPaste.multiPastes[i]) {
                this.multiPastesSelected = i
                this.currentPaste.content = this.currentPaste.multiPastes[i].contents
                codeEditor.value = this.currentPaste.content
                this.updateEditorLang()
                codeEditor.update()
            }
        },
        escapeHtml(unsafe) {
            return unsafe
                .replace(/&/g, "&amp;")
                .replace(/</g, "&lt;")
                .replace(/>/g, "&gt;")
                .replace(/"/g, "&quot;")
                .replace(/'/g, "&#039;");
        },
        onContentInputScroll(e) {
            this.contentInputScrolled = e.target.scrollTop > 30
        }
    }
}
</script>