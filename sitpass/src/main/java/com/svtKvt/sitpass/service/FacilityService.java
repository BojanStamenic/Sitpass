package com.svtKvt.sitpass.service;

import com.svtKvt.sitpass.dto.FacilitySearchResultDto;
import com.svtKvt.sitpass.dto.RatingCategory;
import com.svtKvt.sitpass.dto.SearchOperator;
import com.svtKvt.sitpass.model.Facility;
import com.svtKvt.sitpass.model.Image;
import com.svtKvt.sitpass.repository.FacilityRepository;
import com.svtKvt.sitpass.repository.ImageRepository;
import com.svtKvt.sitpass.repository.ReviewRepository;
import com.svtKvt.sitpass.search.FacilitySearchDocument;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class FacilityService {

    private final FacilityRepository facilityRepository;
    private final ImageRepository imageRepository;
    private final ReviewRepository reviewRepository;
    private final FacilitySearchIndexService facilitySearchIndexService;
    private final ObjectStorageService objectStorageService;
    private final PdfTextExtractorService pdfTextExtractorService;

    public FacilityService(
            FacilityRepository facilityRepository,
            ImageRepository imageRepository,
            ReviewRepository reviewRepository,
            FacilitySearchIndexService facilitySearchIndexService,
            ObjectStorageService objectStorageService,
            PdfTextExtractorService pdfTextExtractorService
    ) {
        this.facilityRepository = facilityRepository;
        this.imageRepository = imageRepository;
        this.reviewRepository = reviewRepository;
        this.facilitySearchIndexService = facilitySearchIndexService;
        this.objectStorageService = objectStorageService;
        this.pdfTextExtractorService = pdfTextExtractorService;
    }

    public List<Facility> getAllFacilities() {
        return facilityRepository.findAll();
    }

    public List<Facility> searchFacilities(String query) {
        return searchFacilities(query, null, null);
    }

    public List<Facility> searchFacilities(String query, Integer minReviews, Integer maxReviews) {
        List<FacilitySearchResultDto> results = searchFacilitiesAdvanced(
                query,
                null,
                null,
                null,
                minReviews,
                maxReviews,
                null,
                null,
                null,
                SearchOperator.AND,
                null
        );
        if (results.isEmpty()) {
            return List.of();
        }

        List<Long> ids = results.stream().map(FacilitySearchResultDto::getId).toList();
        Map<Long, Facility> byId = facilityRepository.findAllById(ids).stream()
                .collect(Collectors.toMap(Facility::getId, facility -> facility));

        List<Facility> ordered = new ArrayList<>();
        for (Long id : ids) {
            Facility facility = byId.get(id);
            if (facility != null) {
                ordered.add(facility);
            }
        }
        return ordered;
    }

    public List<FacilitySearchResultDto> searchFacilitiesAdvanced(
            String generalQuery,
            String nameQuery,
            String descriptionQuery,
            String pdfQuery,
            Integer minReviews,
            Integer maxReviews,
            RatingCategory ratingCategory,
            Double minCategoryRating,
            Double maxCategoryRating,
            SearchOperator operator,
            String sortByName
    ) {
        SearchOperator effectiveOperator = operator == null ? SearchOperator.AND : operator;
        boolean hasTextCriteria = hasAnyTextCriteria(generalQuery, nameQuery, descriptionQuery, pdfQuery);
        boolean hasReviewRange = minReviews != null || maxReviews != null;
        boolean hasCategoryRange = (minCategoryRating != null || maxCategoryRating != null) && ratingCategory != null;

        List<FacilitySearchIndexService.IndexedSearchHit> searchHits;
        if (hasTextCriteria) {
            searchHits = facilitySearchIndexService.searchAdvanced(
                    generalQuery,
                    nameQuery,
                    descriptionQuery,
                    pdfQuery,
                    effectiveOperator,
                    sortByName
            );
        } else {
            searchHits = List.of();
        }
        
        Map<Long, String> highlights = new HashMap<>();
        for (FacilitySearchIndexService.IndexedSearchHit hit : searchHits) {
            highlights.put(hit.id(), hit.highlight());
        }

        Set<Long> reviewRangeIds = null;
        if (hasReviewRange) {
            Long min = minReviews == null ? null : minReviews.longValue();
            Long max = maxReviews == null ? null : maxReviews.longValue();
            reviewRangeIds = new HashSet<>(facilityRepository.findIdsByReviewCountRange(min, max));
        }

        Set<Long> categoryRangeIds = null;
        if (hasCategoryRange) {
            categoryRangeIds = filterIdsByCategoryRatingRange(ratingCategory, minCategoryRating, maxCategoryRating);
        }

        List<Long> allIds = facilityRepository.findAll().stream()
                .map(Facility::getId)
                .toList();

        List<Long> orderedIds;
        if (effectiveOperator == SearchOperator.OR) {
            LinkedHashSet<Long> merged = new LinkedHashSet<>();

            if (hasTextCriteria) {
                for (FacilitySearchIndexService.IndexedSearchHit hit : searchHits) {
                    merged.add(hit.id());
                }
            }

            if (hasReviewRange && reviewRangeIds != null) {
                for (Long id : allIds) {
                    if (reviewRangeIds.contains(id)) {
                        merged.add(id);
                    }
                }
            }

            if (hasCategoryRange && categoryRangeIds != null) {
                for (Long id : allIds) {
                    if (categoryRangeIds.contains(id)) {
                        merged.add(id);
                    }
                }
            }

            if (!hasTextCriteria && !hasReviewRange && !hasCategoryRange) {
                merged.addAll(allIds);
            }
            orderedIds = new ArrayList<>(merged);
        } else {
            orderedIds = hasTextCriteria
                    ? new ArrayList<>(searchHits.stream().map(FacilitySearchIndexService.IndexedSearchHit::id).toList())
                    : new ArrayList<>(allIds);

            if (hasReviewRange && reviewRangeIds != null) {
                Set<Long> finalReviewRangeIds = reviewRangeIds;
                orderedIds = orderedIds.stream()
                        .filter(finalReviewRangeIds::contains)
                        .toList();
            }

            if (hasCategoryRange && categoryRangeIds != null) {
                Set<Long> finalCategoryRangeIds = categoryRangeIds;
                orderedIds = orderedIds.stream()
                        .filter(finalCategoryRangeIds::contains)
                        .toList();
            }
        }

        if (orderedIds.isEmpty()) {
            return List.of();
        }

        Map<Long, Facility> byId = facilityRepository.findAllById(orderedIds).stream()
                .collect(Collectors.toMap(Facility::getId, facility -> facility));

        List<Facility> facilities = new ArrayList<>();
        for (Long id : orderedIds) {
            Facility facility = byId.get(id);
            if (facility != null) {
                facilities.add(facility);
            }
        }

        if ("asc".equalsIgnoreCase(sortByName)) {
            facilities.sort(Comparator.comparing(f -> Optional.ofNullable(f.getName()).orElse(""), String.CASE_INSENSITIVE_ORDER));
        } else if ("desc".equalsIgnoreCase(sortByName)) {
            facilities.sort(Comparator.comparing((Facility f) -> Optional.ofNullable(f.getName()).orElse(""), String.CASE_INSENSITIVE_ORDER).reversed());
        }

        List<FacilitySearchResultDto> result = new ArrayList<>();
        for (Facility facility : facilities) {
            result.add(FacilitySearchResultDto.fromFacility(facility, highlights.get(facility.getId())));
        }
        return result;
    }

    public List<FacilitySearchResultDto> moreLikeThis(Long facilityId, String sortByName) {
        List<FacilitySearchIndexService.IndexedSearchHit> hits = facilitySearchIndexService.moreLikeThis(facilityId, sortByName);
        if (hits.isEmpty()) {
            return List.of();
        }

        List<Long> ids = hits.stream().map(FacilitySearchIndexService.IndexedSearchHit::id).toList();
        Map<Long, String> highlights = new HashMap<>();
        for (FacilitySearchIndexService.IndexedSearchHit hit : hits) {
            highlights.put(hit.id(), hit.highlight());
        }

        Map<Long, Facility> byId = facilityRepository.findAllById(ids).stream()
                .collect(Collectors.toMap(Facility::getId, facility -> facility));

        List<FacilitySearchResultDto> result = new ArrayList<>();
        for (Long id : ids) {
            Facility facility = byId.get(id);
            if (facility != null) {
                result.add(FacilitySearchResultDto.fromFacility(facility, highlights.get(id)));
            }
        }

        if ("asc".equalsIgnoreCase(sortByName)) {
            result.sort(Comparator.comparing(r -> Optional.ofNullable(r.getName()).orElse(""), String.CASE_INSENSITIVE_ORDER));
        } else if ("desc".equalsIgnoreCase(sortByName)) {
            result.sort(Comparator.comparing((FacilitySearchResultDto r) -> Optional.ofNullable(r.getName()).orElse(""), String.CASE_INSENSITIVE_ORDER).reversed());
        }

        return result;
    }

    public Facility findFacilityById(Long id) {
        return facilityRepository.findById(id).orElse(null);
    }

    public Optional<Facility> getFacilityById(Long id) {
        return facilityRepository.findById(id);
    }

    public Facility saveFacility(Facility facility) {
        Facility saved = facilityRepository.save(facility);
        String pdfContent = facilitySearchIndexService.getIndexedPdfContent(saved.getId());
        facilitySearchIndexService.indexFacility(saved, pdfContent);
        return saved;
    }

    public Facility uploadPdf(Long facilityId, MultipartFile file) {
        Facility facility = facilityRepository.findById(facilityId)
                .orElseThrow(() -> new IllegalArgumentException("Facility not found."));

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("PDF file is empty.");
        }
        if (file.getOriginalFilename() == null || !file.getOriginalFilename().toLowerCase(Locale.ROOT).endsWith(".pdf")) {
            throw new IllegalArgumentException("Only PDF files are supported.");
        }

        String objectKey = "facilities/" + facilityId + "/docs/" + UUID.randomUUID() + ".pdf";
        try {
            objectStorageService.upload(
                    objectKey,
                    file.getInputStream(),
                    "application/pdf",
                    file.getSize()
            );

            String pdfText = pdfTextExtractorService.extractText(file.getInputStream());

            facility.setPdfObjectKey(objectKey);
            facility.setPdfFileName(file.getOriginalFilename());
            Facility saved = facilityRepository.save(facility);
            facilitySearchIndexService.indexFacility(saved, pdfText);
            return saved;
        } catch (Exception e) {
            throw new RuntimeException("Failed to upload facility PDF.", e);
        }
    }

    public ObjectStorageService.StoredObject downloadPdf(Long facilityId) {
        Facility facility = facilityRepository.findById(facilityId)
                .orElseThrow(() -> new IllegalArgumentException("Facility not found."));

        if (facility.getPdfObjectKey() == null || facility.getPdfObjectKey().isBlank()) {
            throw new IllegalArgumentException("Facility has no uploaded PDF.");
        }
        return objectStorageService.download(facility.getPdfObjectKey());
    }

    public Image uploadImage(Long facilityId, MultipartFile file) {
        Facility facility = facilityRepository.findById(facilityId)
                .orElseThrow(() -> new IllegalArgumentException("Facility not found."));

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Image file is empty.");
        }

        String originalName = Optional.ofNullable(file.getOriginalFilename()).orElse("image.bin");
        String objectKey = "facilities/" + facilityId + "/images/" + UUID.randomUUID() + "-" + originalName;

        try {
            objectStorageService.upload(
                    objectKey,
                    file.getInputStream(),
                    Optional.ofNullable(file.getContentType()).orElse("application/octet-stream"),
                    file.getSize()
            );

            Image image = new Image();
            image.setFacility(facility);
            image.setPath(objectKey);
            return imageRepository.save(image);
        } catch (Exception e) {
            throw new RuntimeException("Failed to upload facility image.", e);
        }
    }

    public ObjectStorageService.StoredObject downloadImage(Long imageId) {
        Image image = imageRepository.findById(imageId)
                .orElseThrow(() -> new IllegalArgumentException("Image not found."));
        return objectStorageService.download(image.getPath());
    }

    public void deleteFacility(Long id) {
        facilityRepository.deleteById(id);
        facilitySearchIndexService.deleteByFacilityId(id);
    }

    private Set<Long> filterIdsByCategoryRatingRange(RatingCategory ratingCategory, Double minCategoryRating, Double maxCategoryRating) {
        Set<Long> ids = new HashSet<>();
        List<Object[]> rows = reviewRepository.findAverageByFacilityForCategory(ratingCategory.dbField());
        for (Object[] row : rows) {
            if (row == null || row.length < 2 || row[0] == null || row[1] == null) {
                continue;
            }
            Long facilityId = ((Number) row[0]).longValue();
            double average = ((Number) row[1]).doubleValue();

            if (minCategoryRating != null && average < minCategoryRating) {
                continue;
            }
            if (maxCategoryRating != null && average > maxCategoryRating) {
                continue;
            }
            ids.add(facilityId);
        }
        return ids;
    }

    private boolean hasAnyTextCriteria(String generalQuery, String nameQuery, String descriptionQuery, String pdfQuery) {
        return isNotBlank(generalQuery) || isNotBlank(nameQuery) || isNotBlank(descriptionQuery) || isNotBlank(pdfQuery);
    }

    private boolean isNotBlank(String value) {
        return value != null && !value.trim().isEmpty();
    }
}

