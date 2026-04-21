package edu.miu.ads.dentalsurgery;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.miu.ads.dentalsurgery.dto.AppointmentCancelRequest;
import edu.miu.ads.dentalsurgery.dto.AppointmentRequest;
import edu.miu.ads.dentalsurgery.dto.AppointmentRescheduleRequest;
import edu.miu.ads.dentalsurgery.dto.DentistRequest;
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
class AppointmentControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void appointmentEndpointsShouldRejectAnonymousRequests() throws Exception {
        mockMvc.perform(get("/api/v1/appointments"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void createRescheduleCancelAndCompleteAppointmentShouldWork() throws Exception {
        long patientId = createPatient();
        long dentistId = createDentist();
        long treatmentId = createTreatment();
        LocalDateTime startAt = LocalDateTime.now().plusDays(1).withSecond(0).withNano(0);

        AppointmentRequest createRequest = new AppointmentRequest(patientId, dentistId, treatmentId, startAt);
        MvcResult createResult = mockMvc.perform(post("/api/v1/appointments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("SCHEDULED"))
                .andReturn();

        JsonNode createBody = objectMapper.readTree(createResult.getResponse().getContentAsString());
        long appointmentId = createBody.get("id").asLong();

        AppointmentRescheduleRequest rescheduleRequest = new AppointmentRescheduleRequest(startAt.plusDays(1));
        mockMvc.perform(put("/api/v1/appointments/{id}/reschedule", appointmentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(rescheduleRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SCHEDULED"));

        AppointmentCancelRequest cancelRequest = new AppointmentCancelRequest("Patient requested a later date");
        mockMvc.perform(put("/api/v1/appointments/{id}/cancel", appointmentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cancelRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"))
                .andExpect(jsonPath("$.cancellationReason").value("Patient requested a later date"));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void overlappingAppointmentShouldBeRejected() throws Exception {
        long patientId = createPatient();
        long dentistId = createDentist();
        long firstTreatmentId = createTreatment();
        long secondTreatmentId = createTreatmentWithDifferentCode("TREAT-002");
        LocalDateTime startAt = LocalDateTime.now().plusDays(2).withSecond(0).withNano(0);

        AppointmentRequest firstRequest = new AppointmentRequest(patientId, dentistId, firstTreatmentId, startAt);
        mockMvc.perform(post("/api/v1/appointments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(firstRequest)))
                .andExpect(status().isCreated());

        AppointmentRequest overlappingRequest = new AppointmentRequest(patientId, dentistId, secondTreatmentId, startAt.plusMinutes(5));
        mockMvc.perform(post("/api/v1/appointments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(overlappingRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void completeAppointmentShouldWork() throws Exception {
        long patientId = createPatient();
        long dentistId = createDentist();
        long treatmentId = createTreatment();
        LocalDateTime startAt = LocalDateTime.now().plusDays(3).withSecond(0).withNano(0);

        AppointmentRequest createRequest = new AppointmentRequest(patientId, dentistId, treatmentId, startAt);
        MvcResult createResult = mockMvc.perform(post("/api/v1/appointments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        long appointmentId = objectMapper.readTree(createResult.getResponse().getContentAsString()).get("id").asLong();

        mockMvc.perform(put("/api/v1/appointments/{id}/complete", appointmentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void cancelledAppointmentCannotBeCompleted() throws Exception {
        long patientId = createPatient();
        long dentistId = createDentist();
        long treatmentId = createTreatment();
        LocalDateTime startAt = LocalDateTime.now().plusDays(5).withSecond(0).withNano(0);

        AppointmentRequest createRequest = new AppointmentRequest(patientId, dentistId, treatmentId, startAt);
        MvcResult createResult = mockMvc.perform(post("/api/v1/appointments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        long appointmentId = objectMapper.readTree(createResult.getResponse().getContentAsString()).get("id").asLong();

        AppointmentCancelRequest cancelRequest = new AppointmentCancelRequest("Patient unavailable");
        mockMvc.perform(put("/api/v1/appointments/{id}/cancel", appointmentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cancelRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));

        mockMvc.perform(put("/api/v1/appointments/{id}/complete", appointmentId))
                .andExpect(status().isBadRequest());
    }

    private long createPatient() throws Exception {
        PatientRequest request = new PatientRequest("Jane", "Doe", "+15151234567", "jane.doe@example.com");
        MvcResult result = mockMvc.perform(post("/api/v1/patients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();
    }

    private long createDentist() throws Exception {
                String uniqueSuffix = String.valueOf(System.nanoTime());
                DentistRequest request = new DentistRequest("Mia", "Stone", "Orthodontics", "LIC-APPT-" + uniqueSuffix);
        MvcResult result = mockMvc.perform(post("/api/v1/dentists")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();
    }

    private long createTreatment() throws Exception {
                return createTreatmentWithDifferentCode("TREAT-" + System.nanoTime());
    }

    private long createTreatmentWithDifferentCode(String code) throws Exception {
        TreatmentRequest request = new TreatmentRequest(code, "Checkup", "Routine checkup", new BigDecimal("75.00"), 30, true);
        MvcResult result = mockMvc.perform(post("/api/v1/treatments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();
    }
}
