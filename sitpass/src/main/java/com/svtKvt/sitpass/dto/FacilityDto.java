package com.svtKvt.sitpass.dto;

import com.svtKvt.sitpass.model.Facility;
import com.svtKvt.sitpass.model.Image;
import com.svtKvt.sitpass.model.Discipline;
import com.svtKvt.sitpass.model.WorkDay;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FacilityDto {
    private Long id;
    private String name;
    private String description;
    private LocalDate createdAt;
    private String address;
    private String city;
    private Double totalRating;
    private boolean active;
    private List<ImageDto> images;
    private List<DisciplineDto> disciplines;
    private List<WorkDayDto> workDays;

    public Facility convertToModel(){
        return Facility.builder()
                .id(getId())
                .name(getName())
                .description(getDescription())
                .createdAt(getCreatedAt())
                .address(getAddress())
                .city(getCity())
                .totalRating(getTotalRating())
                .active(isActive())
                .workDays(getWorkDays().stream().map(WorkDayDto::convertToModel).toList())
                .disciplines(getDisciplines().stream().map(DisciplineDto::convertToModel).toList())
                .build();

    }

    public static FacilityDto convertToDto(Facility facility){
        return FacilityDto.builder()
                .id(facility.getId())
                .name(facility.getName())
                .description(facility.getDescription())
                .createdAt(facility.getCreatedAt())
                .address(facility.getAddress())
                .city(facility.getCity())
                .totalRating(facility.getTotalRating())
                .active(facility.isActive())
                .workDays(facility.getWorkDays().stream().map(WorkDayDto::convertToDto).toList())
                .disciplines(facility.getDisciplines().stream().map(DisciplineDto::convertToDto).toList())
                .build();

    }
}
