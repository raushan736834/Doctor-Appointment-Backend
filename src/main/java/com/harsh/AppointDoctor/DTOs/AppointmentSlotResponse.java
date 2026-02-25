package com.harsh.AppointDoctor.DTOs;

import com.harsh.AppointDoctor.Enums.SlotStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AppointmentSlotResponse {
    private String slotId;
    private LocalTime slotTime;
    private SlotStatus status;
}

