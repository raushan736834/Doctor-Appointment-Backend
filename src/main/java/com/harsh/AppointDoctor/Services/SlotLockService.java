package com.harsh.AppointDoctor.Services;

import com.harsh.AppointDoctor.Models.AppointmentSlot;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;

@Service
@RequiredArgsConstructor
public class SlotLockService {

    private final StringRedisTemplate redisTemplate;
    private static final Duration LOCK_TTL = Duration.ofMinutes(5);

    public boolean lockSlot(
            String doctorId,
            LocalDate date,
            LocalTime time,
            String userEmail
    ) {
        String key = key(doctorId, date, time);
        Boolean success = redisTemplate.opsForValue()
                .setIfAbsent(key, userEmail, LOCK_TTL);
        return Boolean.TRUE.equals(success);
    }

    public boolean isLockedByUser(
            String doctorId,
            LocalDate date,
            LocalTime time,
            String userEmail
    ) {
        String val = redisTemplate.opsForValue()
                .get(key(doctorId, date, time));
        return userEmail.equals(val);
    }

    public void releaseLock(AppointmentSlot slot, String userEmail) {

        String redisKey = key(
                slot.getDoctor().getDoctorId(),
                slot.getSlotDate(),
                slot.getSlotTime()
        );

        String lockedBy = redisTemplate.opsForValue().get(redisKey);

        // 🔐 Only owner can unlock
        if (userEmail.equals(lockedBy)) {
            redisTemplate.delete(redisKey);
        }
    }


    private String key(String doctorId, LocalDate date, LocalTime time) {
        return "slot:lock:" + doctorId + ":" + date + ":" + time;
    }
}

