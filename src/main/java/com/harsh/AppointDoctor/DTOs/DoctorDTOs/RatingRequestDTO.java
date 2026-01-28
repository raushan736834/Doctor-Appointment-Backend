package com.harsh.AppointDoctor.DTOs.DoctorDTOs;

import lombok.Data;
import java.util.UUID;
import jakarta.validation.constraints.*;


@Data
public class RatingRequestDTO {

    @NotNull(message = "Appointment ID is required")
    private UUID appointmentId;
    @NotNull(message = "Rating is required")
    @Min(value = 1, message = "Rating must be at least 1")
    @Max(value = 5, message = "Rating must be at most 5")
    private int rating;

    @Size(max = 1000, message = "Review text must not exceed 1000 characters")
    private String review;
    private boolean anonymous;
}
