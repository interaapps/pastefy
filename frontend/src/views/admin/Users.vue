<template>
    <div>
        <Header />
        <input v-model="search" class="input" placeholder="Search" @change="page = 1; loadUsers()" @keydown.enter="page = 1; loadUsers()" style="max-width: 1200px">
        <div id="users" >
            <div v-for="(user, i) of users" :key="user.id">
                <img :src="user.avatar">
                <div class="name">
                    <h1>{{ user.unique_name }}</h1>
                    <h2>{{ user.name }}</h2>
                </div>
                <div class="mail">
                    {{ user.e_mail }} ({{ user.auth_provider }})
                </div>
                <div class="action-buttons">
                    <router-link :to="`/admin/pastes?q=${ encodeURIComponent(`user_id=${ user.id }`) }`" class="button small" style="margin-right: 10px">Pastes</router-link>

                    <a v-if="user.type === 'AWAITING_ACCESS'" class="button small" style="margin-right: 10px;" @click="setUserType(user.id, 'USER')">Grant Access</a>
                    <a v-else-if="user.type === 'BLOCKED'" class="button small" style="margin-right: 10px;" @click="setUserType(user.id, 'USER')">UNBLOCK</a>
                    <a v-else-if="user.type === 'USER'" class="button small delete" style="margin-right: 10px;" @click="$refs.blockUserConfirmation[i].open()">BLOCK</a>
                    <a class="button small delete" @click="$refs.deleteUserConfirmation[i].open()">Remove</a>


                    <ConfirmationModal ref="deleteUserConfirmation" title="Delete user?" @confirm="removeUser(user.id)">
                        Do you really want to delete the user '{{ user.name }}'?
                    </ConfirmationModal>
                    <ConfirmationModal ref="blockUserConfirmation" title="Block user?" @confirm="setUserType(user.id, 'BLOCKED')">
                        Do you really want to block the user '{{ user.name }}'?
                    </ConfirmationModal>
                </div>

            </div>
            <a @click="page -= 1; loadUsers()" class="button">PREVIOUS PAGE</a>
            <a @click="page += 1; loadUsers()" style="float: right;" class="button">NEXT PAGE</a>
        </div>
    </div>
</template>

<script>
import Header from "@/components/admin/Header";
import {buildSearchAndFilterQuery} from "@/helper";
import ConfirmationModal from '@/components/ConfirmationModal.vue'
export default {
    name: "Users",
    components: {ConfirmationModal, Header},
    data: () => ({
        users: [],
        search: '',
        page: 1
    }),
    mounted() {
        this.loadUsers()
    },
    methods: {
        async loadUsers() {
            this.users = await this.pastefyAPI.get("/api/v2/admin/users", {page: this.page, ...buildSearchAndFilterQuery(this.search)})
        },
        async removeUser(id) {
            await this.pastefyAPI.delete(`/api/v2/admin/users/${ id }`)
            await this.loadUsers()
        },
        async setUserType(id, type) {
            await this.pastefyAPI.put(`/api/v2/admin/users/${ id }`, {
                type
            })
            await this.loadUsers()
        }
    }
}
</script>

<style lang="scss" scoped>
#users {
    max-width: 1200px;

    > div {
        background: var(--background-color);
        padding: 10px;
        margin-top: 10px;
        border-radius: 10px;

        > * {
            display: inline-block;
            vertical-align: middle;
            overflow: hidden;
        }
        img {
            width:  45px;
            height: 45px;
            border-radius: 25px;
        }
        .name {
            width: 28%;
            margin-left: 20px;
            h1 {
                font-size: 20px;
            }
            h2 {
                font-size: 16px;
                margin-top: -5px;
                opacity: 0.4;
            }
        }
        .mail {
            width: 33%;
        }
        .action-buttons {
            float: right;
        }
    }
}
</style>