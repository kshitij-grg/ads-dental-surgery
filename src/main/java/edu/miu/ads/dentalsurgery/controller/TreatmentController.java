package edu.miu.ads.dentalsurgery.controller;

import java.util.List;

import edu.miu.ads.dentalsurgery.dto.TreatmentRequest;
import edu.miu.ads.dentalsurgery.dto.TreatmentResponse;
import edu.miu.ads.dentalsurgery.service.TreatmentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/treatments")
public class TreatmentController {

    private final TreatmentService treatmentService;

    public TreatmentController(TreatmentService treatmentService) {
        this.treatmentService = treatmentService;
    }

    @GetMapping
    public ResponseEntity<List<TreatmentResponse>> findAll() {
        return ResponseEntity.ok(treatmentService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<TreatmentResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(treatmentService.findById(id));
    }

    @PostMapping
    public ResponseEntity<TreatmentResponse> create(@Valid @RequestBody TreatmentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(treatmentService.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TreatmentResponse> update(@PathVariable Long id, @Valid @RequestBody TreatmentRequest request) {
        return ResponseEntity.ok(treatmentService.update(id, request));
    }
}
