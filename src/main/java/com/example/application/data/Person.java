package com.example.application.data;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;

/**
 * Person entity - basic information and relationship to measurements.
 */
@Entity
@Table(name = "persons")
public class Person extends AbstractEntity {

    /* --------------------------------------------------
     *  Basic fields
     * -------------------------------------------------- */

    @NotBlank
    @Column(nullable = false)
    private String firstName;

    @NotBlank
    @Column(nullable = false)
    private String lastName;

    @Past
    private LocalDate dateOfBirth;

    /** M = male, F = female, U = unspecified */
    @Pattern(regexp = "M|F|U")
    @Column(length = 1)
    private String gender = "U";

    @Email
    @Column(unique = true)
    private String email;

    private String phone;
    private String occupation;
    private String role;

    /** Can be used to mark "important customer," etc. */
    private Boolean important = Boolean.FALSE;

    /* --------------------------------------------------
     *  Helper field (not stored in database)
     * -------------------------------------------------- */
    @Transient
    public int getAge() {
        return dateOfBirth == null ? 0
                : Period.between(dateOfBirth, LocalDate.now()).getYears();
    }

    /* --------------------------------------------------
     *  Relationship to measurements
     * -------------------------------------------------- */
    @OneToMany(mappedBy = "person",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY)
    private List<Measurement> measurements = new ArrayList<>();

    /* --------------------------------------------------
     *  Getters & setters
     * -------------------------------------------------- */

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public LocalDate getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(LocalDate dateOfBirth) { this.dateOfBirth = dateOfBirth; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getOccupation() { return occupation; }
    public void setOccupation(String occupation) { this.occupation = occupation; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public Boolean getImportant() { return important; }
    public void setImportant(Boolean important) { this.important = important; }

    public List<Measurement> getMeasurements() { return measurements; }
    public void setMeasurements(List<Measurement> measurements) { this.measurements = measurements; }

    /* --------------------------------------------------
     *  Helper methods for relationship
     * -------------------------------------------------- */
    public void addMeasurement(Measurement m) {
        m.setPerson(this);
        this.measurements.add(m);
    }

    public void removeMeasurement(Measurement m) {
        m.setPerson(null);
        this.measurements.remove(m);
    }
}