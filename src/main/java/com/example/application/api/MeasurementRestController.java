package com.example.application.api;

import com.example.application.data.Measurement;
import com.example.application.services.MeasurementService;

import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/measurements")
public class MeasurementRestController {

    private final MeasurementService service;

    public MeasurementRestController(MeasurementService service) {
        this.service = service;
    }

    /* ------- LISTAUS ------- */

    @GetMapping                        // GET /api/measurements
    public List<Measurement> all() {
        return service.list();         // kaikki ilman sivutusta
    }

    @GetMapping("/{id}")               // GET /api/measurements/55
    public Measurement one(@PathVariable Long id) {
        return service.get(id)
                .orElseThrow(() -> new MeasurementNotFoundException(id));
    }

    /* Mittaukset tietylle henkilölle: GET /api/measurements/person/12 */
    @GetMapping("/person/{personId}")
    public List<Measurement> byPerson(@PathVariable Long personId) {
        return service.listByPerson(personId);
    }

    /* ------- CREATE ------- */

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping                       // POST /api/measurements
    public Measurement create(@Validated @RequestBody Measurement m) {
        return service.save(m);
    }

    /* ------- UPDATE ------- */

    @PutMapping("/{id}")     // PUT /api/measurements/{id}
    public Measurement update(@PathVariable Long id,
                              @Validated @RequestBody Measurement m) {

        Measurement existing = service.get(id)
                .orElseThrow(() -> new MeasurementNotFoundException(id));

        // kopioi päivitettävät kentät:
        existing.setHeightCm(m.getHeightCm());
        existing.setWeightKg(m.getWeightKg());
        existing.setMeasuredAt(m.getMeasuredAt());
        existing.setPerson(m.getPerson());      // tai pelkkä person-id DTO:sta riippuen

        return service.save(existing);
    }

    /* ------- DELETE ------- */

    @DeleteMapping("/{id}")            // DELETE /api/measurements/55
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }

    /* ------- 404 Poikkeus ------- */

    @ResponseStatus(HttpStatus.NOT_FOUND)
    private static class MeasurementNotFoundException extends RuntimeException {
        MeasurementNotFoundException(Long id) {
            super("Measurement not found: " + id);
        }
    }
}
