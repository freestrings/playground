package hello;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.restdocs.JUnitRestDocumentation;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.requestParameters;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
public class SpringframeworkRestdocApplicationTests {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext context;

    @Rule
    public JUnitRestDocumentation restDocumentation = new JUnitRestDocumentation("api-docs");

    @Before
    public void setUp() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.context)
                .apply(documentationConfiguration(this.restDocumentation))
                .build();
    }

    @Test
    public void index1() throws Exception {
        this.mockMvc.perform(get("/").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(document("index1",
                        responseFields(
                                fieldWithPath("books[].title").description("제목"),
                                fieldWithPath("books[].author").description("저자")
                        ))
                );
    }

    @Test
    public void index2() throws Exception {
        this.mockMvc.perform(get("/").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(document("index2", responseFields(
                        fieldWithPath("books").description("책 리스트"))
                        .andWithPrefix("books[].",
                                fieldWithPath("title").description("제목"),
                                fieldWithPath("author").description("저자")
                        )));
    }

    @Test
    public void weather1() throws Exception {
        this.mockMvc.perform(get("/weather").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(document("weather1", responseBody(
                        beneathPath("weather.temperature").withSubsectionId("temperature")
                )));
    }

    @Test
    public void weather2() throws Exception {
        this.mockMvc.perform(get("/weather").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(document("weather2",
                        responseBody(
                                beneathPath("weather.temperature").withSubsectionId("temperature")
                        ),
                        responseFields(
                                beneathPath("weather.temperature").withSubsectionId("temperature"),
                                fieldWithPath("high").description("최고온도"),
                                fieldWithPath("low").description("최저온도")
                        )));
    }

    @Test
    public void request1() throws Exception {
        this.mockMvc.perform(get("/paging?page=2&perPage=10").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(document("parameter-paging", requestParameters(
                        parameterWithName("page").description("검색 할 페이지"),
                        parameterWithName("perPage").description("페이지 당 개수")
                )));
    }
}
