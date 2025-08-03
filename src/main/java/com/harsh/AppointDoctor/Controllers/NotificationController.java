package com.harsh.AppointDoctor.Controllers;

import com.harsh.AppointDoctor.Models.Notification;
import com.harsh.AppointDoctor.Services.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping("/user/{userEmail}")
    public ResponseEntity<List<Notification>> getUserNotifications(@PathVariable String userEmail) {
        List<Notification> notifications = notificationService.getUserNotifications(userEmail);
        return ResponseEntity.ok(notifications);
    }

    @GetMapping("/user/{userEmail}/unread-count")
    public ResponseEntity<Long> getUnreadCount(@PathVariable String userEmail) {
        long count = notificationService.getUnreadCount(userEmail);
        return ResponseEntity.ok(count);
    }

    @PutMapping("/{notificationId}/read")
    public ResponseEntity<Void> markAsRead(@PathVariable Long notificationId) {
        notificationService.markAsRead(notificationId);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/user/{userEmail}/mark-all-read")
    public ResponseEntity<Void> markAllAsRead(@PathVariable String userEmail) {
        notificationService.markAllAsRead(userEmail);
        return ResponseEntity.ok().build();
    }

//    @DeleteMapping("/{notificationId}")
//    public ResponseEntity<Void> deleteNotification(@PathVariable Long notificationId) {
//        notificationService.deleteNotification(notificationId);
//        return ResponseEntity.ok().build();
//    }

//    @GetMapping("/user/{userEmail}/by-type/{type}")
//    public ResponseEntity<List<Notification>> getNotificationsByType(
//            @PathVariable String userEmail,
//            @PathVariable String type) {
//        List<Notification> notifications = notificationService
//                .getNotificationByType(userEmail, type);
//        return ResponseEntity.ok(notifications);
//    }
}
