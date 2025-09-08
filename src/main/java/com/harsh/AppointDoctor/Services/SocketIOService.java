package com.harsh.AppointDoctor.Services;

import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.listener.ConnectListener;
import com.corundumstudio.socketio.listener.DisconnectListener;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
public class SocketIOService {

    @Autowired
    private SocketIOServer server;

    // Store user sessions: email -> sessionId
    private final Map<String, String> userSessions = new ConcurrentHashMap<>();

    @PostConstruct
    public void startServer() {
        try {
            server.addConnectListener(onConnected());
            server.addDisconnectListener(onDisconnected());

            server.addEventListener("join", String.class, (client, userEmail, ackSender) -> {
                log.info("User {} joined with session {}", userEmail, client.getSessionId());
                userSessions.put(userEmail, client.getSessionId().toString());
                client.joinRoom(userEmail);
            });

            server.start();
            log.info("Socket.IO server started on port: {}", server.getConfiguration().getPort());
        } catch (Exception e) {
            log.error("Failed to start Socket.IO server: {}", e.getMessage(), e);
        }
    }


    @PreDestroy
    public void stopServer() {
        if (server != null) {
            server.stop();
            log.info("Socket.IO server stopped");
        }
    }

    private ConnectListener onConnected() {
        return client -> {
            log.info("Client connected: {}", client.getSessionId());
        };
    }

    private DisconnectListener onDisconnected() {
        return client -> {
            log.info("Client disconnected: {}", client.getSessionId());
            // Remove user from sessions map
            userSessions.entrySet().removeIf(entry ->
                    entry.getValue().equals(client.getSessionId().toString()));
        };
    }

    public void sendNotificationToUser(String userEmail, Object notification) {
        try {
            // Send to user's room
            server.getRoomOperations(userEmail).sendEvent("notification", notification);
            log.info("Notification sent to user: {}", userEmail);
        } catch (Exception e) {
            log.error("Error sending notification to user {}: {}", userEmail, e.getMessage());
        }
    }

    public boolean isUserOnline(String userEmail) {
        return userSessions.containsKey(userEmail);
    }
}