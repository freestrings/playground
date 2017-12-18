package wmp.tnc.product;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class ProductController {

    @Autowired
    private ProductService productService;

    @RequestMapping("/")
    public String home(Model model) {
        return "home";
    }

    @RequestMapping(value = "/product/{productId}")
    public String productDetail(@PathVariable Integer productId, Model model) {
        Product product = productService.getProduct(productId);
        model.addAttribute("title", product.getName());
        model.addAttribute("product", product);
        return "productDetail";
    }

    @RequestMapping(value = "/products")
    public String productList(@RequestParam("id") List<Integer> productIds, Model model) {
        List<Product> products = productService.getProducts(productIds);
        model.addAttribute("products", products);
        return "productList";
    }
}
