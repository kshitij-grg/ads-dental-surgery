package edu.miu.ads.dentalsurgery.controller;

import java.util.List;

import edu.miu.ads.dentalsurgery.dto.AppointmentCancelRequest;
import edu.miu.ads.dentalsurgery.dto.AppointmentRequest;
import edu.miu.ads.dentalsurgery.dto.AppointmentRescheduleRequest;
import edu.miu.ads.dentalsurgery.dto.AppointmentResponse;
import edu.miu.ads.dentalsurgery.service.AppointmentService;
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
@RequestMapping("/api/v1/appointments")
public class AppointmentController {

    private final AppointmentService appointmentService;

    public AppointmentController(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    @GetMapping
    public ResponseEntity<List<AppointmentResponse>> findAll() {
        return ResponseEntity.ok(appointmentService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<AppointmentResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(appointmentService.findById(id));
    }

    @PostMapping
    public ResponseEntity<AppointmentResponse> create(@Valid @RequestBody AppointmentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(appointmentService.create(request));
    }

    @PutMapping("/{id}/reschedule")
    public ResponseEntity<AppointmentResponse> reschedule(@PathVariable Long id, @Valid @RequestBody AppointmentRescheduleRequest request) {
        return ResponseEntity.ok(appointmentService.reschedule(id, request));
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<AppointmentResponse> cancel(@PathVariable Long id, @Valid @RequestBody AppointmentCancelRequest request) {
        return ResponseEntity.ok(appointmentService.cancel(id, request));
    }

    @PutMapping("/{id}/complete")
    public ResponseEntity<AppointmentResponse> complete(@PathVariable Long id) {
        return ResponseEntity.ok(appointmentService.complete(id));
    }
}
