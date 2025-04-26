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
 * Sovelluslogiikka Person-entiteetille.
 * Sisältää kaksi nimisettiä:
 *   • savePerson / findPerson / deletePerson – käytössä UI-näkymissä
 *   • save / get / delete / list            – käytössä REST-rajapinnoissa
 */
@Service
@Transactional
public class PersonService {

    private final PersonRepository repository;

    public PersonService(PersonRepository repository) {
        this.repository = repository;
    }

    /* ======================================================================
     *  -----------  YHTENÄISET CRUD-METODIT (REST)  ------------------------
     * ==================================================================== */

    public Optional<Person> get(Long id) {
        return repository.findById(id);
    }

    public Person save(Person person) {
        return repository.save(person);
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }

    public List<Person> list() {
        return repository.findAll();
    }

    /* ======================================================================
     *  -----------  AIEMMIN KÄYTETYT NIMET (UI)  ----------------------------
     * ==================================================================== */

    public Person savePerson(Person person) {
        return save(person);              // delegoidaan
    }

    public Optional<Person> findPerson(Long id) {
        return get(id);
    }

    public void deletePerson(Long id) {
        delete(id);
    }

    public List<Person> findAll() {
        // oletusjärjestys sukunimi + etunimi
        return repository.findAll(Sort.by("lastName", "firstName"));
    }

    /* ======================================================================
     *  -----------  Listaus & sivutus  -------------------------------------
     * ==================================================================== */

    /** Paginoitu listaus. */
    public Page<Person> listPersons(Pageable pageable) {
        return repository.findAll(pageable);
    }

    /** Kaikki ilman sivutusta (alias listAll). */
    public List<Person> listAll() {
        return list();
    }

    public long count() {
        return repository.count();
    }

    /* ======================================================================
     *  -----------  Specification-filtterit (tarvittaessa)  -----------------
     * ==================================================================== */
    /*
    public Page<Person> listPersons(Pageable pageable, Specification<Person> filter) {
        return repository.findAll(filter, pageable);
    }
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
