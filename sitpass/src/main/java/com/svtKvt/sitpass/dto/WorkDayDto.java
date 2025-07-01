package com.svtKvt.sitpass.dto;

import com.svtKvt.sitpass.model.DayOfWeek;
import com.svtKvt.sitpass.model.WorkDay;
import com.svtKvt.sitpass.model.Facility;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WorkDayDto {
    private Long id;
    private DayOfWeek day;
    private LocalTime startTime;
    private LocalTime endTime;
    private LocalDate validFrom;
    private Long facilityId;

    // Metoda za konverziju modela u DTO
    public static WorkDayDto convertToDto(WorkDay workDay) {
        return WorkDayDto.builder()
                .id(workDay.getId())
                .day(workDay.getDay())
                .startTime(workDay.getStartTime())
                .endTime(workDay.getEndTime())
                .validFrom(workDay.getValidFrom())
                .facilityId(workDay.getFacility() != null ? workDay.getFacility().getId() : null)
                .build();
    }

    // Metoda za konverziju DTO u model
    public WorkDay convertToModel() {
        return WorkDay.builder()
                .id(this.id)
                .day(this.day)
                .startTime(this.startTime)
                .endTime(this.endTime)
                .validFrom(this.validFrom)
                //  .facility(this.facilityId)
                .build();
    }

}
