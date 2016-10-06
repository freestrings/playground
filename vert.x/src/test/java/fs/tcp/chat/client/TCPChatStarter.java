package fs.tcp.chat.client;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;

public class TCPChatStarter extends AbstractVerticle {

    @Override
    public void start() throws Exception {
        vertx.deployVerticle("TCPChatClientReader.java");

        DeploymentOptions deploymentOptions = new DeploymentOptions();
        deploymentOptions.setWorker(true);
        vertx.deployVerticle("TCPChatClientWriter.java", deploymentOptions);
    }
}
