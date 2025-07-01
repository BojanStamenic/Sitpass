package com.svtKvt.sitpass.controller;

import com.svtKvt.sitpass.model.*;
import com.svtKvt.sitpass.service.ExerciseService;
import com.svtKvt.sitpass.service.FacilityService;
import com.svtKvt.sitpass.service.ReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/reviews")
public class ReviewController {

    @Autowired
    private ReviewService reviewService;

    @Autowired
    private ExerciseService exerciseService;

    @Autowired
    private FacilityService facilityService;

    @GetMapping
    public List<Review> getAllReviews() {
        return reviewService.getAllReviews();
    }

    @GetMapping("/facility/{facilityId}")
    public List<Review> getReviewsByFacilityId(@PathVariable Long facilityId) {
        return reviewService.getReviewsByFacilityId(facilityId);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Review> getReviewById(@PathVariable Long id) {
        Optional<Review> review = reviewService.getReviewById(id);
        return review.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Review> createReview(@RequestBody Review review) {
        // Save the review and rate
        Rate rate = review.getRates();
        rate.setReview(review);
        Review savedReview = reviewService.saveReview(review);

        // Fetch the facility associated with this review
        Facility facility = savedReview.getFacility();
        Long facilityId = facility.getId();

        // Fetch the user associated with this review
        User user = savedReview.getUser();  // Assuming review has a reference to the user who created it
        Long userId = user.getId();

        // Calculate the new average rating for the facility
        List<Review> reviews = reviewService.getReviewsByFacilityId(facilityId);

        double totalRating = 0;
        int reviewCount = 0;

        for (Review rev : reviews) {
            Rate revRate = rev.getRates();
            if (revRate != null) {
                double rating = (revRate.getEquipment() + revRate.getStaff() + revRate.getHygiene() + revRate.getSpace()) / 4.0;
                totalRating += rating;
                reviewCount++;
            }
        }

        // Fetch exercises by user ID and facility ID
        List<Exercise> exercises = exerciseService.getExercisesByUserIdAndFacilityId(userId, facilityId);

        // Count the number of exercises and set it in the review
        int exerciseCount = exercises.size();
        savedReview.setExerciseCount(exerciseCount);  // Assuming Review has an exerciseCount field

        // Save the updated review with exercise count
        reviewService.saveReview(savedReview);

        return ResponseEntity.ok(savedReview);
    }



    @PutMapping("/{id}")
    public ResponseEntity<Review> updateReview(@PathVariable Long id, @RequestBody Review reviewDetails) {
        Optional<Review> review = reviewService.getReviewById(id);
        if (review.isPresent()) {
            Review updatedReview = review.get();
            // Ažuriraj polja prema reviewDetails
            updatedReview.setHidden(reviewDetails.getHidden());
            // Dodatna ažuriranja prema potrebi

            return ResponseEntity.ok(reviewService.saveReview(updatedReview));
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReview(@PathVariable Long id) {
        reviewService.deleteReview(id);
        return ResponseEntity.noContent().build();
    }
}
