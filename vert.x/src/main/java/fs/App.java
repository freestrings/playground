package fs;

import fs.redis.Redis;
import fs.web.Http;
import io.vertx.core.*;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.service.ServiceVerticleFactory;

public class App extends AbstractVerticle {

    private Logger logger = LoggerFactory.getLogger(App.class);

    @Override
    public void start() throws Exception {
        logger.info("!!!Start App!!!");
        VertxOptions vertxOptions = new VertxOptions();
        vertxOptions.setEventLoopPoolSize(2);
        this.vertx = Vertx.vertx(vertxOptions);
        this.vertx.registerVerticleFactory(new ServiceVerticleFactory());
        deployVerticle(Redis.class);
        deployVerticle(Http.class);
    }

    private void deployVerticle(Class<?> servicClass) {
        DeploymentOptions externalOptions = getDeploymentOptions();
        logger.info("External Options: " + externalOptions.toJson());
        vertx.deployVerticle(getServiceName(servicClass), externalOptions, getCompletionHandler(servicClass));
    }

    private Handler<AsyncResult<String>> getCompletionHandler(final Class<?> serviceClass) {
        return result -> {
            if (result.failed()) {
                logger.error("Deploy Fail: " + getServiceName(serviceClass), result.cause());
            } else {
                logger.info("Deploy Success: " + getServiceName(serviceClass));
            }
        };
    }

    private DeploymentOptions getDeploymentOptions() {
        DeploymentOptions externalOptions = new DeploymentOptions();
        JsonObject options = config().getJsonObject("options");
        externalOptions.fromJson(options == null ? new JsonObject() : options);
        return externalOptions;
    }

    private String getServiceName(Class<?> c) {
        String serviceName = String.format(
                "service:conf/%s/%s",
                System.getProperty("activeProfile"),
                c.getCanonicalName().toLowerCase()
        );
        logger.info("Service Name: {}", serviceName);
        return serviceName;
    }

}
