package com.example.application.services;

import com.example.application.data.Measurement;
import com.example.application.data.MeasurementRepository;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

/**
 * Service layer for {@link Measurement} entity.
 * Simplified with standardized method naming and reduced redundancy.
 */
@Service
public class MeasurementService {

    private final MeasurementRepository repository;

    public MeasurementService(MeasurementRepository repository) {
        this.repository = repository;
    }

    /* ────────────  CORE CRUD OPERATIONS  ──────────── */

    /**
     * Get a measurement by ID
     */
    public Optional<Measurement> get(Long id) {
        return repository.findById(id);
    }

    /**
     * Save or update a measurement
     */
    public Measurement save(Measurement entity) {
        return repository.save(entity);
    }

    /**
     * Delete a measurement by ID
     */
    public void delete(Long id) {
        repository.deleteById(id);
    }

    /* ────────────  LISTING OPERATIONS  ──────────── */

    /**
     * List all measurements with pagination
     */
    public Page<Measurement> list(Pageable pageable) {
        return repository.findAll(pageable);
    }

    /**
     * List measurements with a filter specification and pagination
     */
    public Page<Measurement> list(Pageable pageable, Specification<Measurement> filter) {
        return repository.findAll(filter, pageable);
    }

    /**
     * List all measurements for a specific person
     */
    public List<Measurement> listByPerson(Long personId) {
        return repository.findByPersonId(personId);
    }

    /**
     * List all measurements without pagination
     */
    public List<Measurement> list() {
        return repository.findAll();
    }

    /**
     * Count all measurements
     */
    public long count() {
        return repository.count();
    }

    /**
     * List measurements with person filter and pagination
     */
    public Page<Measurement> list(Pageable pageable, Long personId) {
        if (personId == null) {
            return repository.findAll(pageable);
        }

        Specification<Measurement> spec = (root, query, cb) ->
                cb.equal(root.get("person").get("id"), personId);

        return repository.findAll(spec, pageable);
    }
}