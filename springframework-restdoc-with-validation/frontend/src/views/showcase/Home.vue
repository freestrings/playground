<template>
    <div id="showcase">
        <h2>
            Showcase <small>작업중</small>
        </h2>

        <div class="block">
            <h3>
                Install Guide
            </h3>
            <ul>
                <li>환경 설정 및 실행</li>
                <li>코딩컨벤션 룰 정책</li>
            </ul>
            <hr>
        </div>

        <div class="block">
            <h3>
                Components Guide
            </h3>
            <ul>
                <li>
                    기본 템플릿 양식
                </li>
                <li> Datepicker </li>
                <li> Layer Popup </li>
                <li> Button </li>
                <li> Checkbox </li>
                <li> Select Box </li>
                <li> </li>
                <li>
                    <router-link to="/showcase/spinner">Spinner</router-link>
                </li>
                <li>
                    <router-link to="/showcase/notify">Notify</router-link>
                </li>
            </ul>
            <hr>
        </div>

        <div class="block">
            <h3>
                Sample Design
            </h3>
            <ul>
                <li>
                    <router-link to="/showcase/list">Vuex</router-link>
                </li>
                <li>
                    Search Box
                </li>
            </ul>
        </div>
    </div>
</template>

<script>
export default {
  name: 'showcase',
  components: {
    
  }
}
</script>
<style lang="less" scoped>
    h3 {
        border-left:3px solid #aaa;
        padding: 5px 15px;
        margin-bottom:5px;
        font-size: 1.5em;
        font-weight: bold;
    }
    .block {
        margin:15px 0;
    }
    ul{
        li{
            list-style: circle;
            margin-left:35px;
            font-size:14px;
            color: #aaa;
            a{
                color:#333;
            }
        }
    }
</style>