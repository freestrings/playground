package fs.playground

import com.fasterxml.jackson.databind.ObjectMapper
import fs.playground.showcase.Dummy
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.web.servlet.MockMvc


@RunWith(SpringRunner::class)
@WebMvcTest
@AutoConfigureRestDocs
@DirtiesContext
class RestDocTests {

    @Autowired
    lateinit var mvc: MockMvc

    @Test
    fun `Echo GET - PathVariable`() {
        mvc
                .perform(RestDocumentationRequestBuilders.get("/showcase/echo/{value}", "testa"))
                .andDo(MockMvcRestDocumentation.document(
                        "showcase-echo-get", // 문서명
                        responsePrettyPrint(),
                        DocBuilder.requestParams()
                                .with("value", "입력값이 되돌아 옴")
                                .asPathParameters()
                        ,
                        DocBuilder.payloadFields()
                                .with(path = "payload", desc = "입력값")
                                .asResponseFields()
                ))
    }

    @Test
    fun `Dummy GET - Parameter`() {
        mvc
                .perform(RestDocumentationRequestBuilders.get("/showcase/dummy")
                        .param("strValue", "string")
                        .param("intValue", (-1).toString())
                        .param("arrayValue", "ay1", "ay2"))
                .andDo(MockMvcRestDocumentation.document(
                        "showcase-dummy-get", // 문서명
                        responsePrettyPrint(),
                        DocBuilder.requestParams()
                                .with(name = "strValue", desc = "문자열 파라메터")
                                .with(name = "intValue", desc = "숫자 파라메터")
                                .with(name = "arrayValue", desc = "배열 파라메터")
                                .asRequestParams()
                        ,
                        DocBuilder.payloadFields()
                                .with(path = "payload.strValue", desc = "문자열 응답")
                                .with(path = "payload.intValue", desc = "숫자 응답")
                                .with(path = "payload.arrayValue", desc = "배열 응답", type = "String[]")
                                .asResponseFields()
                ))
    }

    @Test
    fun `Dummy POST`() {
        mvc
                .perform(RestDocumentationRequestBuilders.post("/showcase/dummy")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(ObjectMapper().writeValueAsString(
                                Dummy(strValue = "리턴백", intValue = 99, arrayValue = arrayOf("리턴백1")))))
//                .andDo(MockMvcResultHandlers.print())
                .andDo(MockMvcRestDocumentation.document(
                        "showcase-dummy-post", // 문서명
                        requestPrettyPrint(),
                        responsePrettyPrint(),
                        DocBuilder.payloadConstrainedFields(Dummy::class.java)
                                .with(path = "strValue", desc = "입력 문자열")
                                .with(path = "intValue", desc = "입력 숫자")
                                .with(path = "arrayValue", desc = "입력 배열", type = "String[]")
                                .asRequestFields()
                        ,
                        DocBuilder.payloadFields()
                                .with(path = "payload.strValue", desc = "문자열 응답")
                                .with(path = "payload.intValue", desc = "숫자 응답")
                                .with(path = "payload.arrayValue", desc = "배열 응답", type = "String[]")
                                .asResponseFields()
                ))
    }
}
