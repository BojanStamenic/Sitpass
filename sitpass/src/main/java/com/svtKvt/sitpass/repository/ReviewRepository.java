package com.svtKvt.sitpass.repository;

import com.svtKvt.sitpass.model.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findByFacilityId(Long facilityId);

    @Query("""
            SELECT r.facility.id, AVG(
                CASE
                    WHEN :category = 'equipment' THEN ra.equipment
                    WHEN :category = 'staff' THEN ra.staff
                    WHEN :category = 'hygiene' THEN ra.hygiene
                    WHEN :category = 'space' THEN ra.space
                    ELSE NULL
                END
            )
            FROM Review r
            JOIN r.rates ra
            GROUP BY r.facility.id
            """)
    List<Object[]> findAverageByFacilityForCategory(@Param("category") String category);
}
