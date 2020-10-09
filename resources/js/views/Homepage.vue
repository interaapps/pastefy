<template>
    <div>
        <div v-if="$store.state.user.loggedIn">
            <h1>Welcome, {{$store.state.user.name}}</h1>
        </div>
        <div v-else>
            <h1>Welcome to Pastefy!</h1>
        </div>
    </div>
</template>
<script>
export default {
    data: ()=>({})
}
</script>
<style scoped lang="scss">
    
</style>