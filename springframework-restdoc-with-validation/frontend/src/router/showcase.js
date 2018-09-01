import Home from '@/views/showcase/Home.vue';
import List from '@/views/showcase/List.vue';
import Detail from '@/views/showcase/Detail.vue';
import Spinner from '@/views/showcase/Spinner.vue';
import Notify from '@/views/showcase/Notify.vue';

export default [
    {
        path: '/showcase',
        component: Home
    },
    {
        path: '/showcase/list',
        component: List
    },    
    {
        path: '/showcase/detail/:id',
        component: Detail
    },
    {
        path:'/showcase/spinner',
        component: Spinner
    },
    {
        path:'/showcase/notify',
        component: Notify
    }
];