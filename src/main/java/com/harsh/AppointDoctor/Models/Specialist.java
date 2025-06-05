package com.harsh.AppointDoctor.Models;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class Specialist {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Column(name = "image")
    private String image;
    @Column(name = "specialist")
    private String specialist;
}
