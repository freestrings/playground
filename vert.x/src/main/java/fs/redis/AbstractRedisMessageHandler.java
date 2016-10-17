package fs.redis;

import io.vertx.redis.RedisClient;

public abstract class AbstractRedisMessageHandler implements IRedisMessageHandler {

    private RedisClient client;

    public AbstractRedisMessageHandler(RedisClient client) {
        this.client = client;
    }

    public RedisClient getClient() {
        return client;
    }
}
