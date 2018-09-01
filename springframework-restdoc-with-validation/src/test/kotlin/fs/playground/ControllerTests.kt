package fs.playground

import com.fasterxml.jackson.databind.ObjectMapper
import fs.playground.core.ResponseCode
import fs.playground.showcase.Dummy
import org.hamcrest.Matchers
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.stereotype.Controller
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.web.bind.annotation.RequestMapping
import javax.ws.rs.core.MediaType


@RunWith(SpringRunner::class)
@WebMvcTest
class ControllerTests {

    @Controller
    class ErrorController {

        @RequestMapping("/error")
        fun testError(): String {
            throw Exception("test error message")
        }

    }


    @Autowired
    lateinit var mvc: MockMvc

    private val objectMapper = ObjectMapper()

    /**
     * FIXME
     * ./gradlew clean 후 test 하면 에러남
     * 가령 ./gradlew clean test 또는 ./gradlew clean asciidoctor 하면 에러남
     *
     * ./gradlew clean test --tests ControllerTests 단일 실행은 문제 없음
     */
    //@Test
    fun `에러페이지 확인`() {
        mvc
                .perform(MockMvcRequestBuilders.get("/error"))
                .andExpect(MockMvcResultMatchers.status().is5xxServerError)
                .andExpect(MockMvcResultMatchers.jsonPath("$.code", Matchers.`is`(ResponseCode.UNEXPECTED_EXCEPTION_OCCURED.code)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.payload", Matchers.`is`("test error message")))
    }

    @Test
    fun `정적파일 읽기`() {
        mvc
                .perform(MockMvcRequestBuilders.get("/static.txt"))
                .andExpect(MockMvcResultMatchers.status().isOk)
    }

    @Test
    fun `Get Validation 테스트`() {
        mvc
                .perform(MockMvcRequestBuilders.get("/showcase/dummy"))
                .andExpect(MockMvcResultMatchers.status().isBadRequest)
                .andExpect(MockMvcResultMatchers.jsonPath("$.code", Matchers.`is`(ResponseCode.INVALID_PARAMETER.code)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.payload.field", Matchers.`is`("strValue")))
                .andExpect(MockMvcResultMatchers.jsonPath("$.payload.message", Matchers.`is`("Required String parameter 'strValue' is not present")))

        mvc
                .perform(MockMvcRequestBuilders.get("/showcase/dummy")
                        .param("strValue", "string")
                )
                .andExpect(MockMvcResultMatchers.status().isBadRequest)
                .andExpect(MockMvcResultMatchers.jsonPath("$.code", Matchers.`is`(ResponseCode.INVALID_PARAMETER.code)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.payload.field", Matchers.`is`("arrayValue")))
                .andExpect(MockMvcResultMatchers.jsonPath("$.payload.message", Matchers.`is`("Required String[] parameter 'arrayValue' is not present")))

        mvc
                .perform(MockMvcRequestBuilders.get("/showcase/dummy")
                        .param("strValue", "string")
                        .param("intValue", "101")
                        .param("arrayValue", "1")
                )
                .andExpect(MockMvcResultMatchers.status().isBadRequest)
                .andExpect(MockMvcResultMatchers.jsonPath("$.code", Matchers.`is`(ResponseCode.INVALID_PARAMETER.code)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.payload['dummy.arrayValue']", Matchers.`is`("size must be between 2 and 3")))
                .andExpect(MockMvcResultMatchers.jsonPath("$.payload['dummy.intValue']", Matchers.`is`("must be less than or equal to 100")))
    }

    @Test
    fun `Post Validation 테스트`() {
        mvc
                .perform(MockMvcRequestBuilders.post("/showcase/dummy")
                        .content(objectMapper.writeValueAsString(Dummy(strValue = "value", intValue = 11, arrayValue = arrayOf("1"))))
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(MockMvcResultMatchers.status().isOk)

        mvc
                .perform(MockMvcRequestBuilders.post("/showcase/dummy")
                        .content(objectMapper.writeValueAsString(Dummy(strValue = "value", intValue = 101, arrayValue = arrayOf("1"))))
                        .contentType(MediaType.APPLICATION_JSON)
                )

//                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isBadRequest)
                .andExpect(MockMvcResultMatchers.jsonPath("$.code", Matchers.`is`(ResponseCode.INVALID_PARAMETER.code)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.payload.dummy.strValue", Matchers.`is`("value")))
                .andExpect(MockMvcResultMatchers.jsonPath("$.payload.dummy.intValue", Matchers.`is`(101)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.payload.dummy.arrayValue[0]", Matchers.`is`("1")))

                .andExpect(MockMvcResultMatchers.jsonPath("$.payload['org.springframework.validation.BindingResult.dummy'][0].objectName", Matchers.`is`("dummy")))
                .andExpect(MockMvcResultMatchers.jsonPath("$.payload['org.springframework.validation.BindingResult.dummy'][0].field", Matchers.`is`("intValue")))
                .andExpect(MockMvcResultMatchers.jsonPath("$.payload['org.springframework.validation.BindingResult.dummy'][0].message", Matchers.`is`("must be less than or equal to 100")))
    }

}
