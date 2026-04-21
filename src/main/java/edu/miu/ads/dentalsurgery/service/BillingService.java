package edu.miu.ads.dentalsurgery.service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import edu.miu.ads.dentalsurgery.domain.Appointment;
import edu.miu.ads.dentalsurgery.domain.AppointmentStatus;
import edu.miu.ads.dentalsurgery.domain.Invoice;
import edu.miu.ads.dentalsurgery.domain.InvoiceStatus;
import edu.miu.ads.dentalsurgery.dto.InvoiceCreateRequest;
import edu.miu.ads.dentalsurgery.dto.InvoiceMarkPaidRequest;
import edu.miu.ads.dentalsurgery.dto.InvoiceResponse;
import edu.miu.ads.dentalsurgery.exception.ResourceNotFoundException;
import edu.miu.ads.dentalsurgery.mapper.InvoiceMapper;
import edu.miu.ads.dentalsurgery.repository.AppointmentRepository;
import edu.miu.ads.dentalsurgery.repository.InvoiceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class BillingService {

    private final InvoiceRepository invoiceRepository;
    private final AppointmentRepository appointmentRepository;
    private final InvoiceMapper invoiceMapper;

    public BillingService(
            InvoiceRepository invoiceRepository,
            AppointmentRepository appointmentRepository,
            InvoiceMapper invoiceMapper) {
        this.invoiceRepository = invoiceRepository;
        this.appointmentRepository = appointmentRepository;
        this.invoiceMapper = invoiceMapper;
    }

    @Transactional(readOnly = true)
    public List<InvoiceResponse> findAll() {
        return invoiceRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(invoiceMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public InvoiceResponse findById(Long id) {
        return invoiceRepository.findDetailedById(id)
                .map(invoiceMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found: " + id));
    }

    public InvoiceResponse createInvoice(InvoiceCreateRequest request) {
        Appointment appointment = appointmentRepository.findDetailedById(request.appointmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found: " + request.appointmentId()));

        ensureAppointmentCanBeBilled(appointment);
        ensureInvoiceDoesNotExist(appointment.getId());

        BigDecimal treatmentCost = appointment.getTreatment().getBaseCost();
        BigDecimal totalAmount = treatmentCost
                .add(request.taxAmount())
                .subtract(request.discountAmount());
        if (totalAmount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Invoice total cannot be negative");
        }

        Invoice invoice = new Invoice();
        invoice.setAppointment(appointment);
        invoice.setTreatmentCost(treatmentCost);
        invoice.setTaxAmount(request.taxAmount());
        invoice.setDiscountAmount(request.discountAmount());
        invoice.setTotalAmount(totalAmount);
        invoice.setStatus(InvoiceStatus.UNPAID);
        invoice.setNotes(request.notes());

        return invoiceMapper.toResponse(invoiceRepository.save(invoice));
    }

    public InvoiceResponse markInvoicePaid(Long id, InvoiceMarkPaidRequest request) {
        Invoice invoice = invoiceRepository.findDetailedById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found: " + id));

        if (invoice.getStatus() == InvoiceStatus.VOID) {
            throw new IllegalArgumentException("Void invoices cannot be marked as paid");
        }
        if (invoice.getStatus() == InvoiceStatus.PAID) {
            throw new IllegalArgumentException("Invoice is already paid");
        }

        invoice.setStatus(InvoiceStatus.PAID);
        invoice.setPaymentReference(request.paymentReference());
        invoice.setPaidAt(Instant.now());
        return invoiceMapper.toResponse(invoiceRepository.save(invoice));
    }

    public InvoiceResponse voidInvoice(Long id) {
        Invoice invoice = invoiceRepository.findDetailedById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found: " + id));

        if (invoice.getStatus() == InvoiceStatus.PAID) {
            throw new IllegalArgumentException("Paid invoices cannot be voided");
        }
        if (invoice.getStatus() == InvoiceStatus.VOID) {
            throw new IllegalArgumentException("Invoice is already void");
        }

        invoice.setStatus(InvoiceStatus.VOID);
        return invoiceMapper.toResponse(invoiceRepository.save(invoice));
    }

    private void ensureAppointmentCanBeBilled(Appointment appointment) {
        if (appointment.getStatus() == AppointmentStatus.CANCELLED) {
            throw new IllegalArgumentException("Cancelled appointments cannot be billed");
        }
        if (appointment.getStatus() != AppointmentStatus.COMPLETED) {
            throw new IllegalArgumentException("Only completed appointments can be billed");
        }
    }

    private void ensureInvoiceDoesNotExist(Long appointmentId) {
        if (invoiceRepository.findByAppointmentId(appointmentId).isPresent()) {
            throw new IllegalArgumentException("Invoice already exists for appointment: " + appointmentId);
        }
    }
}
