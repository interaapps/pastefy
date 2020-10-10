<template>
    <div>
        <h1>Settings</h1>
        
        Fullscreen-Paste on Homepage: <input v-model="$store.state.app.fullscreenOnHomepage" type="checkbox">
    </div>
</template>
<script>
export default {
    data: ()=>({
        fullscreenOnHomepage: false
    }),
    mounted(){
    },
    methods:{
        
    },
    watch: {
        '$store.state.app.fullscreenOnHomepage': {
            handler: function(to, from){
                console.log(to)
                localStorage.setItem('fullscreen_on_homepage', to)
            },
            immediate: false
        }
    }
    
}
</script>
<style lang="scss" scoped>

</style>