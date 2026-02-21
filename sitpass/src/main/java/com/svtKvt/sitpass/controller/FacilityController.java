package com.svtKvt.sitpass.controller;

import com.svtKvt.sitpass.model.Discipline;
import com.svtKvt.sitpass.model.Facility;
import com.svtKvt.sitpass.model.Image;
import com.svtKvt.sitpass.model.WorkDay;
import com.svtKvt.sitpass.service.FacilityService;
import com.svtKvt.sitpass.service.ObjectStorageService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/facilities")
public class FacilityController {

    private final FacilityService facilityService;

    public FacilityController(FacilityService facilityService) {
        this.facilityService = facilityService;
    }

    @GetMapping
    public List<Facility> getAllFacilities() {
        return facilityService.getAllFacilities();
    }

    @GetMapping("/search")
    public List<Facility> searchFacilities(@RequestParam("q") String query) {
        return facilityService.searchFacilities(query);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Facility> getFacilityById(@PathVariable Long id) {
        Optional<Facility> facility = facilityService.getFacilityById(id);
        return facility.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public Facility createFacility(@RequestBody Facility facility) {
        for (Discipline discipline : facility.getDisciplines()) {
            discipline.setFacility(facility);
        }

        for (WorkDay workDay : facility.getWorkDays()) {
            workDay.setFacility(facility);
        }

        return facilityService.saveFacility(facility);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Facility> updateFacility(@PathVariable Long id, @RequestBody Facility facilityDetails) {
        Optional<Facility> facilityOptional = facilityService.getFacilityById(id);

        if (facilityOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Facility existingFacility = facilityOptional.get();

        existingFacility.setName(facilityDetails.getName());
        existingFacility.setDescription(facilityDetails.getDescription());
        existingFacility.setAddress(facilityDetails.getAddress());
        existingFacility.setCity(facilityDetails.getCity());
        existingFacility.setTotalRating(facilityDetails.getTotalRating());
        existingFacility.setActive(facilityDetails.isActive());

        existingFacility.getDisciplines().clear();
        for (Discipline discipline : facilityDetails.getDisciplines()) {
            discipline.setFacility(existingFacility);
            existingFacility.getDisciplines().add(discipline);
        }

        existingFacility.getWorkDays().clear();
        for (WorkDay workDay : facilityDetails.getWorkDays()) {
            workDay.setFacility(existingFacility);
            existingFacility.getWorkDays().add(workDay);
        }

        Facility updatedFacility = facilityService.saveFacility(existingFacility);
        return ResponseEntity.ok(updatedFacility);
    }

    @PostMapping(value = "/{id}/pdf", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Facility> uploadFacilityPdf(
            @PathVariable Long id,
            @RequestPart("file") MultipartFile file
    ) {
        return ResponseEntity.ok(facilityService.uploadPdf(id, file));
    }

    @GetMapping("/{id}/pdf")
    public ResponseEntity<byte[]> downloadFacilityPdf(@PathVariable Long id) {
        ObjectStorageService.StoredObject pdf = facilityService.downloadPdf(id);
        try (var input = pdf.stream()) {
            byte[] content = input.readAllBytes();
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"facility-" + id + ".pdf\"")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(content);
        } catch (Exception e) {
            throw new RuntimeException("Failed to read PDF stream.", e);
        }
    }

    @PostMapping(value = "/{id}/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Image> uploadFacilityImage(
            @PathVariable Long id,
            @RequestPart("file") MultipartFile file
    ) {
        return ResponseEntity.ok(facilityService.uploadImage(id, file));
    }

    @GetMapping("/images/{imageId}")
    public ResponseEntity<byte[]> downloadFacilityImage(@PathVariable Long imageId) {
        ObjectStorageService.StoredObject image = facilityService.downloadImage(imageId);
        try (var input = image.stream()) {
            byte[] content = input.readAllBytes();
            MediaType mediaType = MediaType.APPLICATION_OCTET_STREAM;
            if (image.contentType() != null && !image.contentType().isBlank()) {
                mediaType = MediaType.parseMediaType(image.contentType());
            }
            return ResponseEntity.ok()
                    .contentType(mediaType)
                    .body(content);
        } catch (Exception e) {
            throw new RuntimeException("Failed to read image stream.", e);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFacility(@PathVariable Long id) {
        facilityService.deleteFacility(id);
        return ResponseEntity.noContent().build();
    }
}

