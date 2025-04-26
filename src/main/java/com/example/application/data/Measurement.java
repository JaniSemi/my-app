package com.example.application.data;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "measurements")
public class Measurement extends AbstractEntity {

    @Positive(message = "Height must be greater than 0")
    private double heightCm;                   // height in centimeters

    @Positive(message = "Weight must be greater than 0")
    private double weightKg;                   // weight in kilograms

    @PastOrPresent
    private LocalDateTime measuredAt = LocalDateTime.now();

    /* ---------- Relationship to Person ---------- */
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "person_id", nullable = false)
    private Person person;

    /* ---------- Getters & setters ---------- */
    public double getHeightCm() { return heightCm; }
    public void setHeightCm(double heightCm) { this.heightCm = heightCm; }

    public double getWeightKg() { return weightKg; }
    public void setWeightKg(double weightKg) { this.weightKg = weightKg; }

    public LocalDateTime getMeasuredAt() { return measuredAt; }
    public void setMeasuredAt(LocalDateTime measuredAt) { this.measuredAt = measuredAt; }

    public Person getPerson() { return person; }
    public void setPerson(Person person) { this.person = person; }
}