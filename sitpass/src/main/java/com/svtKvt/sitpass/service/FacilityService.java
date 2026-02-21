package com.svtKvt.sitpass.service;

import com.svtKvt.sitpass.model.Facility;
import com.svtKvt.sitpass.model.Image;
import com.svtKvt.sitpass.repository.FacilityRepository;
import com.svtKvt.sitpass.repository.ImageRepository;
import com.svtKvt.sitpass.search.FacilitySearchDocument;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class FacilityService {

    private final FacilityRepository facilityRepository;
    private final ImageRepository imageRepository;
    private final FacilitySearchIndexService facilitySearchIndexService;
    private final ObjectStorageService objectStorageService;
    private final PdfTextExtractorService pdfTextExtractorService;

    public FacilityService(
            FacilityRepository facilityRepository,
            ImageRepository imageRepository,
            FacilitySearchIndexService facilitySearchIndexService,
            ObjectStorageService objectStorageService,
            PdfTextExtractorService pdfTextExtractorService
    ) {
        this.facilityRepository = facilityRepository;
        this.imageRepository = imageRepository;
        this.facilitySearchIndexService = facilitySearchIndexService;
        this.objectStorageService = objectStorageService;
        this.pdfTextExtractorService = pdfTextExtractorService;
    }

    public List<Facility> getAllFacilities() {
        return facilityRepository.findAll();
    }

    public List<Facility> searchFacilities(String query) {
        List<FacilitySearchDocument> indexedResults = facilitySearchIndexService.search(query);
        if (indexedResults.isEmpty()) {
            return List.of();
        }

        List<Long> ids = indexedResults.stream()
                .map(FacilitySearchDocument::getId)
                .toList();

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
}

