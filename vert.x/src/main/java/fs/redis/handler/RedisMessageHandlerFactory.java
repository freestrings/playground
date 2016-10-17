package fs.redis.handler;

import fs.redis.IRedisMessageHandler;
import io.vertx.redis.RedisClient;

public class RedisMessageHandlerFactory {

    public static IRedisMessageHandler create(String path, RedisClient client) {
        switch (path) {
            case "/v1/path1":
                return new Path1MessageHandlerV1(client);
            default:
                return null;
        }
    }
}
