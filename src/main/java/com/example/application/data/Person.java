package com.example.application.data;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;

/**
 * Henkilö – perustiedot + relaatiot mittauksiin.
 */
@Entity
@Table(name = "persons")
public class Person {

    /* --------------------------------------------------
     *  Peruskentät
     * -------------------------------------------------- */

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

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

    /** Voidaan käyttää esim. “tärkeä asiakas” -merkintään. */
    private Boolean important = Boolean.FALSE;

    /* --------------------------------------------------
     *  Apukenttä (ei tallennu kantaan)
     * -------------------------------------------------- */
    @Transient
    public int getAge() {
        return dateOfBirth == null ? 0
                : Period.between(dateOfBirth, LocalDate.now()).getYears();
    }

    /* --------------------------------------------------
     *  Relaatio mittauksiin
     * -------------------------------------------------- */
    @OneToMany(mappedBy = "person",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY)
    private List<Measurement> measurements = new ArrayList<>();

    /* --------------------------------------------------
     *  Getterit & setterit
     * -------------------------------------------------- */

    public Long getId() { return id; }

    /** Tarvitaan päivitys-operaatioissa (PUT). */
    public void setId(Long id) { this.id = id; }

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
     *  Apumetodit relaatiolle
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
