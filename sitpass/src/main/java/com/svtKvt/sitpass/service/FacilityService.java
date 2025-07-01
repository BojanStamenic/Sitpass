package com.svtKvt.sitpass.service;

import com.svtKvt.sitpass.model.Facility;
import com.svtKvt.sitpass.repository.FacilityRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class FacilityService {

    @Autowired
    private FacilityRepository facilityRepository;

    public List<Facility> getAllFacilities() {
        return facilityRepository.findAll();
    }

    public Facility findFacilityById(Long id) {
        return facilityRepository.findById(id).orElse(null);
    }
    public Optional<Facility> getFacilityById(Long id) {
        return facilityRepository.findById(id);
    }

    public Facility saveFacility(Facility facility) {
        return facilityRepository.save(facility);
    }

    public void deleteFacility(Long id) {
        facilityRepository.deleteById(id);
    }
}
