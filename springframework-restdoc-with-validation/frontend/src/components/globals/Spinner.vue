<template>
    <div class='spinnerWrap' :class="{'dimmend': opt.dimmend, 'show': opt.show}">
        <span><circle2 :size="opt.size"></circle2></span>
    </div>
</template>
<script>
    import {Circle2} from 'vue-loading-spinner';
    export default {
        name: 'Spinner',
        components: {
            Circle2
        },
        props:{
            size: String,
            dimmend: Boolean,
            show: Boolean
        },
        data: () => {
            return {
                opt : {
                    size:  '30px',
                    dimmend: false,
                    show: false
                }
            }
        },
        created () {
            this.init();
        },
        watch: {
            show (val) {
                this.opt.show = val;
            },
            dimmend (val) {
                this.opt.dimmend = val;
            }
        },
        methods:{
            init () {
                this.opt.size = this.size || '120px';
                this.opt.show = this.show;
            }
        }
    }
</script>
<style lang="less" scoped>
    .spinnerWrap {
        display: none;
        &.show{
            display: inline-block;
        }
    }

    .dimmend {
        background-color: rgba(0,0,0,0.5);
        width: 100%;
        height: 100%;
        position: fixed;
        top:0;
        left:0;
        transition: all 0.3s ease-in-out;
        display: inline-block;
        span {
            position: absolute;
            top:50%;
            left:50%;
            transform: translate(-50% ,-50%);
        }
    }

</style>