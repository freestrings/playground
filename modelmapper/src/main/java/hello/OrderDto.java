package hello;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class OrderDto {

    private Integer orderId;

    private String orderName;

}
