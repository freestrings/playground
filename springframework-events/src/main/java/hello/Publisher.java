package hello;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
public class Publisher {

    @Autowired
    ApplicationEventPublisher eventPublisher;

    public void publish(Event event) {
        eventPublisher.publishEvent(event);
    }
}
