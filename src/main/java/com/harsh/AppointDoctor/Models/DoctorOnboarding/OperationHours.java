package com.harsh.AppointDoctor.Models.DoctorOnboarding;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.harsh.AppointDoctor.Enums.Days;
import jakarta.persistence.*;
import lombok.Data;

@Data
@Embeddable
public class OperationHours {
    @Enumerated(EnumType.STRING)
    private Days days;           // e.g., MONDAY, TUESDAY, etc.

    @Column
    private String open;        // opening time (e.g., "09:00 AM")
    @Column
    private String close;        // closing time (e.g., "05:00 PM")

    @JsonProperty("isClosedToday")
    private Boolean isClosedToday;  // true if clinic is closed that day

}

