<template>
    <portal to="modals">
        <div @click="close" v-if="opened" class="modal-bg" v-animate-css="{classes: 'fadeIn', duration: 250}">
            <form @submit.prevent="$emit('submit')">
                <div @click.stop class="modal" :style="{width}">
                    <a @click="close" class="modal-close-button">
                        &#x2715;
                    </a>
                    <h1 class="modal-title">{{ title }}</h1>

                    <div class="modal-body">
                        <slot />
                    </div>
                </div>
            </form>
        </div>
    </portal>
</template>

<script>
export default {
    name: "Modal",
    data: () => ({
        opened: false
    }),
    props: {
        title: {type: String},
        width: {type: String, default: '420px'}
    },
    methods: {
        open() {
            this.opened = true
        },
        close() {
            this.opened = false
        }
    }
}
</script>

<style lang="scss" scoped>
.modal-bg {
    position: fixed;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    background: #00000011;
    backdrop-filter: blur(3px);
    z-index: 10000000;
    color: var(--text-color);

    .modal {
        width: 420px;
        max-width: 100%;
        max-height: 90%;
        min-height: 100px;
        position: absolute;
        background: var(--background-color);
        padding: 23px;
        top: 50%;
        left: 50%;
        border-radius: 10px;
        transform: translateX(-50%) translateY(-50%);
        box-shadow: 0 0 20px 0 #00000017;
        border: 2px solid #00000011;
        .modal-title {
            font-size: 22px;
            line-height: 30px;
            font-weight: 600;
        }
        .modal-close-button {
            font-size: 29px;
            float: right;

            display: inline-block;
            width: 30px;
            height: 30px;
            cursor: pointer;
            text-align: center;
            line-height: 27px;
            border-radius: 40px;

            &:hover {
                background: var(--obj-background-color-hover);
            }
        }
        .modal-body {
            margin-top: 20px;
        }
    }
}
</style>