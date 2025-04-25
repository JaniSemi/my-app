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
 * Sovellus-logiikkakerros {@link Measurement}-entiteetille.
 *  •  Yksinkertainen CRUD
 *  •  Paginointi + vapaaehtoiset filtteri-specifikaatiot
 *  •  Hakutapoja henkilö-ID:n perusteella
 */
@Service
public class MeasurementService {

    private final MeasurementRepository repository;

    public MeasurementService(MeasurementRepository repository) {
        this.repository = repository;
    }

    /* ────────────  CRUD  ──────────── */

    public Optional<Measurement> get(Long id) {
        return repository.findById(id);
    }

    public Measurement save(Measurement entity) {
        return repository.save(entity);
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }

    /* ────────────  LISTAUKSET  ──────────── */

    /** Kaikki mittaukset sivutettuna. */
    public Page<Measurement> list(Pageable pageable) {
        return repository.findAll(pageable);
    }

    /** Listaus Specification-filtterillä (esim. pituus & paino). */
    public Page<Measurement> list(Pageable pageable, Specification<Measurement> filter) {
        return repository.findAll(filter, pageable);
    }

    /** Kaikki mittaukset tietylle henkilölle ilman paginointia. */
    public List<Measurement> listByPerson(Long personId) {
        return repository.findByPersonId(personId);
    }

    public List<Measurement> list() {
        return repository.findAll();
    }

    public long count() {
        return repository.count();
    }
}
