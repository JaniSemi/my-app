package com.example.application.api;

import com.example.application.data.Person;
import com.example.application.data.PersonSpecifications;
import com.example.application.services.PersonService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/persons")
public class PersonRestController {

    private final PersonService service;

    public PersonRestController(PersonService service) {
        this.service = service;
    }

    /* ───────────────────────────────────────────────
     *  READ - listing & individual
     * ───────────────────────────────────────────── */

    /**
     * GET /api/persons
     *  - supports query parameters ?lastName= ... &gender= ... &page= ... &size= ...
     */
    @GetMapping
    public Page<Person> getAll(@RequestParam Optional<String> lastName,
                               @RequestParam Optional<String> gender,
                               Pageable pageable) {

        Specification<Person> spec = Specification.where(null);

        if (lastName.isPresent() && !lastName.get().isBlank()) {
            spec = spec.and(PersonSpecifications.lastNameContains(lastName.get()));
        }
        if (gender.isPresent() && !gender.get().isBlank()) {
            spec = spec.and(PersonSpecifications.genderEquals(gender.get()));
        }

        return service.listPersons(pageable, spec);
    }

    /** GET /api/persons/{id} */
    @GetMapping("/{id}")
    public Person get(@PathVariable Long id) {
        return service.get(id)
                .orElseThrow(() -> new PersonNotFoundException(id));
    }

    /* ───────────────────────────────────────────────
     *  CREATE / UPDATE / DELETE
     * ───────────────────────────────────────────── */

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public Person create(@Validated @RequestBody Person person) {
        return service.save(person);
    }

    @PutMapping("/{id}")
    public Person update(@PathVariable Long id,
                         @Validated @RequestBody Person dto) {

        Person existing = service.get(id)
                .orElseThrow(() -> new PersonNotFoundException(id));

        // copy updatable fields from DTO to existing entity
        existing.setFirstName(dto.getFirstName());
        existing.setLastName(dto.getLastName());
        existing.setDateOfBirth(dto.getDateOfBirth());
        existing.setGender(dto.getGender());
        existing.setEmail(dto.getEmail());
        existing.setPhone(dto.getPhone());
        existing.setOccupation(dto.getOccupation());
        existing.setRole(dto.getRole());
        existing.setImportant(dto.getImportant());

        return service.save(existing);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }

    /* 404 Exception */
    @ResponseStatus(HttpStatus.NOT_FOUND)
    private static class PersonNotFoundException extends RuntimeException {
        PersonNotFoundException(Long id) {
            super("Person not found: " + id);
        }
    }
}
