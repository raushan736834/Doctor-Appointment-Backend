package com.harsh.AppointDoctor.Repo;

import com.harsh.AppointDoctor.Enums.AppointmentStatus;
import com.harsh.AppointDoctor.Models.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByUserEmailOrderByCreatedAtDesc(String userEmail);

    @Query("SELECT COUNT(n) FROM Notification n WHERE n.userEmail = :userEmail AND n.isRead = false")
    long countUnreadByUserEmail(@Param("userEmail") String userEmail);

    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.userEmail = :userEmail")
    void markAllAsReadByUserEmail(@Param("userEmail") String userEmail);

    List<Notification> findByUserEmailAndTypeOrderByCreatedAtDesc(String userEmail, AppointmentStatus type);

    @Query("SELECT n FROM Notification n WHERE n.userEmail = :userEmail AND n.createdAt >= :startDate")
    List<Notification> findRecentNotifications(@Param("userEmail") String userEmail, @Param("startDate") LocalDateTime startDate);
}
