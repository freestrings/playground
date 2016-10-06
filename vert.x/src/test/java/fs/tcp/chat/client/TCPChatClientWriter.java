package fs.tcp.chat.client;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.EventBus;

import java.io.Console;

public class TCPChatClientWriter extends AbstractVerticle {

    @Override
    public void start() throws Exception {
        EventBus eventBus = vertx.eventBus();
        Console console = System.console();
        while (true) {
            eventBus.send("chat", console.readLine());
            try {
                Thread.sleep(10);
            } catch (Exception e) {
            }
        }
    }
}
