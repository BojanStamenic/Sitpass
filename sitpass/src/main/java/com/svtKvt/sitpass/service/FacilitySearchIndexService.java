package com.svtKvt.sitpass.service;

import com.svtKvt.sitpass.model.Facility;
import com.svtKvt.sitpass.search.FacilitySearchDocument;
import com.svtKvt.sitpass.search.FacilitySearchRepository;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.IndexOperations;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class FacilitySearchIndexService {

    private static final Map<Character, String> CYRILLIC_TO_LATIN = new HashMap<>();

    static {
        CYRILLIC_TO_LATIN.put('\u0430', "a");
        CYRILLIC_TO_LATIN.put('\u0431', "b");
        CYRILLIC_TO_LATIN.put('\u0432', "v");
        CYRILLIC_TO_LATIN.put('\u0433', "g");
        CYRILLIC_TO_LATIN.put('\u0434', "d");
        CYRILLIC_TO_LATIN.put('\u0452', "dj");
        CYRILLIC_TO_LATIN.put('\u0435', "e");
        CYRILLIC_TO_LATIN.put('\u0436', "z");
        CYRILLIC_TO_LATIN.put('\u0437', "z");
        CYRILLIC_TO_LATIN.put('\u0438', "i");
        CYRILLIC_TO_LATIN.put('\u0458', "j");
        CYRILLIC_TO_LATIN.put('\u043a', "k");
        CYRILLIC_TO_LATIN.put('\u043b', "l");
        CYRILLIC_TO_LATIN.put('\u0459', "lj");
        CYRILLIC_TO_LATIN.put('\u043c', "m");
        CYRILLIC_TO_LATIN.put('\u043d', "n");
        CYRILLIC_TO_LATIN.put('\u045a', "nj");
        CYRILLIC_TO_LATIN.put('\u043e', "o");
        CYRILLIC_TO_LATIN.put('\u043f', "p");
        CYRILLIC_TO_LATIN.put('\u0440', "r");
        CYRILLIC_TO_LATIN.put('\u0441', "s");
        CYRILLIC_TO_LATIN.put('\u0442', "t");
        CYRILLIC_TO_LATIN.put('\u045b', "c");
        CYRILLIC_TO_LATIN.put('\u0443', "u");
        CYRILLIC_TO_LATIN.put('\u0444', "f");
        CYRILLIC_TO_LATIN.put('\u0445', "h");
        CYRILLIC_TO_LATIN.put('\u0446', "c");
        CYRILLIC_TO_LATIN.put('\u0447', "c");
        CYRILLIC_TO_LATIN.put('\u045f', "dz");
        CYRILLIC_TO_LATIN.put('\u0448', "s");
    }

    private final FacilitySearchRepository facilitySearchRepository;
    private final ElasticsearchOperations elasticsearchOperations;

    public FacilitySearchIndexService(
            FacilitySearchRepository facilitySearchRepository,
            ElasticsearchOperations elasticsearchOperations
    ) {
        this.facilitySearchRepository = facilitySearchRepository;
        this.elasticsearchOperations = elasticsearchOperations;
    }

    public void indexFacility(Facility facility, String pdfContent) {
        ensureIndexExists();
        FacilitySearchDocument document = new FacilitySearchDocument();
        document.setId(facility.getId());
        document.setName(facility.getName());
        document.setDescription(facility.getDescription());
        document.setPdfContent(pdfContent);
        facilitySearchRepository.save(document);
    }

    public String getIndexedPdfContent(Long facilityId) {
        if (!indexExists()) {
            return null;
        }
        return facilitySearchRepository.findById(facilityId)
                .map(FacilitySearchDocument::getPdfContent)
                .orElse(null);
    }

    public List<FacilitySearchDocument> search(String query) {
        ensureIndexExists();
        String normalized = normalizeQuery(query);
        if (normalized.isBlank()) {
            return List.of();
        }
        return facilitySearchRepository.searchByQuery(normalized);
    }

    public void deleteByFacilityId(Long facilityId) {
        if (!indexExists()) {
            return;
        }
        facilitySearchRepository.deleteById(facilityId);
    }

    public void reindexFacilities(List<Facility> facilities) {
        ensureIndexExists();
        for (Facility facility : facilities) {
            String pdfContent = getIndexedPdfContent(facility.getId());
            indexFacility(facility, pdfContent);
        }
    }

    private void ensureIndexExists() {
        IndexOperations indexOps = elasticsearchOperations.indexOps(FacilitySearchDocument.class);
        if (!indexOps.exists()) {
            indexOps.create();
            indexOps.putMapping(indexOps.createMapping(FacilitySearchDocument.class));
        }
    }

    private boolean indexExists() {
        return elasticsearchOperations.indexOps(FacilitySearchDocument.class).exists();
    }

    private String normalizeQuery(String query) {
        if (query == null) {
            return "";
        }

        String lower = query.toLowerCase(Locale.ROOT).trim();
        StringBuilder normalized = new StringBuilder();
        for (char c : lower.toCharArray()) {
            normalized.append(CYRILLIC_TO_LATIN.getOrDefault(c, String.valueOf(c)));
        }
        return normalized.toString();
    }
}
