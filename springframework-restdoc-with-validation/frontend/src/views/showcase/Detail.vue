<template>
    <div id="showcaseDetail">
        <h1>Showcase Detail</h1>
        <table>
            <tr>
                <td>Subject</td>
                <td>
                    {{id}} - {{item.subject}}
                </td>
            </tr>
            <tr>
                <td>Content</td>
                <td>
                    {{item.content}}
                </td>
            </tr>
        </table>
    </div>
</template>

<script>
import store from '@/store';
import { mapGetters, mapActions } from 'vuex';

export default {
    name: 'showcaseDetail',
    store,
    data() {
        return {
            id: null
        }
    },
    computed: mapGetters({
        item: 'showcase/getItemDetail'
    }),
    mounted() {
        this.id = this.$route.params.id;

        this.fetchItemDetail(this.id);
    },
    methods: {
        ...mapActions({
            fetchItemDetail: 'showcase/fetchItemDetail',
        })
    }
};
</script>

<style lang="less" scoped >
    table{
        width: 100%;
        border-top: 1px solid #ad95cc;
        border-bottom: 1px solid #ad95cc;
        th {
            &:first-child {
                border-bottom: 1px solid #f3f3f3;
            }
            background-color: #faf9fa;
            padding: 10px 0;
            border-right: 1px solid #d8d8d8;
            font-size:14px;
            font-weight: bold;
        }

        td {
            padding: 10px;
            border-bottom: 1px solid #f3f3f3;
        }
    }
</style>