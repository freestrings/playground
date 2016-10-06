package fs.tcp.chat.server;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.AsyncResultHandler;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.net.NetServer;
import io.vertx.core.net.NetSocket;
import io.vertx.core.shareddata.LocalMap;

import java.util.stream.Collectors;

/**
 * vertx run TCPChatServer.java --instance 3
 */
public class TCPChatServer extends AbstractVerticle {

    private LocalMap<String, Object> clients;

    @Override
    public void start() throws Exception {
        NetServer netServer = vertx.createNetServer();
        // instance 옵션을 주면 instance간 clients 맵이 공유되지 않기에 sharedData를 사용
        clients = vertx.sharedData().getLocalMap("clients");
        netServer.connectHandler(socket -> {
            System.out.println("왔다~" + socket.remoteAddress());
            socket.exceptionHandler(throwable -> throwable.printStackTrace());
            socket.closeHandler(closeHandler(socket));
            socket.handler(socketHandler(socket));
        });
        netServer.listen(8081, "localhost", (AsyncResultHandler<NetServer>) event -> {
            System.out.println("Run on 8081: " + event.succeeded());
        });
    }

    private Handler<Void> closeHandler(NetSocket socket) {
        return event -> {
            System.out.println("클라가 끊음: " + socket.remoteAddress());
            clients.remove(socket.writeHandlerID());
        };
    }

    private Handler<Buffer> socketHandler(NetSocket socket) {
        clients.put(socket.writeHandlerID(), socket.writeHandlerID());
        return buffer -> clients.keySet().stream()
                .filter(client -> !client.equals(socket.writeHandlerID()))
                .collect(Collectors.toList())
                .forEach(client -> vertx.eventBus().send(client, buffer));
    }
}
