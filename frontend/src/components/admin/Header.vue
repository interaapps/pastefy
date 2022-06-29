<template>
    <div>
        <h1>Admin</h1>
        <div>
            <router-link to="/admin/overview" class="tab">Overview</router-link>
            <router-link to="/admin/users" class="tab">Users</router-link>
            <router-link to="/admin/pastes" class="tab">Pastes</router-link>
        </div>
        <br>
    </div>
</template>

<script>
export default {
    name: "Header"
}
</script>

<style lang="scss" scoped>
h1 {
    margin-bottom: 10px;
}
.tab {
    display: inline-block;
    padding: 6px 9px;
    margin-right: 10px;
    background: var(--tab-color);
    border-radius: 10px;
    text-decoration: none;

    &.router-link-active {
        background: var(--tab-color-selected);
    }
}
</style>