package com.harsh.AppointDoctor.Controllers;

import com.harsh.AppointDoctor.DTOs.ApiResponse;
import com.harsh.AppointDoctor.DTOs.SlotLockRequest;
import com.harsh.AppointDoctor.Enums.SlotStatus;
import com.harsh.AppointDoctor.Models.AppointmentSlot;
import com.harsh.AppointDoctor.Repo.AppointmentSlotRepo;
import com.harsh.AppointDoctor.Services.SlotLockService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/slots")
@RequiredArgsConstructor
public class SlotController {

    private final AppointmentSlotRepo slotRepo;
    private final SlotLockService lockService;

    @PostMapping("/lock")
    public ResponseEntity<ApiResponse<?>> lockSlot(
            @RequestBody SlotLockRequest req,
            Authentication authentication
    ) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("User not authenticated",401));
        }

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String userEmail = userDetails.getUsername();

        AppointmentSlot slot = slotRepo.findById(req.getSlotId())
                .orElseThrow(() -> new RuntimeException("Slot not found"));

        if (slot.getStatus() == SlotStatus.BOOKED) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(ApiResponse.error("Slot already booked",409));
        }

        boolean locked = lockService.lockSlot(
                slot.getDoctor().getDoctorId(),
                slot.getSlotDate(),
                slot.getSlotTime(),
                userEmail
        );

        if (!locked) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(ApiResponse.error("Slot is locked by another user", 409));
        }

        return ResponseEntity.ok(ApiResponse.success(null,"Slot locked successfully", 200));
    }

    @PostMapping("/unlock")
    public ResponseEntity<ApiResponse<?>> unlockSlot(
            @RequestBody SlotLockRequest req,
            Authentication authentication
    ) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("User not authenticated",401));
        }

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String userEmail = userDetails.getUsername();

        AppointmentSlot slot = slotRepo.findById(req.getSlotId())
                .orElseThrow(() -> new RuntimeException("Slot not found"));


        lockService.releaseLock(
                slot,
                userEmail
        );


        return ResponseEntity.ok(ApiResponse.success(null,"Slot unlocked successfully", 200));
    }
}

