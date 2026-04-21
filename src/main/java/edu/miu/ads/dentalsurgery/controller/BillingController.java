package edu.miu.ads.dentalsurgery.controller;

import java.util.List;

import edu.miu.ads.dentalsurgery.dto.InvoiceCreateRequest;
import edu.miu.ads.dentalsurgery.dto.InvoiceMarkPaidRequest;
import edu.miu.ads.dentalsurgery.dto.InvoiceResponse;
import edu.miu.ads.dentalsurgery.service.BillingService;
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
@RequestMapping("/api/v1/billing/invoices")
public class BillingController {

    private final BillingService billingService;

    public BillingController(BillingService billingService) {
        this.billingService = billingService;
    }

    @GetMapping
    public ResponseEntity<List<InvoiceResponse>> findAll() {
        return ResponseEntity.ok(billingService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<InvoiceResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(billingService.findById(id));
    }

    @PostMapping
    public ResponseEntity<InvoiceResponse> create(@Valid @RequestBody InvoiceCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(billingService.createInvoice(request));
    }

    @PutMapping("/{id}/mark-paid")
    public ResponseEntity<InvoiceResponse> markPaid(@PathVariable Long id, @Valid @RequestBody InvoiceMarkPaidRequest request) {
        return ResponseEntity.ok(billingService.markInvoicePaid(id, request));
    }

    @PutMapping("/{id}/void")
    public ResponseEntity<InvoiceResponse> voidInvoice(@PathVariable Long id) {
        return ResponseEntity.ok(billingService.voidInvoice(id));
    }
}
