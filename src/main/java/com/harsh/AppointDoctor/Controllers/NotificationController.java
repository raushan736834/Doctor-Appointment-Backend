package com.harsh.AppointDoctor.Controllers;

import com.harsh.AppointDoctor.DTOs.ApiResponse;
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
    public ResponseEntity<ApiResponse<List<Notification>>> getUserNotifications(@PathVariable String userEmail) {
        List<Notification> notifications = notificationService.getUserNotifications(userEmail);
        return ResponseEntity.ok(ApiResponse.success(notifications,"",200));
    }

    @GetMapping("/user/{userEmail}/unread-count")
    public ResponseEntity<ApiResponse<Long>> getUnreadCount(@PathVariable String userEmail) {
        long count = notificationService.getUnreadCount(userEmail);
        return ResponseEntity.ok(ApiResponse.success(count,"",200));
    }

    @PutMapping("/{notificationId}/read")
    public ResponseEntity<?> markAsRead(@PathVariable Long notificationId) {
        notificationService.markAsRead(notificationId);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/user/{userEmail}/mark-all-read")
    public ResponseEntity<?> markAllAsRead(@PathVariable String userEmail) {
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
