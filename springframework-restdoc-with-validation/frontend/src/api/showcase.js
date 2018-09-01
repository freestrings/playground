import axios from 'axios';
import mock from '@/api/mock/showcase';

mock.fetch(true);

export default {
    getItemList() {
        return axios.get('/items');
    },
    getItemDetail(id) {
        return axios.get(`/item/${id}`);
    }
};