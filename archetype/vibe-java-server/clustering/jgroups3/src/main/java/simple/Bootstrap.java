package simple;

import java.util.Map;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import org.atmosphere.vibe.platform.Action;
import org.atmosphere.vibe.platform.server.atmosphere2.AtmosphereBridge;
import org.atmosphere.vibe.server.ClusteredServer;
import org.atmosphere.vibe.server.ServerSocket;
import org.jgroups.JChannel;
import org.jgroups.Message;
import org.jgroups.ReceiverAdapter;

@WebListener
public class Bootstrap implements ServletContextListener {
    @SuppressWarnings("resource")
    @Override
    public void contextInitialized(ServletContextEvent event) {
        final ClusteredServer server = new ClusteredServer();
        final JChannel channel;
        try {
            channel = new JChannel();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        // Receives a message
        channel.setReceiver(new ReceiverAdapter() {
            @SuppressWarnings("unchecked")
            @Override
            public void receive(Message message) {
                System.out.println("receiving a message: " + message.getObject());
                server.messageAction().on((Map<String, Object>) message.getObject());
            }
        });
        // Publishes a message
        server.publishAction(new Action<Map<String, Object>>() {
            @Override
            public void on(Map<String, Object> message) {
                System.out.println("publishing a message: " + message);
                try {
                    channel.send(new Message(null, message));
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        });
        try {
            channel.connect("vibe");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        
        server.socketAction(new Action<ServerSocket>() {
            @Override
            public void on(final ServerSocket socket) {
                System.out.println("on socket: " + socket.uri());
                socket.on("echo", new Action<Object>() {
                    @Override
                    public void on(Object data) {
                        System.out.println("on echo event: " + data);
                        socket.send("echo", data);
                    }
                });
                socket.on("chat", new Action<Object>() {
                    @Override
                    public void on(Object data) {
                        System.out.println("on chat event: " + data);
                        server.all().send("chat", data);
                    }
                });
            }
        });
        new AtmosphereBridge(event.getServletContext(), "/vibe").httpAction(server.httpAction()).websocketAction(server.websocketAction());
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {}
}
