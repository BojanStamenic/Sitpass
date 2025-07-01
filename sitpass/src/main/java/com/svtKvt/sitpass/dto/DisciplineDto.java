package com.svtKvt.sitpass.dto;

import com.svtKvt.sitpass.model.Discipline;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DisciplineDto {
    private Long id;
    private String name;
    private Long facilityId;

    public static DisciplineDto convertToDto(Discipline discipline) {
        return DisciplineDto.builder()
                .id(discipline.getId())
                .name(discipline.getName())
                .facilityId(discipline.getFacility().getId())
                .build();
    }

    public Discipline convertToModel() {
        // Pretvorba DTO-a u model
        return Discipline.builder()
                .id(getId())
                .name(getName())
                // .facility(getFacility().getId())
                .build();
    }

}
