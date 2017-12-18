package wmp.tnc;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.hamcrest.core.IsEqual;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.AutoConfigureJsonTesters;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import wmp.tnc.product.Product;
import wmp.tnc.product.ProductService;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureJsonTesters
@AutoConfigureMockMvc
@AutoConfigureWireMock(stubs = "classpath:/META-INF/wmp.tnc/demo-backend-api/**/*.json", port = 8090)
@DirtiesContext
public class ProductTests {

    @Autowired
    private MockMvc mockMvc;

    private JacksonTester<Product> json;

    @Autowired
    private ProductService productService;

    private Product product10000 = Product
            .builder()
            .id(10000)
            .name("단둘이 여행!!")
            .images(Arrays.asList("/image1.png", "/image2.png"))
            .build();

    private Product product10001 = Product
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
    public void testProduct() throws Exception {
        Product product = productService.getProduct(10000);
        product.equals(product10000);
    }

    @Test
    public void testProducts() throws Exception {
        List<Product> products = productService.getProducts(Arrays.asList(10000, 10001));
        assertEquals(product10000, products.get(0));
        assertEquals(product10001, products.get(1));
    }

    @Test
    public void testProductView() throws Exception {

        this.mockMvc
                .perform(get("/product/{productId}", 10000))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(model().attribute("product", new IsEqual(product10000)
                ));
    }

    @Test
    public void testProductsView() throws Exception {
        this.mockMvc
                .perform(get("/products?id=10000&id=10001"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(model().attribute("products", new IsEqual(Arrays.asList(product10000, product10001))));
    }

    @Test
    public void testProductApiViaProxy() throws Exception {
        this.mockMvc
                .perform(get("/product-api/product/{productId}", 10000))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().json(json.write(product10000).getJson()));
    }
}
