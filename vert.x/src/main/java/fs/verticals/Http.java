package fs.verticals;

import fs.util.MD5Utils;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

public class Http extends AbstractVerticle implements Events {

    private Logger logger = LoggerFactory.getLogger(Http.class);

    @Override
    public void start() throws Exception {
        logger.info("!!!Start Http!!!");
        EventBus eventBus = vertx.eventBus();
        vertx.createHttpServer()
                .requestHandler(request -> {
                    String sendMsg = MD5Utils.md5(String.valueOf(System.nanoTime()));
                    logger.info("Send: {}", sendMsg);

                    eventBus.send(EVENT_NAME_1, sendMsg, (Handler<AsyncResult<Message<String>>>) message -> {
                        HttpServerResponse response = request.response();
                        if (message.succeeded()) {
                            String receiveMsg = message.result().body();
                            String responseMsg = String.format("%s -> %b", sendMsg, sendMsg.equals(receiveMsg));
                            logger.info("Callback: {}", responseMsg);

                            response.putHeader("Content-Length", String.valueOf(responseMsg.length()));
                            response.write(responseMsg);
                        } else {
                            logger.error("Send Fail: {}", sendMsg);

                            response.setStatusCode(500);
                            response.setStatusMessage("Failed");
                        }
                        response.end();
                    });
                })
                .listen(8080, "localhost");
    }
}
