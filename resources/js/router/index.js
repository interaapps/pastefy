import Vue from 'vue'
import VueRouter from 'vue-router'
import Homepage from '../views/Homepage.vue';
import Paste from '../views/Paste.vue';
import Folder from '../views/Folder.vue';
import NotFound from '../views/NotFound.vue';
import Settings from '../views/Settings.vue';
import SharedPastes from '../views/SharedPastes.vue';

Vue.use(VueRouter)

const routes = [
    {
        path: "/",
        name: "Homepage",
        component: Homepage
    },
    {
        path: "/home",
        name: "Homepage",
        component: Homepage
    },
    {
        path: "/shared",
        name: "SharedPastes",
        component: SharedPastes
    },
    {
        path: "/settings",
        name: "Settings",
        component: Settings
    },
    {
        path: "/:id",
        name: "Paste",
        component: Paste
    },
    {
        path: "/folder/:id",
        name: "Folder",
        component: Folder
    },
    {
        path: "/*",
        name: "Page not Found",
        component: NotFound
    }
]

const router = new VueRouter({
    mode: 'history',
    base: process.env.BASE_URL,
    routes: routes
})


export default router