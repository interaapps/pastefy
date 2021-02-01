<template>
    <div>
        <h1>Logging in...</h1>
    </div>
</template>
<script>
export default {
    mounted(){
        localStorage["session"] = this.$route.query.key
        this.pastefyAPI.options.header["x-auth-key"] = localStorage["session"]
        this.$router.push("/")
    }
}
</script>