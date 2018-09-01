import api from '@/api/showcase.js';
import * as types from './mutation-types.js';

const state = {
    itemList: [],
    itemDetail: {}
};

const getters = {
    getItemList: state => state.itemList,
    getItemDetail: state => state.itemDetail
};

const actions = {
    fetchItemList: async ({commit}) => {
        const response = await api.getItemList();
        commit(`${types.ITEM_LIST}`, response.data);
    },
    fetchItemDetail: async ({commit}, id) => {
        const response = await api.getItemDetail(id);
        commit(`${types.ITEM_DETAIL}`, response.data);
    }
};

const mutations = {
    [types.ITEM_LIST](state, itemList) {
        state.itemList = itemList;
    },

    [types.ITEM_DETAIL](state, itemDetail) {
        state.itemDetail = itemDetail;
    }
};

const namespaced = true;

export default {
    namespaced,
    state,
    getters,
    actions,
    mutations
};