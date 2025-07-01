package com.svtKvt.sitpass.service;

import com.svtKvt.sitpass.model.Exercise;
import com.svtKvt.sitpass.repository.ExerciseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ExerciseService {

    @Autowired
    private ExerciseRepository exerciseRepository;

    public List<Exercise> getAllExercises() {
        return exerciseRepository.findAll();
    }

    public Optional<Exercise> getExerciseById(Long id) {
        return exerciseRepository.findById(id);
    }

    public Exercise saveExercise(Exercise exercise) {
        return exerciseRepository.save(exercise);
    }

    public List<Exercise> getExercisesByUserId(Long userId) {
        return exerciseRepository.findByUserId(userId);
    }

    public List<Exercise> getExercisesByUserIdAndFacilityId(Long userId, Long facilityId) {

        return exerciseRepository.findByUserIdAndFacilityId(userId, facilityId);
    }

    public void deleteExercise(Long id) {
        exerciseRepository.deleteById(id);
    }
}
