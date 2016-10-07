package fs;

import fs.web.HttpWithRedis;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.service.ServiceVerticleFactory;

public class App2 extends AbstractVerticle {

    private Logger logger = LoggerFactory.getLogger(App2.class);

    @Override
    public void start() throws Exception {
        logger.info("!!!Start App2!!!");
        vertx.registerVerticleFactory(new ServiceVerticleFactory());
        deployVerticle(HttpWithRedis.class);
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
