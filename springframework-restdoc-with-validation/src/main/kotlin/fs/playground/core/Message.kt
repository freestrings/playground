package fs.playground.core

import com.fasterxml.jackson.annotation.JsonValue

enum class ResponseCode(@get: JsonValue val code: Int) {

    /**
     * 성공
     */
    SUCCESS(0),

    /**
     * 지원하지 않는 버전
     */
    UNSUPPOTED_VERSION(100),

    /**
     * 파라메터 입력값 오류
     */
    INVALID_PARAMETER(200),

    /**
     * 인증되지 않음
     */
    UNAUTHORIZED(300),

    /**
     * 쿠키 오류
     */
    INVALID_COOKIE(400),

    /**
     * 내부 api 호출 오류
     */
    INTERNAL_API_CALL_ERROR(500),

    /**
     * Database 오류
     */
    DATABASE_ERROR(600),

    /**
     * 알수 없는 에러
     */
    UNEXPECTED_EXCEPTION_OCCURED(1000);

}

class Response<T>(

        /**
         * 요청 URL에 포함된 버전 정보
         */
        val version: String = "0.1.0",

        /**
         * 상태코드
         */
        val code: ResponseCode,

        /**
         * 에러일때 메세지나 응답값. (String, JSON)
         */
        val payload: T?
) {

    private constructor(builder: Builder<T>) : this(code = builder.code, payload = builder.payload)

    companion object {
        inline fun <T> build(block: Builder<T>.() -> Unit) = Builder<T>().apply(block).build()
    }

    class Builder<T> {
        var code: ResponseCode = ResponseCode.SUCCESS
        var payload: T? = null

        fun build() = Response<T>(this)
    }
}
