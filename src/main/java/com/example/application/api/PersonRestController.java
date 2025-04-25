package com.example.application.api;

import com.example.application.data.Person;
import com.example.application.services.PersonService;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST – rajapinta Person-entiteetille.
 * Polun alkuosa /api/persons on määritelty @RequestMapping-tasolla.
 */
@RestController
@RequestMapping("/api/persons")
public class PersonRestController {

    private final PersonService service;

    public PersonRestController(PersonService service) {
        this.service = service;
    }

    /* --------------------------------------------------------------------
     *  READ
     * ------------------------------------------------------------------ */

    /** GET /api/persons  – kaikki henkilöt ilman sivutusta. */
    @GetMapping
    public List<Person> getAll() {
        return service.list();
    }

    /** GET /api/persons/{id} – yksittäinen henkilö. */
    @GetMapping("/{id}")
    public Person get(@PathVariable Long id) {
        return service.get(id)
                .orElseThrow(() -> new PersonNotFoundException(id));
    }

    /* --------------------------------------------------------------------
     *  CREATE
     * ------------------------------------------------------------------ */

    /** POST /api/persons – luo uusi henkilö. */
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public Person create(@Validated @RequestBody Person person) {
        // person.getId() on null → tallennetaan uutena
        return service.save(person);
    }

    /* --------------------------------------------------------------------
     *  UPDATE
     * ------------------------------------------------------------------ */

    /** PUT /api/persons/{id} – päivitä olemassa oleva henkilö. */
    @PutMapping("/{id}")
    public Person update(@PathVariable Long id,
                         @Validated @RequestBody Person person) {

        if (service.get(id).isEmpty())
            throw new PersonNotFoundException(id);

        person.setId(id);          // varmistetaan oikea id
        return service.save(person);
    }

    /* --------------------------------------------------------------------
     *  DELETE
     * ------------------------------------------------------------------ */

    /** DELETE /api/persons/{id} – poista henkilö. */
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }

    /* --------------------------------------------------------------------
     *  404 -poikkeus
     * ------------------------------------------------------------------ */

    @ResponseStatus(HttpStatus.NOT_FOUND)
    private static class PersonNotFoundException extends RuntimeException {
        PersonNotFoundException(Long id) {
            super("Henkilöä ei löytynyt: " + id);
        }
    }
}
