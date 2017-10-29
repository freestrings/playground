package hello;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class App {

    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(App.class, args);
        Publisher publisher = context.getBean(Publisher.class);
        publisher.publish(new Event(null));
        publisher.publish(new Event("Test"));

        Subscribe subscribe = context.getBean(Subscribe.class);
        SubscribeCondition subscribeCondition = context.getBean(SubscribeCondition.class);

        System.out.println(subscribe.getEventNames().size() == 2);
        System.out.println(subscribeCondition.getEventNames().size() == 1);
    }
}
