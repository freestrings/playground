package hello;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.restdocs.request.RequestParametersSnippet;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.requestParameters;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureRestDocs
public class SpringframeworkRestdocApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void index1() throws Exception {
        this.mockMvc.perform(get("/"))
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
        this.mockMvc.perform(get("/"))
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
        this.mockMvc.perform(get("/weather"))
                .andExpect(status().isOk())
                .andDo(document("weather1", responseBody(
                        beneathPath("weather.temperature").withSubsectionId("temperature")
                )));
    }

    @Test
    public void weather2() throws Exception {
        this.mockMvc.perform(get("/weather"))
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
    public void paging() throws Exception {
        this.mockMvc
                .perform(
                        get("/paging")
                )
                .andExpect(status().isOk())
                .andDo(document("parameter-paging"));
    }

    private RequestParametersSnippet requestParametersSnippet = requestParameters(
            parameterWithName("page").description("검색 할 페이지, 옵션, 기본값 1"),
            parameterWithName("perPage").description("페이지 당 개수, 옵션, 기본값 10")
    );

    @Test
    public void paging1() throws Exception {


        this.mockMvc
                .perform(
                        get("/paging")
                                .param("page", "1")
                                .param("perPage", "10")
                )
                .andExpect(status().isOk())
                .andDo(document("parameter-paging1", requestParametersSnippet));
    }

    @Test
    public void paging2() throws Exception {
        this.mockMvc
                .perform(
                        get("/paging")
                                .param("page", "2")
                                .param("perPage", "10")
                )
                .andExpect(status().isOk())
                .andDo(document("parameter-paging2", requestParametersSnippet));
    }
}
