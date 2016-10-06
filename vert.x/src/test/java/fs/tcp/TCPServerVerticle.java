package fs.tcp;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.net.NetServer;

/**
 * client -> telnet localhost 8081
 */
public class TCPServerVerticle extends AbstractVerticle {

    @Override
    public void start() throws Exception {

        NetServer netServer = vertx.createNetServer();
        netServer.connectHandler(socket -> {
            socket.handler(buffer -> socket.write(buffer.toString().toUpperCase()));
            socket.closeHandler(event -> System.out.println("클라가 끊음: " + socket.remoteAddress()));
            socket.exceptionHandler(throwable -> throwable.printStackTrace());
            socket.write("왔다\n");
        });

        netServer.listen(8081, "localhost");
    }
}
