package edu.miu.ads.dentalsurgery.controller;

import java.util.List;

import edu.miu.ads.dentalsurgery.dto.DentistRequest;
import edu.miu.ads.dentalsurgery.dto.DentistResponse;
import edu.miu.ads.dentalsurgery.service.DentistService;
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
@RequestMapping("/api/v1/dentists")
public class DentistController {

    private final DentistService dentistService;

    public DentistController(DentistService dentistService) {
        this.dentistService = dentistService;
    }

    @GetMapping
    public ResponseEntity<List<DentistResponse>> findAll() {
        return ResponseEntity.ok(dentistService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<DentistResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(dentistService.findById(id));
    }

    @PostMapping
    public ResponseEntity<DentistResponse> create(@Valid @RequestBody DentistRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(dentistService.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<DentistResponse> update(@PathVariable Long id, @Valid @RequestBody DentistRequest request) {
        return ResponseEntity.ok(dentistService.update(id, request));
    }
}
