<template>
    <div style="min-height: 100%;">
        <side-bar />
        <router-view :class="{'mobile': $store.state.mobileVersion}" id="page" />

        <div v-if="$store.state.user.type == 'AWAITING_ACCESS'" id="fullscreen-alert">
            <div>
                <h1>Hey, {{ $store.state.user.name }}!<br>Please wait until an admin activates your account.</h1>
                <a class="button gray" @click="pastefyAPI.logout()">Logout</a>
            </div>
        </div>
        <div v-else-if="$store.state.user.type == 'BLOCKED'" id="fullscreen-alert">
            <div>
                <h1>Hey, {{ $store.state.user.name }},<br>You have been blocked!</h1>
            </div>
        </div>

        <portal-target name="modals" />
    </div>
</template>
<script>
import Sidebar from "./components/sidebar/Sidebar.vue";

export default {
    components: {
        "side-bar": Sidebar
    }
}
</script>
<style lang="scss" scoped>
#page {
    min-height: 100%;
    padding: 30px;
    color: var(--text-color);
    padding-left: 420px;

    &.mobile {
        padding: 20px;
        padding-top: 80px;
    }
}

#fullscreen-alert {
    position: fixed;
    width: 100%;
    height: 100%;
    top: 0;
    left: 0;
    background: var(--obj-background-color);
    color: var(--text-color);
    z-index: 10000000;
    div {
        position: absolute;
        left: 50%;
        top: 50%;
        transform: translateX(-50%) translateY(-50%);
        text-align: center;
        .button {
            margin-top: 40px;
        }
    }
}
</style>