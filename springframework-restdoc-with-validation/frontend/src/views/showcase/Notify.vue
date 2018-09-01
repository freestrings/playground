<template>
    <div>
        <h1>Toastr Alert Sample</h1>
        <button @click="notifyOn(1)">Basic</button>
        <button @click="notifyOn(2)">Success</button>
        <button @click="notifyOn(3)">Error</button>
        <button @click="notifyOn(4)">Warning</button>
        <button @click="notifyOn(5)">Info</button>
        <br><br><br><br><br>
        <md-notify class="markdown-body"></md-notify>
    </div>
</template>

<script>
import mdNotify from '@/docs/notify.md';
import 'highlight.js/styles/github.css'
import 'github-markdown-css'
export default {
    name:'Notify',
    components: {
      mdNotify
    },
    created () {
        console.log('Created::')
    },
    methods: {
        notifyOn(type = 1) {
            switch(type) {
                case 1: this.$noty.show("Hello world!"); break;
                case 2: this.$noty.success("Hello world!"); break;
                case 3: this.$noty.error("Hello world!"); break;
                case 4: this.$noty.warning("Hello world!"); break;
                case 5: this.$noty.info("Hello world!"); break;
            }
        }
    }
}
</script>

<style lang="less" scoped>
    button{
        border: 1px solid #666666;
        padding: 10px 15px;
        border-radius: 5px;
        margin:5px;
        transition: all 0.3s ease-in-out;
        cursor: pointer;
        &:hover{
            background-color: #666666;
            color:#d8d8d8;
        }
    }
</style>