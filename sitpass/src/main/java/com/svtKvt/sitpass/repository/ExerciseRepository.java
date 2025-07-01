package com.svtKvt.sitpass.repository;

import com.svtKvt.sitpass.model.Exercise;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ExerciseRepository extends JpaRepository<Exercise, Long> {
    List<Exercise> findByUserId(Long userId);

    List<Exercise> findByUserIdAndFacilityId(Long userId, Long facilityId);
}
