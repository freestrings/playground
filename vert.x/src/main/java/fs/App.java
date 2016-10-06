package fs;

import fs.verticals.Http;
import fs.verticals.Redis;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

public class App extends AbstractVerticle {

    private Logger logger = LoggerFactory.getLogger(App.class);

    @Override
    public void start() throws Exception {
        logger.info("!!!Start App!!!");
        Vertx vertx = Vertx.vertx();

        DeploymentOptions redisOptions = new DeploymentOptions();
        // round-robin
        redisOptions.setInstances(2);
        vertx.deployVerticle(Redis.class.getCanonicalName(), redisOptions);
        vertx.deployVerticle(Http.class.getCanonicalName());
    }

}
