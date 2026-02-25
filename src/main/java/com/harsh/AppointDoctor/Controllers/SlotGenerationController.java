package com.harsh.AppointDoctor.Controllers;

import com.harsh.AppointDoctor.DTOs.ApiResponse;
import com.harsh.AppointDoctor.Services.SlotGenerationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/slots")
@RequiredArgsConstructor
public class SlotGenerationController {

    private final SlotGenerationService slotGenerationService;

    // Generate slots for next 14 days
    @PostMapping("/generate/{doctorId}")
    public ResponseEntity<ApiResponse<?>> generateSlots(
            @PathVariable String doctorId,
            @RequestParam(defaultValue = "14") int days
    ) {
        slotGenerationService.generateSlotsForDoctor(doctorId, days);
        return ResponseEntity.ok(ApiResponse.success(null,"Slots generated successfully",200));
    }


    @GetMapping("/generate")
    public ResponseEntity<ApiResponse<?>> generateSlotsForAllDoctors() {

        slotGenerationService.generateSlotsForAllDoctors();

        return ResponseEntity.ok(
                ApiResponse.success(null,"Slots generated successfully for next 14 days",200)
        );
    }

}

