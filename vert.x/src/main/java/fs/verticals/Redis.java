package fs.verticals;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.Message;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

public class Redis extends AbstractVerticle implements Events {

    private Logger logger = LoggerFactory.getLogger(Redis.class);

    @Override
    public void start() throws Exception {
        logger.info("!!!Start Redis!!!");
        vertx.eventBus().localConsumer(EVENT_NAME_1, (Handler<Message<String>>) message -> {
            if (false) {
                message.fail(-1, "No Reason");
            } else {
                String body = message.body();
                logger.info("Receive: {}", body);
                message.reply(body);
            }
        });
    }
}
