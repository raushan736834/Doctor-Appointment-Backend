package com.harsh.AppointDoctor.Services;

import com.harsh.AppointDoctor.Models.AppointmentSlot;
import com.harsh.AppointDoctor.Repo.AppointmentSlotRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SlotQueryService {

    private final AppointmentSlotRepo slotRepo;

    public List<AppointmentSlot> getAvailableSlots(
            String doctorId,
            LocalDate date
    ) {
        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();

        if (date.isBefore(today)) {
            return List.of();
        }

        return slotRepo.findAvailableSlots(
                doctorId,
                date,
                today,
                now
        );
    }
}

