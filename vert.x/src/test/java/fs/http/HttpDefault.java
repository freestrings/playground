package fs.http;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.Router;

public class HttpDefault extends AbstractVerticle {

    @Override
    public void start() throws Exception {
        Router router = Router.router(vertx);

        router.route("/vertx").handler(routingContext -> {

            HttpServerResponse response = routingContext.response();
            response.setChunked(true);
            String str = MD5Utils.md5(String.valueOf(System.currentTimeMillis()));
            response.headers().set("Content-Type", "text/html; charset=UTF-8");
            response.end(str);

        });

        Future<HttpServer> future = Future.future();
        vertx.createHttpServer()
                .requestHandler(router::accept)
                .listen(8080, "localhost"
                        , future.completer()
                );
    }
}
