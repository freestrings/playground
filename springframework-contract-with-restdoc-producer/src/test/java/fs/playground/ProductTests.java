package fs.playground;

import com.fasterxml.jackson.databind.ObjectMapper;
import fs.playground.model.ProductInfo;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.AutoConfigureJsonTesters;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.cloud.contract.wiremock.restdocs.SpringCloudContractRestDocs;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureRestDocs(outputDir = "target/snippets")
//
// AutoConfigureMockMvc을 하지 않으면 snippets/stubs/*.json이 생성되지 않는다.
//
@AutoConfigureMockMvc
@AutoConfigureJsonTesters
@DirtiesContext
public class ProductTests {

    @Autowired
    private MockMvc mockMvc;

    private JacksonTester<ProductInfo> json;

    @Before
    public void setUp() {
        ObjectMapper objectMappper = new ObjectMapper();
        JacksonTester.initFields(this, objectMappper);
    }

    @Test
    public void generateAdoc() throws Exception {
        this.mockMvc
                .perform(get("/product/{productId}", 1))
                .andExpect(status().isOk())
                .andExpect(
                        content().json(
                                json.write(ProductInfo.builder()
                                        .productId(1)
                                        .productName("상품이름1")
                                        .build()).getJson()
                        )
                )
                .andDo(
                        document("getProductAdoc", responseFields(
                                fieldWithPath("productId").description("상품 ID"),
                                fieldWithPath("productName").description("상품 이름")
                        ))
                );
    }

    @Test
    public void generateAdocContract() throws Exception {
        this.mockMvc
                .perform(get("/product/{productId}", 1))
                .andExpect(status().isOk())
                .andExpect(
                        content().json(
                                json.write(ProductInfo.builder()
                                        .productId(1)
                                        .productName("상품이름1")
                                        .build()).getJson()
                        )
                )
                .andDo(
                        document("getProductAdocContract", SpringCloudContractRestDocs.dslContract())
                );
    }
}
