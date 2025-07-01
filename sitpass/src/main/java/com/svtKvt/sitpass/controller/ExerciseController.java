package com.svtKvt.sitpass.controller;

import com.svtKvt.sitpass.dto.ExerciseDto;
import com.svtKvt.sitpass.model.Exercise;
import com.svtKvt.sitpass.model.Facility;
import com.svtKvt.sitpass.model.User;
import com.svtKvt.sitpass.service.ExerciseService;
import com.svtKvt.sitpass.service.FacilityService;
import com.svtKvt.sitpass.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/exercises")
public class ExerciseController {

    @Autowired
    private ExerciseService exerciseService;

    @Autowired
    private FacilityService facilityService;

    @Autowired
    private UserService userService;

    @GetMapping
    public List<Exercise> getAllExercises() {
        return exerciseService.getAllExercises();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Exercise> getExerciseById(@PathVariable Long id) {
        Optional<Exercise> exercise = exerciseService.getExerciseById(id);
        return exercise.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Exercise>> getExercisesByUserId(@PathVariable Long userId) {
        List<Exercise> exercises = exerciseService.getExercisesByUserId(userId);
        if (exercises.isEmpty()) {
            return ResponseEntity.noContent().build(); // Ako nema termina, vraća se 204 No Content
        }
        return ResponseEntity.ok(exercises);
    }

    @GetMapping("/user/{userId}/facility/{facilityId}")
    public ResponseEntity<List<Exercise>> getExercisesByUserIdAndFacilityId(
            @PathVariable Long userId,
            @PathVariable Long facilityId) {
        List<Exercise> exercises = exerciseService.getExercisesByUserIdAndFacilityId(userId, facilityId);
        if (exercises.isEmpty()) {
            return ResponseEntity.noContent().build(); // Returns 204 No Content if no exercises are found
        }
        return ResponseEntity.ok(exercises);
    }



    @PostMapping
    public ResponseEntity<Exercise> createExercise(@RequestBody ExerciseDto exerciseDto) {
        // Fetch the Facility and User from their respective IDs
        Facility facility = facilityService.findFacilityById(exerciseDto.getFacilityId());
        User user = userService.findUserById(exerciseDto.getUserId());

        if (facility == null || user == null) {
            return ResponseEntity.badRequest().build(); // Handle cases where the facility or user does not exist
        }

        // Create a new Exercise instance and set properties
        Exercise exercise = new Exercise();
        exercise.setStartTime(exerciseDto.getStartTime());
        exercise.setEndTime(exerciseDto.getEndTime());
        exercise.setFacility(facility);
        exercise.setUser(user);

        // Save the Exercise entity
        Exercise savedExercise = exerciseService.saveExercise(exercise);
        return ResponseEntity.ok(savedExercise);
    }


    @PutMapping("/{id}")
    public ResponseEntity<Exercise> updateExercise(@PathVariable Long id, @RequestBody Exercise exerciseDetails) {
        Optional<Exercise> exercise = exerciseService.getExerciseById(id);
        if (exercise.isPresent()) {
            Exercise updatedExercise = exercise.get();
            // Ažuriraj polja prema exerciseDetails
            updatedExercise.setStartTime(exerciseDetails.getStartTime());
            updatedExercise.setEndTime(exerciseDetails.getEndTime());
            // Dodatna ažuriranja prema potrebi

            return ResponseEntity.ok(exerciseService.saveExercise(updatedExercise));
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteExercise(@PathVariable Long id) {
        exerciseService.deleteExercise(id);
        return ResponseEntity.noContent().build();
    }
}
