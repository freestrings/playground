import axios from 'axios';
import mockAdapter from 'axios-mock-adapter';
import itemList from './mock-item-list.js';
import itemDetail from './mock-item-detail.js';

const mock = new mockAdapter(axios);

mock.onGet('/items').reply(200, [
    ...itemList
]);

mock.onGet('/item/1').reply(200, {
    ...itemDetail
});

export default {
    fetch(flag) {
        if (flag == false) {
            mock.restore()
        }
    }
}