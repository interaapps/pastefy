import Vue from 'vue'
import VueRouter from 'vue-router'
import NotFound from '../views/NotFound.vue';

Vue.use(VueRouter)

const routes = [
    {
        path: "/",
        name: "Homepage",
        component: ()=> import('../views/Homepage')
    },
    {
        path: "/home",
        name: "Homepage",
        component: ()=> import('../views/Homepage')
    },
    {
        path: "/shared",
        name: "SharedPastes",
        component: ()=> import('../views/SharedPastes')
    },
    {
        path: "/settings",
        name: "Settings",
        component: ()=> import('../views/Settings')
    },
    {
        path: "/auth",
        name: "Authentication",
        component: ()=> import('../views/auth/LoginViaAPI')
    },
    {
        path: "/apikeys",
        name: "API-Keys",
        component: ()=> import('../views/ApiKeys')
    },
    {
        path: "/:id",
        name: "Paste",
        component: ()=> import('../views/Paste')
    },
    {
        path: "/folder/:id",
        name: "Folder",
        component: ()=> import('../views/Folder')
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