package hello;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class Main {

    public static void main(String... args) {
//        ModelMapper modelMapper = new ModelMapper();
//        Order order = new Order();
//        order.setOrderId(1);
//        order.setOrderName("name");
//        OrderDto orderDto = modelMapper.map(order, OrderDto.class);
//        System.out.println(orderDto.getOrderId());
//        System.out.println(orderDto.getOrderName());

        Order order1 = Order.builder().orderId(1).orderName("name1").build();
        Order order2 = Order.builder().orderId(2).orderName("name2").build();

        OrderDto orderDto = new OrderDtoBuilder().apply(order1);
        System.out.println(orderDto.getOrderId());
        System.out.println(orderDto.getOrderName());

        List<OrderDto> orderDtos = Arrays.asList(order1, order2).stream().map(new OrderDtoBuilder()).collect(Collectors.toList());
        OrderDto orderDto1 = orderDtos.get(0);
        OrderDto orderDto2 = orderDtos.get(1);

        System.out.println(orderDto1.getOrderId());
        System.out.println(orderDto1.getOrderName());

        System.out.println(orderDto2.getOrderId());
        System.out.println(orderDto2.getOrderName());
    }
}
