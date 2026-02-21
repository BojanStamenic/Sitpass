package com.svtKvt.sitpass.search;

import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.util.List;

public interface FacilitySearchRepository extends ElasticsearchRepository<FacilitySearchDocument, Long> {

    @Query("""
            {
              "multi_match": {
                "query": "?0",
                "fields": ["name^3", "description^2", "pdfContent"]
              }
            }
            """)
    List<FacilitySearchDocument> searchByQuery(String query);
}

