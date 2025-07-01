package com.svtKvt.sitpass.model;

import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
public class Manages {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate startDate;
    private LocalDate endDate;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "facility_id")
    private Facility facility;

    // Getters and setters
}
