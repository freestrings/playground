package wmp.tnc.product;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class ProductController {

    @Autowired
    private ProductService productService;

    @GetMapping("/product/{productId}")
    public Product product(@PathVariable Integer productId) {
        return productService.getProduct(productId);
    }

    @GetMapping("/products")
    public List<Product> products(@RequestParam("id") List<Integer> productIds) {
        return productService.getProducts(productIds);
    }
}
