package wmp.tnc.product;

import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class ProductService {

    public Product getProduct(int id) {
        return Product
                .builder()
                .id(id)
                .name("단둘이 여행!!")
                .images(Arrays.asList("/image1.png", "/image2.png"))
                .build();
    }

    public List<Product> getProducts(List<Integer> productIds) {
        //
        // 실제는 product 호출을 N번 하면 안댐...
        //
        return productIds.stream().map(pid -> getProduct(pid)).collect(Collectors.toList());
    }
}
