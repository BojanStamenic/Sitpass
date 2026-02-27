package com.svtKvt.sitpass.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.query_dsl.Operator;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import com.svtKvt.sitpass.dto.SearchOperator;
import com.svtKvt.sitpass.model.Facility;
import com.svtKvt.sitpass.search.FacilitySearchDocument;
import com.svtKvt.sitpass.search.FacilitySearchRepository;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.IndexOperations;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class FacilitySearchIndexService {

    private static final String NAME_FIELD = "name";
    private static final String DESCRIPTION_FIELD = "description";
    private static final String PDF_FIELD = "pdfContent";

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
    private final ElasticsearchClient elasticsearchClient;

    public FacilitySearchIndexService(
            FacilitySearchRepository facilitySearchRepository,
            ElasticsearchOperations elasticsearchOperations,
            ElasticsearchClient elasticsearchClient
    ) {
        this.facilitySearchRepository = facilitySearchRepository;
        this.elasticsearchOperations = elasticsearchOperations;
        this.elasticsearchClient = elasticsearchClient;
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
        List<IndexedSearchHit> hits = searchAdvancedInternal(query, null, null, null, SearchOperator.AND, null, true, true);
        if (hits.isEmpty()) {
            return List.of();
        }

        List<Long> ids = hits.stream().map(IndexedSearchHit::id).toList();
        Map<Long, FacilitySearchDocument> byId = new HashMap<>();
        facilitySearchRepository.findAllById(ids).forEach(document -> byId.put(document.getId(), document));

        List<FacilitySearchDocument> ordered = new ArrayList<>();
        for (Long id : ids) {
            FacilitySearchDocument document = byId.get(id);
            if (document != null) {
                ordered.add(document);
            }
        }
        return ordered;
    }

    public List<IndexedSearchHit> searchAdvanced(
            String generalQuery,
            String nameQuery,
            String descriptionQuery,
            String pdfQuery,
            SearchOperator operator,
            String sortByName
    ) {
        return searchAdvancedInternal(generalQuery, nameQuery, descriptionQuery, pdfQuery, operator, sortByName, true, false);
    }

    private List<IndexedSearchHit> searchAdvancedInternal(
            String generalQuery,
            String nameQuery,
            String descriptionQuery,
            String pdfQuery,
            SearchOperator operator,
            String sortByName,
            boolean autoPrefixGeneral,
            boolean autoPrefixSpecific
    ) {
        ensureIndexExists();

        Query finalQuery = buildCombinedQuery(
                generalQuery,
                nameQuery,
                descriptionQuery,
                pdfQuery,
                operator,
                autoPrefixGeneral,
                autoPrefixSpecific
        );
        return executeSearch(finalQuery, sortByName);
    }

    public List<IndexedSearchHit> moreLikeThis(Long facilityId, String sortByName) {
        ensureIndexExists();
        FacilitySearchDocument source = facilitySearchRepository.findById(facilityId).orElse(null);
        if (source == null) {
            return List.of();
        }

        String likeText = String.join(" ",
                safe(source.getName()),
                safe(source.getDescription()),
                safe(source.getPdfContent())
        ).trim();
        if (likeText.isBlank()) {
            return List.of();
        }

        Query mlt = Query.of(q -> q.moreLikeThis(mltQuery -> mltQuery
                .fields(NAME_FIELD, DESCRIPTION_FIELD, PDF_FIELD)
                .like(l -> l.text(likeText))
                .minTermFreq(1)
                .maxQueryTerms(25)
        ));

        return executeSearch(mlt, sortByName).stream()
                .filter(hit -> !facilityId.equals(hit.id()))
                .toList();
    }

    public void deleteByFacilityId(Long facilityId) {
        if (!indexExists()) {
            return;
        }
        facilitySearchRepository.deleteById(facilityId);
    }

    public void rebuildIndex(List<Facility> facilities) {
        IndexOperations indexOps = elasticsearchOperations.indexOps(FacilitySearchDocument.class);
        if (indexOps.exists()) {
            indexOps.delete();
        }
        indexOps.create();
        indexOps.putMapping(indexOps.createMapping(FacilitySearchDocument.class));

        for (Facility facility : facilities) {
            indexFacility(facility, null);
        }
    }

    public void reindexFacilities(List<Facility> facilities) {
        ensureIndexExists();
        for (Facility facility : facilities) {
            String pdfContent = getIndexedPdfContent(facility.getId());
            indexFacility(facility, pdfContent);
        }
    }

    private List<IndexedSearchHit> executeSearch(Query finalQuery, String sortByName) {
        try {
            SearchResponse<FacilitySearchDocument> response = elasticsearchClient.search(s -> {
                var builder = s
                        .index("facilities")
                        .query(finalQuery)
                        .highlight(h -> h
                                .preTags("<em>")
                                .postTags("</em>")
                                .fields(NAME_FIELD, f -> f.fragmentSize(120).numberOfFragments(1))
                                .fields(DESCRIPTION_FIELD, f -> f.fragmentSize(140).numberOfFragments(1))
                                .fields(PDF_FIELD, f -> f.fragmentSize(180).numberOfFragments(1))
                        );

                return builder;
            }, FacilitySearchDocument.class);

            List<IndexedSearchHit> result = new ArrayList<>();
            for (Hit<FacilitySearchDocument> hit : response.hits().hits()) {
                FacilitySearchDocument source = hit.source();
                if (source == null || source.getId() == null) {
                    continue;
                }
                result.add(new IndexedSearchHit(source.getId(), pickHighlight(hit)));
            }
            return result;
        } catch (Exception e) {
            throw new RuntimeException("Failed to execute Elasticsearch search.", e);
        }
    }

    private Query buildCombinedQuery(
            String generalQuery,
            String nameQuery,
            String descriptionQuery,
            String pdfQuery,
            SearchOperator operator,
            boolean autoPrefixGeneral,
            boolean autoPrefixSpecific
    ) {
        List<Query> clauses = new ArrayList<>();

        Query general = buildGeneralTextQuery(generalQuery, autoPrefixGeneral);
        if (general != null) {
            clauses.add(general);
        }

        Query name = buildFieldQuery(NAME_FIELD, nameQuery, autoPrefixSpecific);
        Query description = buildFieldQuery(DESCRIPTION_FIELD, descriptionQuery, autoPrefixSpecific);
        Query pdf = buildFieldQuery(PDF_FIELD, pdfQuery, autoPrefixSpecific);
        if (name != null) {
            clauses.add(name);
        }
        if (description != null) {
            clauses.add(description);
        }
        if (pdf != null) {
            clauses.add(pdf);
        }

        if (clauses.isEmpty()) {
            return Query.of(q -> q.matchAll(m -> m));
        }

        if (operator == SearchOperator.OR) {
            return Query.of(q -> q.bool(b -> b.should(clauses).minimumShouldMatch("1")));
        }
        return Query.of(q -> q.bool(b -> b.must(clauses)));
    }

    private Query buildGeneralTextQuery(String input, boolean autoPrefix) {
        String normalized = normalizeQuery(input);
        if (normalized.isBlank()) {
            return null;
        }

        if (isFuzzyQuery(normalized)) {
            String term = normalized.substring(1).trim();
            if (term.isBlank()) {
                return null;
            }
            return Query.of(q -> q.bool(b -> b
                    .should(Query.of(s -> s.match(m -> m.field(NAME_FIELD).query(term).fuzziness("AUTO"))))
                    .should(Query.of(s -> s.match(m -> m.field(DESCRIPTION_FIELD).query(term).fuzziness("AUTO"))))
                    .should(Query.of(s -> s.match(m -> m.field(PDF_FIELD).query(term).fuzziness("AUTO"))))
                    .minimumShouldMatch("1")
            ));
        }

        if (isPhraseQuery(normalized)) {
            String phrase = unwrapQuotes(normalized);
            return Query.of(q -> q.multiMatch(m -> m
                    .query(phrase)
                    .fields(NAME_FIELD + "^3", DESCRIPTION_FIELD + "^2", PDF_FIELD)
                    .type(co.elastic.clients.elasticsearch._types.query_dsl.TextQueryType.Phrase)
            ));
        }

        if (isPrefixQuery(normalized)) {
            return Query.of(q -> q.queryString(qs -> qs
                    .query(normalized)
                    .fields(NAME_FIELD + "^3", DESCRIPTION_FIELD + "^2", PDF_FIELD)
                    .defaultOperator(Operator.And)
                    .analyzeWildcard(true)
            ));
        }

        String prepared = autoPrefix ? preparePrefixFriendlyQuery(normalized) : normalized;
        return Query.of(q -> q.simpleQueryString(s -> s
                .query(prepared)
                .fields(NAME_FIELD + "^3", DESCRIPTION_FIELD + "^2", PDF_FIELD)
                .defaultOperator(Operator.And)
                .analyzeWildcard(true)
        ));
    }

    private Query buildFieldQuery(String field, String input, boolean autoPrefix) {
        String normalized = normalizeQuery(input);
        if (normalized.isBlank()) {
            return null;
        }

        if (isFuzzyQuery(normalized)) {
            String term = normalized.substring(1).trim();
            if (term.isBlank()) {
                return null;
            }
            return Query.of(q -> q.match(m -> m.field(field).query(term).fuzziness("AUTO")));
        }

        if (isPhraseQuery(normalized)) {
            String phrase = unwrapQuotes(normalized);
            return Query.of(q -> q.matchPhrase(m -> m.field(field).query(phrase)));
        }

        if (isPrefixQuery(normalized)) {
            return Query.of(q -> q.queryString(qs -> qs
                    .query(normalized)
                    .fields(field)
                    .defaultOperator(Operator.And)
                    .analyzeWildcard(true)
            ));
        }

        String prepared = autoPrefix ? preparePrefixFriendlyQuery(normalized) : normalized;
        return Query.of(q -> q.simpleQueryString(s -> s
                .query(prepared)
                .fields(field)
                .defaultOperator(Operator.And)
                .analyzeWildcard(true)
        ));
    }

    private String pickHighlight(Hit<FacilitySearchDocument> hit) {
        if (hit.highlight() == null || hit.highlight().isEmpty()) {
            return null;
        }
        if (hit.highlight().containsKey(DESCRIPTION_FIELD)) {
            return String.join(" ... ", hit.highlight().get(DESCRIPTION_FIELD));
        }
        if (hit.highlight().containsKey(PDF_FIELD)) {
            return String.join(" ... ", hit.highlight().get(PDF_FIELD));
        }
        if (hit.highlight().containsKey(NAME_FIELD)) {
            return String.join(" ... ", hit.highlight().get(NAME_FIELD));
        }
        return hit.highlight().values().stream().findFirst().map(values -> String.join(" ... ", values)).orElse(null);
    }

    private boolean isPhraseQuery(String query) {
        return query.length() >= 2 && query.startsWith("\"") && query.endsWith("\"");
    }

    private boolean isFuzzyQuery(String query) {
        return query.startsWith("~");
    }

    private boolean isPrefixQuery(String query) {
        return query.contains("*");
    }

    private String unwrapQuotes(String query) {
        return query.substring(1, query.length() - 1).trim();
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

    private String preparePrefixFriendlyQuery(String normalized) {
        if (normalized.contains("\"") || normalized.contains("~") || normalized.contains("*")) {
            return normalized;
        }

        String[] tokens = normalized.split("\\s+");
        StringBuilder prepared = new StringBuilder();
        for (String rawToken : tokens) {
            String token = rawToken.trim();
            if (token.isEmpty()) {
                continue;
            }
            if (prepared.length() > 0) {
                prepared.append(' ');
            }
            prepared.append(token).append('*');
        }
        return prepared.length() > 0 ? prepared.toString() : normalized;
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    public record IndexedSearchHit(Long id, String highlight) {
    }
}
