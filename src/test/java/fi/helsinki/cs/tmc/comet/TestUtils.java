package fi.helsinki.cs.tmc.comet;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import org.cometd.bayeux.Message;
import org.cometd.bayeux.client.ClientSession;
import org.cometd.bayeux.client.ClientSessionChannel;
import org.cometd.client.BayeuxClient;
import org.cometd.websocket.client.WebSocketTransport;
import org.eclipse.jetty.websocket.WebSocketClientFactory;
import static org.junit.Assert.*;

public class TestUtils {
    public static final String TEST_USER = "test_user";
    public static final String TEST_PASSWORD = "test_password";
    
    public static String getJettyPort() {
        return System.getProperty("jetty.port");
    }
    
    public static String getBackendKey() {
        return System.getProperty("fi.helsinki.cs.tmc.comet.backendKey.test");
    }

    public static String getCometUrl() {
        return "http://localhost:" + getJettyPort() + "/";
    }
    
    public static String getAdminMsgChannel() {
        return "/broadcast/tmc/global/admin-msg";
    }
    
    public static String getAuthServerBaseUrl() {
        return "http://localhost:" + getAuthServerPort() + "/foo";
    }
    
    public static int getAuthServerPort() {
        return 8081;
    }
    

    public static BayeuxClient connectAsFrontend() {
        BayeuxClient client = createClient();
        client.addExtension(getAuthenticationExtension(getFrontendAuthFields()));
        doHandshake(client);
        
        return client;
    }
    
    public static BayeuxClient connectAsBackend() {
        BayeuxClient client = createClient();
        client.addExtension(getAuthenticationExtension(getBackendAuthFields()));
        doHandshake(client);
        
        return client;
    }
    
    private static BayeuxClient createClient() {
        Map<String, Object> transportOpts = new HashMap<String, Object>();
        WebSocketTransport transport = WebSocketTransport.create(transportOpts, new WebSocketClientFactory());
        
        return new BayeuxClient(getCometUrl(), transport);
    }

    private static void doHandshake(BayeuxClient client) {
        client.handshake();
        if (!client.waitFor(3000, BayeuxClient.State.CONNECTED)) {
            fail("Handshake failed.");
        }
    }
    
    private static ClientSession.Extension getAuthenticationExtension(final Map<String, Object> fields) {
        return new ClientSession.Extension() {
            public boolean rcv(ClientSession session, Message.Mutable message) {
                return true;
            }

            public boolean rcvMeta(ClientSession session, Message.Mutable message) {
                return true;
            }

            public boolean send(ClientSession session, Message.Mutable message) {
                return true;
            }

            public boolean sendMeta(ClientSession session, Message.Mutable message) {
                message.getExt(true).put("authentication", fields);
                return true;
            }
        };
    }
    
    private static Map<String, Object> getFrontendAuthFields() {
        HashMap<String, Object> result = new HashMap<String, Object>();
        result.put("username", TEST_USER);
        result.put("password", TEST_PASSWORD);
        result.put("serverBaseUrl", getAuthServerBaseUrl());
        return result;
    }
    
    private static Map<String, Object> getBackendAuthFields() {
        HashMap<String, Object> result = new HashMap<String, Object>();
        result.put("backendKey", getBackendKey());
        result.put("serverBaseUrl", getAuthServerBaseUrl());
        return result;
    }
    
    public static void addEnqueueingListener(final BlockingQueue<Message> queue, ClientSessionChannel channel) {
        channel.addListener(new ClientSessionChannel.MessageListener() {
            @Override
            public void onMessage(ClientSessionChannel channel, Message message) {
                queue.add(message);
            }
        });
    }
    
    public static void addEnqueueingSubscription(final BlockingQueue<Message> queue, ClientSessionChannel channel) {
        channel.subscribe(new ClientSessionChannel.MessageListener() {
            @Override
            public void onMessage(ClientSessionChannel channel, Message message) {
                queue.add(message);
            }
        });
    }
}