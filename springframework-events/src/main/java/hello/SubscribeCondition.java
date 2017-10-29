package hello;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class SubscribeCondition {

    private List<String> eventNames = new ArrayList<>();

    @EventListener(condition = "#event.name != null")
    public void subscribe(Event event) {
        eventNames.add(event.getName());
    }

    public List<String> getEventNames() {
        return eventNames;
    }
}
