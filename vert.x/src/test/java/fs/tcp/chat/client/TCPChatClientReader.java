package fs.tcp.chat.client;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.net.NetSocket;

public class TCPChatClientReader extends AbstractVerticle {

    private String writeHandleId;

    @Override
    public void start() throws Exception {
        vertx.createNetClient().connect(8081, "localhost", response -> {
            boolean succeeded = response.succeeded();
            System.out.println(succeeded);
            if (!succeeded) {
                return;
            }

            NetSocket socket = response.result();
            socket.handler(buffer -> System.out.println("받음 :" + buffer.toString()));
            socket.closeHandler(event -> System.out.println("닫힘 :" + socket.remoteAddress()));
            socket.exceptionHandler(throwable -> throwable.printStackTrace());

            writeHandleId = socket.writeHandlerID();
            System.out.println("Write Handle ID:" + writeHandleId);
        });

        vertx.eventBus().consumer("chat").handler(message -> {
            vertx.eventBus().send(writeHandleId, Buffer.buffer(message.body().toString()));
        });
    }

}
