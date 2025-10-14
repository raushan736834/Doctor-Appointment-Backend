package com.harsh.AppointDoctor.Models.DoctorOnboarding;

import com.harsh.AppointDoctor.Enums.Days;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Data;

@Data
@Embeddable
public class OperationHours {
    @Enumerated(EnumType.STRING)
    private Days days;
    private String open;
    private String close;
    private boolean isClosedToday;
}
