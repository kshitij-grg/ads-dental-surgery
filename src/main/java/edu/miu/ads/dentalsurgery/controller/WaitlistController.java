package edu.miu.ads.dentalsurgery.controller;

import java.util.List;

import edu.miu.ads.dentalsurgery.dto.WaitlistEntryCreateRequest;
import edu.miu.ads.dentalsurgery.dto.WaitlistEntryResponse;
import edu.miu.ads.dentalsurgery.dto.WaitlistSuggestionResponse;
import edu.miu.ads.dentalsurgery.service.WaitlistService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated
@RequestMapping("/api/v1/waitlist")
public class WaitlistController {

    private final WaitlistService waitlistService;

    public WaitlistController(WaitlistService waitlistService) {
        this.waitlistService = waitlistService;
    }

    @GetMapping
    public ResponseEntity<List<WaitlistEntryResponse>> findAll() {
        return ResponseEntity.ok(waitlistService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<WaitlistEntryResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(waitlistService.findById(id));
    }

    @PostMapping
    public ResponseEntity<WaitlistEntryResponse> create(@Valid @RequestBody WaitlistEntryCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(waitlistService.create(request));
    }

    @PutMapping("/{id}/deactivate")
    public ResponseEntity<WaitlistEntryResponse> deactivate(@PathVariable Long id) {
        return ResponseEntity.ok(waitlistService.deactivate(id));
    }

    @GetMapping("/suggestions")
    public ResponseEntity<List<WaitlistSuggestionResponse>> suggestForCancelledAppointment(
            @RequestParam Long appointmentId,
            @RequestParam(defaultValue = "5") @Min(1) @Max(20) Integer limit) {
        return ResponseEntity.ok(waitlistService.suggestForCancelledAppointment(appointmentId, limit));
    }
}