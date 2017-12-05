package fs.playground.product;

import fs.playground.model.ProductInfo;
import org.springframework.stereotype.Component;

@Component
public class ProductService {

    public ProductInfo getProductInfo(Integer productId) {
        return ProductInfo.builder()
                .productId(productId)
                .productName("상품이름" + productId)
                .build();
    }
}
