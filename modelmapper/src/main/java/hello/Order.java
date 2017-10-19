package hello;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class Order {

    private Integer orderId;

    private String orderName;
}
