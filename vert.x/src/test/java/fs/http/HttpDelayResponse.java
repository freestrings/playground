package fs.http;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.Router;

public class HttpDelayResponse extends AbstractVerticle {

    @Override
    public void start() throws Exception {
        Router router = Router.router(vertx);

        router.route("/some/path/").handler(routingContext -> {
            HttpServerResponse response = routingContext.response();
            response.setChunked(true);
            response.write("route1\n");
            routingContext.vertx().setTimer(5000, tid -> routingContext.next());
        });

        router.route("/some/path/").handler(routingContext -> {
            HttpServerResponse response = routingContext.response();
            response.write("route2\n");
            routingContext.vertx().setTimer(5000, tid -> routingContext.next());
        });

        router.route("/some/path/").handler(routingContext -> {
            HttpServerResponse response = routingContext.response();
            response.write("route3");
            response.end();
        });

        vertx.createHttpServer()
                .requestHandler(router::accept)
                .listen(8080, "localhost");

    }
}
