package com.example.application.data;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "measurements")
public class Measurement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Positive(message = "Pituus täytyy olla suurempi kuin 0")
    private double heightCm;                   // pituus senttimetreissä

    @Positive(message = "Paino täytyy olla suurempi kuin 0")
    private double weightKg;                   // paino kilogrammoina

    @PastOrPresent
    private LocalDateTime measuredAt = LocalDateTime.now();

    /* ---------- Relaatio Personiin ---------- */
    @ManyToOne(fetch = FetchType.EAGER, optional = false)

    @JoinColumn(name = "person_id", nullable = false)
    private Person person;

    /* ---------- Getterit & setterit ---------- */
    public Long getId() { return id; }

    public double getHeightCm() { return heightCm; }
    public void setHeightCm(double heightCm) { this.heightCm = heightCm; }

    public double getWeightKg() { return weightKg; }
    public void setWeightKg(double weightKg) { this.weightKg = weightKg; }

    public LocalDateTime getMeasuredAt() { return measuredAt; }
    public void setMeasuredAt(LocalDateTime measuredAt) { this.measuredAt = measuredAt; }

    public Person getPerson() { return person; }
    public void setPerson(Person person) { this.person = person; }

    public void setId(Long id) {
    }
}
