package fs.web;

import fs.Events;
import fs.util.MD5Utils;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.redis.RedisClient;
import io.vertx.redis.RedisOptions;

/**
 * 이렇게 하면 안된다.
 */
public class HttpWithRedis extends AbstractVerticle implements Events {

    private Logger logger = LoggerFactory.getLogger(HttpWithRedis.class);

    @Override
    public void start() throws Exception {
        logger.info("!!!Start HttpWithRedis!!!");

        final RedisClient redis = RedisClient.create(vertx, createRedisOptions());

        vertx.createHttpServer()
                .requestHandler(request -> {
                    String sendMsg = MD5Utils.md5(String.valueOf(System.nanoTime()));
                    logger.info("Send: {}", sendMsg);
                    HttpServerResponse response = request.response();
                    redis.get("test", result -> {
                        if (result.failed()) {
                            logger.error("Send Fail", sendMsg);
                            response.setStatusCode(500);
                            response.setStatusMessage("Failed");
                        } else {
                            logger.info("Callback: {}", sendMsg);
                            response.putHeader("Content-Length", String.valueOf(sendMsg.length()));
                            response.write(sendMsg);
                        }
                    });
                })
                .listen(getPort(), getHost());


    }

    private int getPort() {
        JsonObject http = config().getJsonObject("http");
        System.out.println(http);
        return http.getInteger("port");
    }

    private String getHost() {
        JsonObject http = config().getJsonObject("http");
        return http.getString("host");
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
