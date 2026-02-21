package com.svtKvt.sitpass.service;

import com.svtKvt.sitpass.model.Facility;
import com.svtKvt.sitpass.repository.FacilityRepository;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class FacilitySearchBootstrapService {

    private final FacilityRepository facilityRepository;
    private final FacilitySearchIndexService facilitySearchIndexService;

    public FacilitySearchBootstrapService(
            FacilityRepository facilityRepository,
            FacilitySearchIndexService facilitySearchIndexService
    ) {
        this.facilityRepository = facilityRepository;
        this.facilitySearchIndexService = facilitySearchIndexService;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void bootstrapIndex() {
        List<Facility> facilities = facilityRepository.findAll();
        facilitySearchIndexService.reindexFacilities(facilities);
    }
}
