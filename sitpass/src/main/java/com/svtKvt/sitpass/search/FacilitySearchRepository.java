package com.svtKvt.sitpass.search;

import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.util.List;

public interface FacilitySearchRepository extends ElasticsearchRepository<FacilitySearchDocument, Long> {

    @Query("""
            {
              "simple_query_string": {
                "query": "?0",
                "fields": ["name^3", "description^2", "pdfContent"],
                "default_operator": "and",
                "analyze_wildcard": true
              }
            }
            """)
    List<FacilitySearchDocument> searchByQuery(String query);
}

