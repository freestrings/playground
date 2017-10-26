package hello;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Builder
@Getter
@ToString
public class Order {

    private Integer orderId;

    private String orderName;

    private String address;
}
