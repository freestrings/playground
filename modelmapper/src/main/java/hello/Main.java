package hello;

import org.modelmapper.ModelMapper;

public class Main {

    public static void main(String... args) {
        ModelMapper modelMapper = new ModelMapper();
        Order order = new Order();
        order.setOrderId(1);
        order.setOrderName("name");
        OrderDto orderDto = modelMapper.map(order, OrderDto.class);
        System.out.println(orderDto.getOrderId());
        System.out.println(orderDto.getOrderName());
    }
}
