package com.harsh.AppointDoctor.Services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

/**
 * Service for sending real-time notifications via STOMP WebSocket protocol.
 * Replaces the previous Socket.IO implementation.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class StompNotificationService {

    private final SimpMessagingTemplate messagingTemplate;

    /**
     * Sends a notification to a specific user via STOMP.
     * The message is sent to /user/{email}/queue/notifications destination.
     *
     * @param userEmail    The email address of the user (used as the identifier)
     * @param notification The notification object to send
     */
    public void sendNotificationToUser(String userEmail, Object notification) {
        try {
            // Send to user-specific queue: /user/{email}/queue/notifications
            messagingTemplate.convertAndSendToUser(
                    userEmail,
                    "/queue/notifications",
                    notification);
            log.info("STOMP notification sent to user: {}", userEmail);
        } catch (Exception e) {
            log.error("Error sending STOMP notification to user {}: {}", userEmail, e.getMessage(), e);
        }
    }

}
