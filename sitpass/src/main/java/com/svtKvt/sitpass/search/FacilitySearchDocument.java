package com.svtKvt.sitpass.search;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.InnerField;
import org.springframework.data.elasticsearch.annotations.MultiField;
import org.springframework.data.elasticsearch.annotations.Setting;

@Document(indexName = "facilities")
@Setting(settingPath = "elasticsearch/facility-settings.json")
@JsonIgnoreProperties(ignoreUnknown = true)
public class FacilitySearchDocument {

    @Id
    private Long id;

    @MultiField(
            mainField = @Field(type = FieldType.Text, analyzer = "serbian_custom", searchAnalyzer = "serbian_query"),
            otherFields = {
                    @InnerField(suffix = "sort", type = FieldType.Keyword, normalizer = "lowercase_normalizer")
            }
    )
    private String name;

    @Field(type = FieldType.Text, analyzer = "serbian_custom", searchAnalyzer = "serbian_query")
    private String description;

    @Field(type = FieldType.Text, analyzer = "serbian_custom", searchAnalyzer = "serbian_query")
    private String pdfContent;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPdfContent() {
        return pdfContent;
    }

    public void setPdfContent(String pdfContent) {
        this.pdfContent = pdfContent;
    }
}
