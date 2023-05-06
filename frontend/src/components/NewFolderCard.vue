<template>
    <a class="folder-card new" @click="$refs.createFolderModal.open()">
        <div class="folder-card-box">
            <svg xmlns="http://www.w3.org/2000/svg" class="icon icon-tabler icon-tabler-folder" width="28" height="28" viewBox="0 0 24 24" stroke-width="2" stroke="currentColor" fill="none" stroke-linecap="round" stroke-linejoin="round">
                <path stroke="none" d="M0 0h24v24H0z" fill="none"></path>
                <path d="M5 4h4l3 3h7a2 2 0 0 1 2 2v8a2 2 0 0 1 -2 2h-14a2 2 0 0 1 -2 -2v-11a2 2 0 0 1 2 -2"></path>
            </svg>
        </div>

        <span>new</span>

        <Modal ref="createFolderModal" title="Create Folder">
            <input autofocus v-model="name" placeholder="Folder name" type="text" class="input">
            <button class="button right" @click="createFolder">Create</button>
        </Modal>
    </a>
</template>

<script>
import Modal from "@/components/Modal.vue";
import eventBus from "@/eventBus";

export default {
    components: {Modal},
    props: {
        parent: { type: String, default: undefined }
    },
    data: () => ({
        name: ''
    }),
    methods: {
        createFolder() {
            this.pastefyAPI.post("/api/v2/folder", {
                name: this.name,
                parent: this.parent
            }).then(folder => {
                this.name = ""
                this.$refs.createFolderModal.close()
                this.$emit('created', folder)
                eventBus.$emit('loadFolders')
            })
        }
    }
}
</script>