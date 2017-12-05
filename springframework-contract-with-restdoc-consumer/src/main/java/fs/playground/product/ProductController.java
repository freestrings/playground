package fs.playground.product;

import fs.playground.product.model.ProductInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.net.URI;

@RestController
public class ProductController {

    @Autowired
    private RestTemplate restTemplate;

    @GetMapping("/productName/{productId}")
    public String getName(@PathVariable Integer productId) {
        ResponseEntity<ProductInfo> response = this.restTemplate.exchange(
                RequestEntity.get(URI.create("http://localhost:9090/product/" + productId)).build(),
                // stub의 ProductInfo 아님
                ProductInfo.class
        );
        return response.getBody().getProductName();
    }
}
