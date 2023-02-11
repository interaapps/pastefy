<template>
    <Modal ref="modal" :title="title" @submit="$emit('confirm')">
        <p>
            <slot />
        </p>
        <input style="opacity: 0; height: 1px" autofocus />
        <button type="submit" class="button right">Confirm</button>
    </Modal>
</template>

<script>
import Modal from "@/components/Modal.vue";

export default {
    name: "ConfirmationModal",
    components: {Modal},
    props: {
        title: {type: String}
    },
    methods: {
        open() {
            this.$refs.modal.open()
        }
    }
}
</script>

<style scoped>

</style>