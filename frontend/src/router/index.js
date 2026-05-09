import { createRouter, createWebHistory } from 'vue-router'

const routes = [
    {
        path: '/',
        redirect: '/admin'
    },
    {
        path: '/admin',
        component: () => import('../views/admin/ActivityList.vue')
    },
    {
        path: '/admin/activity/:id',
        component: () => import('../views/admin/ActivityDetail.vue')
    },
    {
        path: '/checkin/:token',
        component: () => import('../views/public/Checkin.vue')
    },
    {
        path: '/checkin/success',
        component: () => import('../views/public/CheckinSuccess.vue')
    }
]

const router = createRouter({
    history: createWebHistory(),
    routes
})

export default router
