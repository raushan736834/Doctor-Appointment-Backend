package com.harsh.AppointDoctor.DTOs.DoctorDTOs;

import com.harsh.AppointDoctor.Enums.Days;
import lombok.Data;
@Data
public class OperatingHoursResponse {
    private Days days;
    private String open;
    private String close;
    private Boolean isClosedToday;

}
