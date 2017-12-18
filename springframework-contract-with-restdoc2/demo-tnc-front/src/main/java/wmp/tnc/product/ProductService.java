package wmp.tnc.product;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class ProductService {

    @Autowired
    private RestTemplate restTemplate;

    @Value("${host.product}")
    private String host;

    public Product getProduct(Integer productId) {
        return restTemplate.getForObject(host + "/product/" + productId, Product.class);
    }

    public List<Product> getProducts(List<Integer> productIds) {
        String params = productIds.stream().map(pid -> "id=" + pid.toString()).collect(Collectors.joining("&"));
        return Arrays.asList(restTemplate.getForObject(host + "/products?" + params, Product[].class));
    }
}
