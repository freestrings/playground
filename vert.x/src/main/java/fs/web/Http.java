package fs.web;

import fs.Events;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;

import java.util.HashMap;
import java.util.Map;

public class Http extends AbstractVerticle implements Events {

    private Logger logger = LoggerFactory.getLogger(Http.class);

    @Override
    public void start() throws Exception {
        logger.info("!!!Start Http!!!");
        EventBus eventBus = vertx.eventBus();
        Router router = Router.router(vertx);

        router.route("/v1/*").handler(routingContext ->
                eventBus.send(Events.REDIS_GET,
                        defaultMessage(routingContext.request()),
                        handler(routingContext)));

        Future<HttpServer> future = Future.future();
        vertx.createHttpServer()
                .requestHandler(router::accept)
                .listen(getPort(), getHost(), future.completer());
    }

    private JsonObject defaultMessage(HttpServerRequest request) {

        if (logger.isTraceEnabled()) {
            logger.trace("path: {}", request.path());
            logger.trace("//params---");
            request.params().forEach(entry ->
                    logger.trace("-param: {}={}", entry.getKey(), entry.getValue()));
            logger.trace("params---//");
        }

        JsonObject jsonObject = new JsonObject();
        jsonObject.put("path", request.path());
        if (request.params().size() > 0) {
            Map<String, String> params = new HashMap<>();
            request.params().forEach(entry -> params.put(entry.getKey(), entry.getValue()));
            jsonObject.put("params", params);
        }
        jsonObject.put("_timestamp", System.currentTimeMillis());
        return jsonObject;
    }

    private Handler<AsyncResult<Message<String>>> handler(RoutingContext routingContext) {
        return message -> {
            if (message.succeeded()) {
                String body = message.result().body();

                if (logger.isTraceEnabled()) {
                    logger.trace("Callback: {}", body);
                }

                routingContext.response().putHeader("Content-Length", String.valueOf(body.length()));
                routingContext.response().write(body);
            } else {
                logger.error("Send Fail: {}", message.cause());
                routingContext.response().setStatusCode(500);
                routingContext.response().setStatusMessage("Failed");
            }
            routingContext.response().end();
        };
    }

    private int getPort() {
        JsonObject http = config().getJsonObject("http");
        return http.getInteger("port");
    }

    private String getHost() {
        JsonObject http = config().getJsonObject("http");
        return http.getString("host");
    }
}
