package edu.miu.ads.dentalsurgery;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.miu.ads.dentalsurgery.dto.AppointmentRequest;
import edu.miu.ads.dentalsurgery.dto.DentistRequest;
import edu.miu.ads.dentalsurgery.dto.InvoiceCreateRequest;
import edu.miu.ads.dentalsurgery.dto.InvoiceMarkPaidRequest;
import edu.miu.ads.dentalsurgery.dto.PatientRequest;
import edu.miu.ads.dentalsurgery.dto.TreatmentRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class BillingControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void billingEndpointsShouldRejectAnonymousRequests() throws Exception {
        mockMvc.perform(get("/api/v1/billing/invoices"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void createAndMarkPaidInvoiceShouldWork() throws Exception {
        long completedAppointmentId = createCompletedAppointment();

        InvoiceCreateRequest createRequest = new InvoiceCreateRequest(
                completedAppointmentId,
                new BigDecimal("10.00"),
                new BigDecimal("5.00"),
                "Discount approved by reception");

        MvcResult createResult = mockMvc.perform(post("/api/v1/billing/invoices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("UNPAID"))
                .andExpect(jsonPath("$.treatmentCost").value(75.00))
                .andExpect(jsonPath("$.taxAmount").value(10.00))
                .andExpect(jsonPath("$.discountAmount").value(5.00))
                .andExpect(jsonPath("$.totalAmount").value(80.00))
                .andReturn();

        long invoiceId = objectMapper.readTree(createResult.getResponse().getContentAsString()).get("id").asLong();

        InvoiceMarkPaidRequest markPaidRequest = new InvoiceMarkPaidRequest("PAY-REF-123");
        mockMvc.perform(put("/api/v1/billing/invoices/{id}/mark-paid", invoiceId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(markPaidRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PAID"))
                .andExpect(jsonPath("$.paymentReference").value("PAY-REF-123"));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void duplicateInvoiceForSameAppointmentShouldBeRejected() throws Exception {
        long completedAppointmentId = createCompletedAppointment();

        InvoiceCreateRequest createRequest = new InvoiceCreateRequest(
                completedAppointmentId,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                null);

        mockMvc.perform(post("/api/v1/billing/invoices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/v1/billing/invoices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void scheduledAppointmentShouldNotBeBillable() throws Exception {
        long scheduledAppointmentId = createScheduledAppointment();

        InvoiceCreateRequest createRequest = new InvoiceCreateRequest(
                scheduledAppointmentId,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                null);

        mockMvc.perform(post("/api/v1/billing/invoices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void negativeInvoiceTotalShouldBeRejected() throws Exception {
        long completedAppointmentId = createCompletedAppointment();

        InvoiceCreateRequest createRequest = new InvoiceCreateRequest(
                completedAppointmentId,
                BigDecimal.ZERO,
                new BigDecimal("1000.00"),
                "Discount exceeds treatment cost");

        mockMvc.perform(post("/api/v1/billing/invoices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void paidInvoiceCannotBeMarkedPaidAgainOrVoided() throws Exception {
        long completedAppointmentId = createCompletedAppointment();

        InvoiceCreateRequest createRequest = new InvoiceCreateRequest(
                completedAppointmentId,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                null);

        MvcResult createResult = mockMvc.perform(post("/api/v1/billing/invoices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        long invoiceId = objectMapper.readTree(createResult.getResponse().getContentAsString()).get("id").asLong();

        InvoiceMarkPaidRequest firstPayment = new InvoiceMarkPaidRequest("PAY-ONCE-001");
        mockMvc.perform(put("/api/v1/billing/invoices/{id}/mark-paid", invoiceId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(firstPayment)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PAID"));

        InvoiceMarkPaidRequest secondPayment = new InvoiceMarkPaidRequest("PAY-TWICE-002");
        mockMvc.perform(put("/api/v1/billing/invoices/{id}/mark-paid", invoiceId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(secondPayment)))
                .andExpect(status().isBadRequest());

        mockMvc.perform(put("/api/v1/billing/invoices/{id}/void", invoiceId))
                .andExpect(status().isBadRequest());
    }

    private long createScheduledAppointment() throws Exception {
        long patientId = createPatient();
        long dentistId = createDentist();
        long treatmentId = createTreatment();
        LocalDateTime startAt = LocalDateTime.now().plusDays(4).withSecond(0).withNano(0);

        AppointmentRequest request = new AppointmentRequest(patientId, dentistId, treatmentId, startAt);
        MvcResult result = mockMvc.perform(post("/api/v1/appointments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();
    }

    private long createCompletedAppointment() throws Exception {
        long appointmentId = createScheduledAppointment();
        mockMvc.perform(put("/api/v1/appointments/{id}/complete", appointmentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"));
        return appointmentId;
    }

    private long createPatient() throws Exception {
        String suffix = String.valueOf(System.nanoTime());
        PatientRequest request = new PatientRequest("Liam", "Brown", "+1515000" + suffix.substring(suffix.length() - 6), "liam." + suffix + "@example.com");
        MvcResult result = mockMvc.perform(post("/api/v1/patients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();
    }

    private long createDentist() throws Exception {
        String suffix = String.valueOf(System.nanoTime());
        DentistRequest request = new DentistRequest("Noah", "Miller", "General Dentistry", "LIC-BILL-" + suffix);
        MvcResult result = mockMvc.perform(post("/api/v1/dentists")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();
    }

    private long createTreatment() throws Exception {
        String suffix = String.valueOf(System.nanoTime());
        TreatmentRequest request = new TreatmentRequest(
                "BILL-TREAT-" + suffix,
                "Cleaning",
                "Deep cleaning",
                new BigDecimal("75.00"),
                30,
                true);
        MvcResult result = mockMvc.perform(post("/api/v1/treatments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();
    }
}
