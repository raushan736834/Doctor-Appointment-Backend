package com.harsh.AppointDoctor.Services;

import com.harsh.AppointDoctor.Models.Specialist;
import com.harsh.AppointDoctor.Repo.SpecialistRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class SpecialistService {

    private final SpecialistRepo repo;

    @Cacheable("all_specialist")
    public List<Specialist> getAllSpecialist() {
        return repo.findAllByOrderBySpecialistAsc();
    }
}