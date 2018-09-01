package fs.playground.showcase

import fs.playground.core.Response
import fs.playground.core.ResponseCode
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*
import javax.validation.Valid
import javax.validation.constraints.Max
import javax.validation.constraints.Size

@RestController
@RequestMapping("/showcase")
//
// 실제 요청에서 GetMapping Validation을 하려면 class 수준에 정의해야 한다.
// @see ControllerTests.`Get Validation 테스트`
@Validated
//
//
class ShowcaseController {

    @GetMapping("/echo/{value}")
    fun echo(@PathVariable value: String): Response<String> = Response.build {
        code = ResponseCode.SUCCESS
        payload = value
    }

    @GetMapping("/dummy")
    fun dummy(
            @RequestParam(value = "strValue") strValue: String,
            @Max(value = 100) @RequestParam(value = "intValue", required = false, /* required가 false일 경우, 값이 null 이면 java.lang.IllegalStateException가 발생한다. 기본값을 지정해야 한다. */ defaultValue = "-1") intValue: Int,
            @Size(min = 2, max = 3) @RequestParam(value = "arrayValue") arrayValue: Array<String>): Response<Dummy> = Response.build {
        code = ResponseCode.SUCCESS
        payload = Dummy(strValue = strValue, intValue = intValue, arrayValue = arrayValue)
    }

    @PostMapping("/dummy")
    fun dummy(@Valid @RequestBody dummyDto: Dummy) = Response.build<Dummy> {
        code = ResponseCode.SUCCESS
        payload = dummyDto
    }
}
