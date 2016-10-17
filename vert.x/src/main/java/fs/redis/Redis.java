package fs.redis;

import fs.Events;
import fs.redis.handler.RedisMessageHandlerFactory;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.redis.RedisClient;
import io.vertx.redis.RedisOptions;

public class Redis extends AbstractVerticle implements Events {

    private Logger logger = LoggerFactory.getLogger(Redis.class);

    @Override
    public void start() throws Exception {
        logger.info("!!!Start Redis!!!");

        RedisClient redis = RedisClient.create(vertx, createRedisOptions());

        vertx.eventBus().consumer(REDIS_GET, (Handler<Message<JsonObject>>) message -> {
            JsonObject body = message.body();

            if (logger.isTraceEnabled()) {
                logger.trace("Receive: {}", body.toString());
            }

            IRedisMessageHandler handler = RedisMessageHandlerFactory.create(body.getString("path"), redis);
            if (handler == null) {
                logger.error("No Handler for '{}'", body);
                message.fail(-1, String.format("No Handler for '%s'", body));
            } else {
                handler.doHandle(message);
            }

        });
    }

    private RedisOptions createRedisOptions() {
        logger.info("Redis Config " + config());
        JsonObject redis = config().getJsonObject("redis");
        RedisOptions options = new RedisOptions();
        options.setHost(redis.getString("host"));
        options.setPort(redis.getInteger("port"));
        return options;
    }
}
