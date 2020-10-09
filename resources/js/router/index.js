import Vue from 'vue'
import VueRouter from 'vue-router'
import Homepage from '../views/Homepage.vue';
import Paste from '../views/Paste.vue';

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
        path: "/:id",
        name: "Paste",
        component: Paste
    }
]

const router = new VueRouter({
    mode: 'history',
    base: process.env.BASE_URL,
    routes: routes
})


export default router