package fs.redis;

import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;

public interface IRedisMessageHandler {

    void doHandle(Message<JsonObject> message);
}
