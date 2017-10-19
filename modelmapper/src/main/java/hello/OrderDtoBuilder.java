package hello;

import java.util.function.Function;

public class OrderDtoBuilder implements Function<Order, OrderDto> {

    @Override
    public OrderDto apply(Order order) {
        return OrderDto.builder()
                .orderId(order.getOrderId())
                .orderName(order.getOrderName())
                .build();
    }
}
