<template>
    <span v-if="!banner" />
    <a v-else :href="banner.url" :style="banner.style || {}" :target="banner.new_tab ? '_blank' : false" class="info-banner">
        <picture>
            <template v-if="banner.image_sources">
                <source v-for="imageSource of banner.image_sources" :key="imageSource.media" :srcset="imageSource.src" :media="imageSource.media" >
            </template>
            <img :style="banner.image_style || {}" :src="banner.image">
        </picture>
    </a>
</template>

<script>
export default {
    name: "WebsiteBanner",
    props: {
        paste: {type: String}
    },
    data: () => ({
        banner: false
    }),
    async created() {
        try {
            const banner = await this.pastefyAPI.get("/api/v2/paste/" + this.paste)

            const bannerContent = JSON.parse(banner.content)

            if (bannerContent.active) {
                this.banner = bannerContent
            }

        } catch(e) {
            //
        }
    }
}
</script>

<style lang="scss" scoped>
ignore {
    display: none;
}
.info-banner {
    max-width: 1000px;
    display: block;
    img, picture, source {
        max-width: 100%;
    }
}
</style>