package com.svtKvt.sitpass.controller;

import com.svtKvt.sitpass.dto.FacilitySearchResultDto;
import com.svtKvt.sitpass.dto.RatingCategory;
import com.svtKvt.sitpass.dto.SearchOperator;
import com.svtKvt.sitpass.model.Discipline;
import com.svtKvt.sitpass.model.Facility;
import com.svtKvt.sitpass.model.Image;
import com.svtKvt.sitpass.model.WorkDay;
import com.svtKvt.sitpass.service.FacilityService;
import com.svtKvt.sitpass.service.ObjectStorageService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

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
    public List<Facility> searchFacilities(
            @RequestParam(value = "q", required = false, defaultValue = "") String query,
            @RequestParam(value = "minReviews", required = false) Integer minReviews,
            @RequestParam(value = "maxReviews", required = false) Integer maxReviews
    ) {
        validateReviewRange(minReviews, maxReviews);
        return facilityService.searchFacilities(query, minReviews, maxReviews);
    }

    @GetMapping("/search/advanced")
    public List<FacilitySearchResultDto> advancedSearchFacilities(
            @RequestParam(value = "q", required = false) String generalQuery,
            @RequestParam(value = "nameQuery", required = false) String nameQuery,
            @RequestParam(value = "descriptionQuery", required = false) String descriptionQuery,
            @RequestParam(value = "pdfQuery", required = false) String pdfQuery,
            @RequestParam(value = "minReviews", required = false) Integer minReviews,
            @RequestParam(value = "maxReviews", required = false) Integer maxReviews,
            @RequestParam(value = "ratingCategory", required = false) RatingCategory ratingCategory,
            @RequestParam(value = "minCategoryRating", required = false) Double minCategoryRating,
            @RequestParam(value = "maxCategoryRating", required = false) Double maxCategoryRating,
            @RequestParam(value = "operator", required = false, defaultValue = "AND") SearchOperator operator,
            @RequestParam(value = "sortByName", required = false) String sortByName
    ) {
        validateReviewRange(minReviews, maxReviews);
        validateCategoryRange(minCategoryRating, maxCategoryRating);

        return facilityService.searchFacilitiesAdvanced(
                generalQuery,
                nameQuery,
                descriptionQuery,
                pdfQuery,
                minReviews,
                maxReviews,
                ratingCategory,
                minCategoryRating,
                maxCategoryRating,
                operator,
                sortByName
        );
    }

    @GetMapping("/search/more-like-this/{id}")
    public List<FacilitySearchResultDto> moreLikeThis(
            @PathVariable("id") Long facilityId,
            @RequestParam(value = "sortByName", required = false) String sortByName
    ) {
        return facilityService.moreLikeThis(facilityId, sortByName);
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

    private void validateReviewRange(Integer minReviews, Integer maxReviews) {
        if (minReviews != null && minReviews < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "minReviews must be >= 0");
        }
        if (maxReviews != null && maxReviews < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "maxReviews must be >= 0");
        }
        if (minReviews != null && maxReviews != null && minReviews > maxReviews) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "minReviews cannot be greater than maxReviews");
        }
    }

    private void validateCategoryRange(Double minCategoryRating, Double maxCategoryRating) {
        if (minCategoryRating != null && minCategoryRating < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "minCategoryRating must be >= 0");
        }
        if (maxCategoryRating != null && maxCategoryRating < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "maxCategoryRating must be >= 0");
        }
        if (minCategoryRating != null && maxCategoryRating != null && minCategoryRating > maxCategoryRating) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "minCategoryRating cannot be greater than maxCategoryRating");
        }
    }
}

