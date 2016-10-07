package fs.redis;

import fs.Events;
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

        vertx.eventBus().consumer(REDIS_GET, (Handler<Message<String>>) message -> {
            String body = message.body();
            logger.info("Receive: {}", body);
            redis.get("test", result -> {
                if (result.failed()) {
                    message.fail(-1, result.cause().toString());
                } else {
                    message.reply(body);
                }
            });
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
