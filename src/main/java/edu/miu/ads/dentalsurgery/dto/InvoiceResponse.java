package edu.miu.ads.dentalsurgery.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;

public record InvoiceResponse(
        Long id,
        Long appointmentId,
        Long patientId,
        String patientName,
        Long dentistId,
        String dentistName,
        Long treatmentId,
        String treatmentCode,
        String treatmentName,
        LocalDateTime appointmentStartAt,
        LocalDateTime appointmentEndAt,
        BigDecimal treatmentCost,
        BigDecimal taxAmount,
        BigDecimal discountAmount,
        BigDecimal totalAmount,
        String status,
        String paymentReference,
        Instant paidAt,
        String notes,
        Instant createdAt,
        Instant updatedAt) {
}
