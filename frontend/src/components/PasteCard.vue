<template>
    <div>
        <router-link :to="'/'+paste.id" class="paste-card" v-if="paste">
            <span class="date" v-if="paste.created_at">{{ new Date(paste.created_at.replace(' ', 'T')).toLocaleString() }}</span>

            <h3 v-if="!paste.encrypted && paste.title">{{ paste.title }}</h3>

            <pre v-if="paste.encrypted"><code>This paste can't be previewed. It's client-encrypted.</code></pre>

            <pre v-else-if="paste.type === 'MULTI_PASTE'"><!--
            --><code v-html="this.multiPasteParts.length > 0 ? highlight(multiPasteParts[0].name, multiPasteParts[0].contents) : ''" /><!--
            --></pre>

            <pre v-else><!--
            --><code v-html="highlight(paste.title, paste.content)" /><!--
            --></pre>

            <h4
                class="multi-paste-size-indicator"
                v-if="paste.type == 'MULTI_PASTE'"
                style="right: 15px; top: 15px"
            >
                <img v-if="paste.tags?.includes('codebox')" src="@/assets/img/codebox-logo.svg" alt="">
                {{
                    paste.tags?.includes('codebox')
                    ? 'Codebox'
                    : multiPasteParts.length > 1
                      && this.multiPasteParts[0].contents.split('\n').length >= 2
                        ? `+${multiPasteParts.length - 1} Files`
                        : 'Multi-Paste'
                }}
            </h4>
        </router-link>
    </div>
</template>
<script>
import hljs from "highlight.js";
import {getLanguageByFileName} from "@/helper";

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
        highlight(title, content) {
            const [, language] = getLanguageByFileName(title)
            return language ? hljs.highlight(language, content).value : hljs.highlightAuto(content).value
        },
    }
}
</script>