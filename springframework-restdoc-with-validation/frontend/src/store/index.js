import Vue from 'vue';
import Vuex from 'vuex';
import showcase from './modules/showcase/store';

Vue.use(Vuex);

export default new Vuex.Store({
    modules: {
        showcase
    }
});