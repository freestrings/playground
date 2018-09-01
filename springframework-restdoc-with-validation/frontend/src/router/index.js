import Vue from 'vue';
import VueRouter from 'vue-router';
import ShowcaseRouter from './showcase.js';

Vue.use(VueRouter);

export default new VueRouter({
    mode: 'history',
    routes: [
        {
            path: '/',
            component: () => import('@/views/Home.vue')
        },
        ...ShowcaseRouter
    ]
});