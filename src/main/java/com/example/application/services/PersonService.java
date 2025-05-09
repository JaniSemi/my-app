package com.example.application.services;

import com.example.application.data.Person;
import com.example.application.data.PersonRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Service layer for Person entity with standardized method names.
 */
@Service
@Transactional
public class PersonService {

    private final PersonRepository repository;

    public PersonService(PersonRepository repository) {
        this.repository = repository;
    }

    /* ======================================================================
     *  -----------  CORE CRUD METHODS  ------------------------
     * ==================================================================== */

    /**
     * Find a person by ID
     */
    public Optional<Person> get(Long id) {
        return repository.findById(id);
    }

    /**
     * Save or update a person
     */
    public Person save(Person person) {
        return repository.save(person);
    }

    /**
     * Delete a person by ID
     */
    public void delete(Long id) {
        repository.deleteById(id);
    }

    /**
     * Get all persons sorted by last name and first name
     */
    public List<Person> findAll() {
        return repository.findAll(Sort.by("lastName", "firstName"));
    }

    /* ======================================================================
     *  -----------  Listing & Pagination  -------------------------------------
     * ==================================================================== */

    /**
     * Paginated list of all persons
     */
    public Page<Person> listPersons(Pageable pageable) {
        return repository.findAll(pageable);
    }

    /**
     * Paginated list of persons with filter
     */
    public Page<Person> listPersons(Pageable pageable, Specification<Person> filter) {
        return repository.findAll(filter, pageable);
    }

    /**
     * List all persons without pagination
     */
    public List<Person> listAll() {
        return findAll();
    }

    /**
     * Count all persons
     */
    public long count() {
        return repository.count();
    }

    /* ======================================================================
     *  -----------  Specification-filtterit (tarvittaessa)  -----------------
     * ==================================================================== */

    /**
     * List persons with last name and/or gender filters
     */
    public Page<Person> listPersons(Pageable pageable,
                                    String lastNameFilter,
                                    String genderFilter) {

        Specification<Person> spec = Specification.where(null);

        if (lastNameFilter != null && !lastNameFilter.isBlank()) {
            spec = spec.and((root, q, cb) ->
                    cb.like(cb.lower(root.get("lastName")),
                            "%" + lastNameFilter.toLowerCase() + "%"));
        }
        if (genderFilter != null && !genderFilter.isBlank()) {
            spec = spec.and((root, q, cb) ->
                    cb.equal(root.get("gender"), genderFilter));
        }
        return repository.findAll(spec, pageable);
    }
}