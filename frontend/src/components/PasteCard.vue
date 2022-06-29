<template>
    <div>
        <router-link :to="'/'+paste.id" class="paste" v-if="paste">
            <span class="date">{{ paste.created_at }}</span>
            <h4 v-if="paste.type == 'MULTI_PASTE'"
                style="float: right; font-weight: 500; padding: 0px 10px; margin-bottom: 10px;background: #FFFFFF11; border-radius: 100px; display: inline-block">
                MULTI</h4>
            <h3 v-if="!paste.encrypted">{{ paste.title }}</h3>
            <pre v-if="paste.encrypted"><code>This paste can't be previewed. It's client-encrypted.</code></pre>
            <pre v-else-if="paste.type == 'MULTI_PASTE'"><code
                v-html="this.multiPasteParts.length > 0 ? highlight(this.multiPasteParts[0].contents) : ''"></code></pre>
            <pre v-else><code v-html="highlight(paste.content)"></code></pre>
            <h4 v-if="paste.type == 'MULTI_PASTE' && multiPasteParts.length > 1 && this.multiPasteParts[0].contents.split('\n').length >= 3"
                class="multi-paste-size-indicator">+{{ multiPasteParts.length - 1 }} Files</h4>

        </router-link>
    </div>
</template>
<script>
import hljs from "highlight.js";

export default {
    data: () => ({
        multiPasteParts: []
    }),
    props: ['paste'],
    created() {
        try {
            if (this.paste.type == 'MULTI_PASTE') {
                this.multiPasteParts = JSON.parse(this.paste.content)
            }
        } catch (e) {
            console.log(e);
        }
    },
    methods: {
        highlight(content) {
            return hljs.highlightAuto(content).value
        },
    }
}
</script>

<style lang="scss" scoped>
.paste {
    .multi-paste-size-indicator {
        float: right;
        font-weight: 500;
        padding: 0px 10px;
        margin-top: -25px;
        background: #FFFFFF11;
        border-radius: 100px;
        display: inline-block
    }
}
</style>