package fs.playground

import org.hamcrest.Matchers
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers

@RunWith(SpringRunner::class)
@WebMvcTest
class ShowcaseTests {

    @Autowired
    lateinit var mvc: MockMvc

    @Test
    fun `showcase echo`() {
        mvc
                .perform(MockMvcRequestBuilders.get("/showcase/echo/testa"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.code", Matchers.`is`(0)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.payload", Matchers.`is`("testa")))
    }

    @Test
    fun `showcase dummy`() {
        mvc
                .perform(MockMvcRequestBuilders.get("/showcase/dummy?strValue=string&intValue=-1&arrayValue=ay1&arrayValue=ay2"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.code", Matchers.`is`(0)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.payload.strValue", Matchers.`is`("string")))
                .andExpect(MockMvcResultMatchers.jsonPath("$.payload.intValue", Matchers.`is`(-1)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.payload.arrayValue[0]", Matchers.`is`("ay1")))
                .andExpect(MockMvcResultMatchers.jsonPath("$.payload.arrayValue[1]", Matchers.`is`("ay2")))
    }
}
