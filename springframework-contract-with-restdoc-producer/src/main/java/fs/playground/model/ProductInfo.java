package fs.playground.model;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class ProductInfo {

    private Integer productId;

    private String productName;
}
