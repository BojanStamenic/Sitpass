package com.svtKvt.sitpass.controller;

import com.svtKvt.sitpass.model.Discipline;
import com.svtKvt.sitpass.model.Facility;
import com.svtKvt.sitpass.model.WorkDay;
import com.svtKvt.sitpass.repository.DisciplineRepository;
import com.svtKvt.sitpass.repository.WorkDayRepository;
import com.svtKvt.sitpass.service.FacilityService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/facilities")
@RequiredArgsConstructor
public class FacilityController {

    @Autowired
    private FacilityService facilityService;

    private final DisciplineRepository disciplineRepository;

    private final WorkDayRepository workDayRepository;
    @GetMapping
    public List<Facility> getAllFacilities() {
        return facilityService.getAllFacilities();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Facility> getFacilityById(@PathVariable Long id) {
        Optional<Facility> facility = facilityService.getFacilityById(id);
        return facility.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public Facility createFacility(@RequestBody Facility facility) {
        // Set facility reference for each discipline
        for (Discipline discipline : facility.getDisciplines()) {
            discipline.setFacility(facility);
        }

        // Set facility reference for each work day
        for (WorkDay workDay : facility.getWorkDays()) {
            workDay.setFacility(facility);
        }

        // Save facility along with disciplines and work days due to CascadeType.ALL
        return facilityService.saveFacility(facility);
    }


    @PutMapping("/{id}")
    public ResponseEntity<Facility> updateFacility(@PathVariable Long id, @RequestBody Facility facilityDetails) {
        Optional<Facility> facilityOptional = facilityService.getFacilityById(id);

        if (facilityOptional.isPresent()) {
            Facility existingFacility = facilityOptional.get();

            // Ažuriranje osnovnih polja objekta Facility
            existingFacility.setName(facilityDetails.getName());
            existingFacility.setDescription(facilityDetails.getDescription());
            existingFacility.setAddress(facilityDetails.getAddress());
            existingFacility.setCity(facilityDetails.getCity());
            existingFacility.setTotalRating(facilityDetails.getTotalRating());
            existingFacility.setActive(facilityDetails.isActive());

            // Ažuriranje Disciplines
            existingFacility.getDisciplines().clear();
            for (Discipline discipline : facilityDetails.getDisciplines()) {
                discipline.setFacility(existingFacility); // Povezivanje discipline sa Facility
                existingFacility.getDisciplines().add(discipline); // Dodavanje discipline
            }

            // Ažuriranje WorkDays
            existingFacility.getWorkDays().clear();
            for (WorkDay workDay : facilityDetails.getWorkDays()) {
                workDay.setFacility(existingFacility); // Povezivanje workDay sa Facility
                existingFacility.getWorkDays().add(workDay); // Dodavanje workDay
            }

            // Snimanje objekta Facility zajedno sa Disciplines i WorkDays zahvaljujući CascadeType.ALL
            Facility updatedFacility = facilityService.saveFacility(existingFacility);

            return ResponseEntity.ok(updatedFacility);
        } else {
            return ResponseEntity.notFound().build();
        }
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFacility(@PathVariable Long id) {
        facilityService.deleteFacility(id);
        return ResponseEntity.noContent().build();
    }
}
