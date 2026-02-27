package com.svtKvt.sitpass.dto;

import com.svtKvt.sitpass.model.Facility;
import com.svtKvt.sitpass.model.Discipline;
import com.svtKvt.sitpass.model.Image;
import com.svtKvt.sitpass.model.WorkDay;

import java.util.List;

public class FacilitySearchResultDto {

    private Long id;
    private String name;
    private String description;
    private String address;
    private String city;
    private Double totalRating;
    private boolean active;
    private String pdfObjectKey;
    private String pdfFileName;
    private List<Image> images;
    private List<Discipline> disciplines;
    private List<WorkDay> workDays;
    private String highlight;

    public static FacilitySearchResultDto fromFacility(Facility facility, String highlight) {
        FacilitySearchResultDto dto = new FacilitySearchResultDto();
        dto.setId(facility.getId());
        dto.setName(facility.getName());
        dto.setDescription(facility.getDescription());
        dto.setAddress(facility.getAddress());
        dto.setCity(facility.getCity());
        dto.setTotalRating(facility.getTotalRating());
        dto.setActive(facility.isActive());
        dto.setPdfObjectKey(facility.getPdfObjectKey());
        dto.setPdfFileName(facility.getPdfFileName());
        dto.setImages(facility.getImages());
        dto.setDisciplines(facility.getDisciplines());
        dto.setWorkDays(facility.getWorkDays());
        dto.setHighlight(highlight);
        return dto;
    }

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

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public Double getTotalRating() {
        return totalRating;
    }

    public void setTotalRating(Double totalRating) {
        this.totalRating = totalRating;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getPdfObjectKey() {
        return pdfObjectKey;
    }

    public void setPdfObjectKey(String pdfObjectKey) {
        this.pdfObjectKey = pdfObjectKey;
    }

    public String getPdfFileName() {
        return pdfFileName;
    }

    public void setPdfFileName(String pdfFileName) {
        this.pdfFileName = pdfFileName;
    }

    public String getHighlight() {
        return highlight;
    }

    public void setHighlight(String highlight) {
        this.highlight = highlight;
    }

    public List<Image> getImages() {
        return images;
    }

    public void setImages(List<Image> images) {
        this.images = images;
    }

    public List<Discipline> getDisciplines() {
        return disciplines;
    }

    public void setDisciplines(List<Discipline> disciplines) {
        this.disciplines = disciplines;
    }

    public List<WorkDay> getWorkDays() {
        return workDays;
    }

    public void setWorkDays(List<WorkDay> workDays) {
        this.workDays = workDays;
    }
}
