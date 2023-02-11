import Vue from 'vue'
import VueRouter from 'vue-router'
import NotFound from '../views/NotFound.vue';
import LoginViaAPI from '../views/auth/LoginViaAPI.vue';

Vue.use(VueRouter)
const routes = [
    {
        path: "/",
        name: "Homepage",
        component: () => import('../views/Homepage')
    },
    {
        path: "/home",
        name: "Homepage",
        component: () => import('../views/Homepage')
    },
    {
        path: "/public",
        name: "Public",
        component: () => import('../views/PublicPastes')
    },
    {
        path: "/shared",
        name: "SharedPastes",
        component: () => import('../views/SharedPastes')
    },
    {
        path: "/settings",
        name: "Settings",
        component: () => import('../views/Settings')
    },
    {
        path: "/login-with",
        name: "Login with",
        component: () => import('../views/auth/LoginSelection')
    },
    {
        path: "/auth",
        name: "Authentication",
        component: LoginViaAPI
    },
    {
        path: "/apikeys",
        name: "API-Keys",
        component: () => import('../views/ApiKeys')
    },
    {
        path: "/admin/overview",
        name: "Admin Overview",
        component: () => import('../views/admin/Overview'),
    },
    {
        path: "/admin",
        redirect: "/admin/overview"
    },
    {
        path: "/admin/users",
        name: "Admin Users",
        component: () => import('../views/admin/Users')
    },
    {
        path: "/admin/pastes",
        name: "Admin Pastes",
        component: () => import('../views/admin/Pastes')
    },
    {
        path: "/:id",
        name: "Paste",
        component: () => import('../views/Paste')
    },
    {
        path: "/folder/:id",
        name: "Folder",
        component: () => import('../views/Folder')
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