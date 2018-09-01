<template>
    <div id="wrap" class="ui_guide">
        <headers></headers>

        <div id="container">
            <div class="container_wrap">
                <sidebar></sidebar>
                <div id="contents">
                    <div class="inner_wrap">
                        <div class="section">

                            <router-view/>

                        </div>
                    </div>
                </div>
            </div>
        </div>

        <footers></footers>
    </div>
</template>

<script>
    import Headers from '@/components/layout/Header.vue';
    import Sidebar from '@/components/layout/Sidebar.vue';
    import Footers from '@/components/layout/Footer.vue';
    import '@/assets/css/layout.css';
    export default {
        components: {
            Headers,
            Sidebar,
            Footers
        },
        name: 'wrap',
    };
</script>

<style>
    #contents h2 {
        border-bottom: 1px solid #7858a1;
        padding-bottom: 10px;
        margin-bottom:20px;
        font-size:2em;
        small{
            font-size: 0.75em;

        }
    }
</style>