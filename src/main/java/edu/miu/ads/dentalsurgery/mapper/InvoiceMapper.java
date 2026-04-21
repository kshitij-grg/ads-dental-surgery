package edu.miu.ads.dentalsurgery.mapper;

import edu.miu.ads.dentalsurgery.domain.Invoice;
import edu.miu.ads.dentalsurgery.dto.InvoiceResponse;
import org.springframework.stereotype.Component;

@Component
public class InvoiceMapper {

    public InvoiceResponse toResponse(Invoice invoice) {
        return new InvoiceResponse(
                invoice.getId(),
                invoice.getAppointment().getId(),
                invoice.getAppointment().getPatient().getId(),
                invoice.getAppointment().getPatient().getFirstName() + " " + invoice.getAppointment().getPatient().getLastName(),
                invoice.getAppointment().getDentist().getId(),
                invoice.getAppointment().getDentist().getFirstName() + " " + invoice.getAppointment().getDentist().getLastName(),
                invoice.getAppointment().getTreatment().getId(),
                invoice.getAppointment().getTreatment().getCode(),
                invoice.getAppointment().getTreatment().getName(),
                invoice.getAppointment().getStartAt(),
                invoice.getAppointment().getEndAt(),
                invoice.getTreatmentCost(),
                invoice.getTaxAmount(),
                invoice.getDiscountAmount(),
                invoice.getTotalAmount(),
                invoice.getStatus().name(),
                invoice.getPaymentReference(),
                invoice.getPaidAt(),
                invoice.getNotes(),
                invoice.getCreatedAt(),
                invoice.getUpdatedAt());
    }
}
