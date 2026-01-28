package com.harsh.AppointDoctor.Controllers;

import com.harsh.AppointDoctor.DTOs.ApiResponse;
import com.harsh.AppointDoctor.DTOs.AppointmentSlotResponse;
import com.harsh.AppointDoctor.Models.AppointmentSlot;
import com.harsh.AppointDoctor.Repo.AppointmentSlotRepo;
import com.harsh.AppointDoctor.Services.SlotQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/slots")
@RequiredArgsConstructor
public class SlotQueryController {

    private final SlotQueryService slotQueryService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<AppointmentSlotResponse>>> getSlots(
            @RequestParam String doctorId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate date
    ) {
        List<AppointmentSlotResponse> list =
                slotQueryService.getAvailableSlots(doctorId, date)
                        .stream()
                        .map(slot -> new AppointmentSlotResponse(
                                slot.getSlotId(),
                                slot.getSlotTime(),
                                slot.getStatus()
                        ))
                        .toList();

        return ResponseEntity.ok(ApiResponse.success(list, "", 200));
    }
}

