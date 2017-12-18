package wmp.tnc.product;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.AutoConfigureJsonTesters;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.requestParameters;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureRestDocs
@AutoConfigureMockMvc
@AutoConfigureJsonTesters
@DirtiesContext
@Import(TestConf.class)
public class ProductTests {

    @Autowired
    private MockMvc mockMvc;

    private JacksonTester<Product> json;

    @Autowired
    private ProductService productService;

    private final Product product10000 = Product
            .builder()
            .id(10000)
            .name("단둘이 여행!!")
            .images(Arrays.asList("/image1.png", "/image2.png"))
            .build();

    private final Product product10001 = Product
            .builder()
            .id(10001)
            .name("단둘이 여행!!")
            .images(Arrays.asList("/image1.png", "/image2.png"))
            .build();

    @Before
    public void setUp() {
        ObjectMapper objectMappper = new ObjectMapper();
        JacksonTester.initFields(this, objectMappper);
    }

    @Test
    public void testProduct() {
        Product product = productService.getProduct(10000);
        assertEquals(product10000, product);
    }

    @Test
    public void testProducts() {
        List<Product> products = productService.getProducts(Arrays.asList(10000, 10001));
        assertEquals(product10000, products.get(0));
        assertEquals(product10001, products.get(1));
    }

    @Test
    public void testProductApi() throws Exception {
        this.mockMvc
                .perform(get("/product/{productId}", 10000))
                .andExpect(status().isOk())
                .andExpect(content().json(json.write(product10000).getJson()))
                .andDo(
                        document("get-product", responseFields(
                                fieldWithPath("id").description("상품 ID"),
                                fieldWithPath("name").description("상품명"),
                                fieldWithPath("images").description("상품 이미지 리스트")
                        ))
                )
        ;
    }

    @Test
    public void testProductsApi() throws Exception {
        this.mockMvc
                .perform(get("/products?id=10000&id=10001"))
                .andExpect(status().isOk())
                .andExpect(content().json("[" +
                        json.write(product10000).getJson() +
                        ", " +
                        json.write(product10001).getJson() +
                        "]"
                ))
                .andDo(
                        document("get-products",
                                requestParameters(
                                        parameterWithName("id").description("상품 ID")
                                ),
                                responseFields(
                                        fieldWithPath("[]").description("배열 응답"),
                                        fieldWithPath("[].id").description("상품 ID"),
                                        fieldWithPath("[].name").description("상품명"),
                                        fieldWithPath("[].images").description("상품 이미지 리스트")
                                )
                        )
                )
        ;
    }
}
