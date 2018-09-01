# Router 가이드
: Template에서 사용되는 Router 파일의 설정 가이드

[Router 설정](#setting)
 
## Router 설정
> /router/<파일명>.js 
```
# 설정파일 구성
import <Alias> from '@/views/<PATH>.vue'
export default [
    {
        path: '<URI>', // 브라우저의 URL경로 
        component: <Alias>, // 해당 URI로 접근시 보여줄 Html 파일
        
        ---
        name: <NAME>, // 해당설정의 Name Key 별도 링크를 이용할때 사용가능
        redirect: <URI>, // path의 경로 접근 되면 Redirect 주소 
        
    }
]
```

> /router/store.js
```
# Router 설정 등록
import <Alias> from '@/router/<파일명>.js'

export default new Router({
  routes: [
    ...<Alias>
  ]
})
```


## 참고 문서
> [VueJS Router 사용법](https://router.vuejs.org/kr)

> [Vue-Router 공식 MD 가이드](https://github.com/vuejs/vue-router/tree/dev/docs/kr)
