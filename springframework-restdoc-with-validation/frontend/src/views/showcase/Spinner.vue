<template>
    <div class="spinners">
        <div>
            일반 스피너 : <spinner :show="true"></spinner>
        </div>
        <hr>
        <div>
            사이즈 조절 :
            <spinner :show="true" size="30px"></spinner>
            <spinner :show="true" :size="size"></spinner>
        </div>
        <hr>
        <div>
            Dimmend 처리
            <button type="button" @click="onDimmend">DIMMEND ON/OFF</button>
            <spinner :dimmend="dimmend"></spinner>
        </div>
        <hr>
        <div>
            버튼 Spinner 처리
            <button @click="onLoading">
                <spinner :show="isLoading" size="15px"></spinner>
                전송
            </button>
        </div>
    </div>
</template>

<script>
    import Spinner from '@/components/globals/Spinner.vue';

    export default {
        components: {
            Spinner,
        },
        data: () => {
           return {
               size: '200px',
               dimmend: false,
               isLoading: false
           }
        } ,
        created () {
        },
        methods:{
            onLoading () {
                this.isLoading = !this.isLoading;
            },
            onDimmend () {
                this.dimmend = true;
                window.setTimeout(() => {
                    this.dimmend = false;
                }, 3000)
            }

        }
    }
</script>