<template>
    <router-link class="folder-card" :to="{name: 'Folder', params: {id: folder.id}}">
        <div class="folder-card-box">
            <svg xmlns="http://www.w3.org/2000/svg" class="icon icon-tabler icon-tabler-folder" width="28" height="28" viewBox="0 0 24 24" stroke-width="2" stroke="currentColor" fill="none" stroke-linecap="round" stroke-linejoin="round">
                <path stroke="none" d="M0 0h24v24H0z" fill="none"></path>
                <path d="M5 4h4l3 3h7a2 2 0 0 1 2 2v8a2 2 0 0 1 -2 2h-14a2 2 0 0 1 -2 -2v-11a2 2 0 0 1 2 -2"></path>
            </svg>
        </div>

        <span>{{ folder.name }}</span>
    </router-link>
</template>

<script>
export default {
    name: "FolderCard",
    props: {
        folder: { type: Object }
    }
}
</script>