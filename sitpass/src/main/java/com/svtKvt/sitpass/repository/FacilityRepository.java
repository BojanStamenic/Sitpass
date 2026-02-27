package com.svtKvt.sitpass.repository;

import com.svtKvt.sitpass.model.Facility;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface FacilityRepository extends JpaRepository<Facility, Long> {

    @Query("""
            SELECT f.id
            FROM Facility f
            LEFT JOIN Review r ON r.facility = f
            GROUP BY f.id
            HAVING (:minReviews IS NULL OR COUNT(r.id) >= :minReviews)
               AND (:maxReviews IS NULL OR COUNT(r.id) <= :maxReviews)
            """)
    List<Long> findIdsByReviewCountRange(
            @Param("minReviews") Long minReviews,
            @Param("maxReviews") Long maxReviews
    );
}
