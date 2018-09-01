<template>
    <div id="showcaseList">
        <h2>Showcase List</h2>

        <table>
            <thead>
            <tr>
                <th>No</th>
                <th>Subject</th>
                <th>Date</th>
            </tr>
            </thead>
            <tbody>
            <tr v-for="(item, index) in itemList" :key="index">
                <td>{{item.id}}</td>
                <td><a v-bind:href="`/showcase/detail/${item.id}`">{{item.subject}}</a></td>
                <td>{{item.date}}</td>
            </tr>
            </tbody>
        </table>
    </div>
</template>

<script>
import store from '@/store';
import {mapGetters, mapActions} from 'vuex';

export default {
    name: 'showcaseList',
    store,
    data() {
        return {

        };
    },
    computed: {
        ...mapGetters('showcase', {
            itemList: 'getItemList'
        })
    },
    mounted() {
        this.fetchItemList();
    },
    methods: {
        ...mapActions('showcase', {
            fetchItemList: 'fetchItemList'
        })
    }
};
</script>

<style lang="less" src="@/assets/less/showcase.less"></style>