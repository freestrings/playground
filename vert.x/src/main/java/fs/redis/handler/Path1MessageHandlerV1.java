package fs.redis.handler;

import fs.redis.AbstractRedisMessageHandler;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import io.vertx.redis.RedisClient;

import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class Path1MessageHandlerV1 extends AbstractRedisMessageHandler {

    public Path1MessageHandlerV1(RedisClient client) {
        super(client);
    }

    @Override
    public void doHandle(Message<JsonObject> message) {
        JsonObject jsonObject = message.body();
        Long timestamp = jsonObject.getLong("_timestamp");
        setInfo1(timestamp)
                .thenCompose((Function) o -> {
                    if (!o.equals(Boolean.TRUE)) {
                        throw new RuntimeException("fail setInfo1");
                    }
                    return setInfo2(timestamp);
                })
                .thenCompose((Function) o -> {
                    if (!o.equals(Boolean.TRUE)) {
                        throw new RuntimeException("fail setInfo2");
                    }
                    return getInfo1(timestamp, new JsonObject());
                })
                .thenCompose((Function) o -> getInfo2(timestamp, ((JsonObject) o)))
                .exceptionally(o -> {
                    message.fail(-1, "Fail :" + ((Throwable) o).getMessage());
                    return null;
                }).whenComplete((BiConsumer) (o, o2) -> {
            String info1 = ((JsonObject) o).getString("info1");
            message.reply(info1);
        });
    }

    private CompletableFuture setInfo1(Long timestamp) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        getClient().set(
                "info1:" + timestamp,
                String.valueOf(timestamp),
                event -> future.complete(event.succeeded()));
        return future;
    }

    private CompletableFuture setInfo2(Long timestamp) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        getClient().set(
                "info2:" + timestamp,
                String.valueOf(timestamp),
                event -> future.complete(event.succeeded()));
        return future;
    }

    private CompletableFuture getInfo1(Long timestamp, JsonObject jsonObject) {
        CompletableFuture<JsonObject> future = new CompletableFuture<>();
        getClient().get("info1:" + timestamp, event -> {
            jsonObject.put("info1", event.result());
            future.complete(jsonObject);
        });
        return future;
    }

    private CompletableFuture getInfo2(Long timestamp, JsonObject jsonObject) {
        CompletableFuture<JsonObject> future = new CompletableFuture<>();
        getClient().get("info2:" + timestamp, event -> {
            jsonObject.put("info2", event.result());
            future.complete(jsonObject);
        });
        return future;
    }
}
