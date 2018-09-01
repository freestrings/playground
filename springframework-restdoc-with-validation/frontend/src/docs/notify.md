# Toastr Alert 사용가이드

## Toastr 타입 사용예시
``` 
- show : 커스텀 Toastr를 사용해야 할때 사용
- success : 사용자의 액션이 정상적으로 이루어졌을때 사용
- error : 사용자의 액션이 비정상적으로 이루어졌을때 사용
- warning : 시스템의 의해 사용자 액션이 차단 되었을때 사용
- Info : 시스템에서 사용자에게 정보를 전달할때 사용
```

## 사용 예시
```js
// Basic alert
this.$noty.show("Hello world!")

// Success notification
this.$noty.success("Your profile has been saved!")

// Error message
this.$noty.error("Oops, something went wrong!")

// Warning
this.$noty.warning("Please review your information.")

// Basic alert
this.$noty.info("New version of the app is available!")
```

## 참고 사이트
- [Noty 가이드 문서](https://ned.im/noty/#/options)
