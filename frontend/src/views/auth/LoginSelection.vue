<template>
    <div>
        <h1>Log in with</h1>
        <a v-for="authtype of $store.state.user.auth_types" :key="authtype" :href="'/api/v2/auth/oauth2/'+authtype" style="display: block; margin-bottom: 20px;">Login with your {{authtype}}-Account</a>
    </div>
</template>
<script>
export default {
    mounted(){
    }
}
</script>