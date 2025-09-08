package com.harsh.AppointDoctor.Models;

import jakarta.persistence.*;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

//@Entity
//@Table(name = "cities", uniqueConstraints = {
//        @UniqueConstraint(columnNames = {"name", "state", "country"})
//})
//@Data
public class City {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String state;
    private String country;

    @OneToMany(mappedBy = "city", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DoctorProfile> doctors = new ArrayList<>();
}
