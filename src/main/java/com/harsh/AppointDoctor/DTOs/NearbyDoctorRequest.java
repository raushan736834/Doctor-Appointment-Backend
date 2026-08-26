package com.harsh.AppointDoctor.DTOs;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NearbyDoctorRequest {

    private Double latitude;
    private Double longitude;
    private String specialist;

}
