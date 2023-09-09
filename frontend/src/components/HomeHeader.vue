<template>
    <div>
        <h1 v-if="$store.state.user.logged_in">Welcome, <span v-animate-css="{classes: 'fadeIn', delay: 0}">{{ $store.state.user.name }}</span></h1>
        <h1 v-else>Welcome to {{ $store.state.appInfo.custom_name || 'Pastefy' }}!</h1><br>

        <div>
            <router-link v-if="$store.state.appInfo.public_pastes_enabled || ($store.state.user.logged_in && $store.state.user.auth_type === 'interaapps') || $store.state.user.type == 'ADMIN'" to="/" class="tab">Home</router-link>
            <router-link v-if="$store.state.appInfo.public_pastes_enabled" to="/public" class="tab">Public</router-link>
            <router-link v-if="$store.state.user.logged_in && $store.state.user.auth_type === 'interaapps'" to="/shared" class="tab">Shared</router-link>
            <router-link v-if="$store.state.user.type == 'ADMIN'" to="/admin" class="tab">Admin</router-link>
        </div>
        <br>
    </div>
</template>

<script>
export default {
}
</script>

<style lang="scss" scoped>
h1 {
    margin-bottom: -10px;
}
.tab {
    display: inline-block;
    padding: 6px 9px;
    margin-right: 10px;
    background: var(--tab-color);
    border-radius: 10px;
    text-decoration: none;
    user-select: none;

    &.router-link-exact-active {
        background: var(--tab-color-selected);
    }
}
</style>