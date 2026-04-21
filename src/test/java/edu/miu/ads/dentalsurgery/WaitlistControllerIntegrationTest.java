package edu.miu.ads.dentalsurgery;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.miu.ads.dentalsurgery.dto.AppointmentCancelRequest;
import edu.miu.ads.dentalsurgery.dto.AppointmentRequest;
import edu.miu.ads.dentalsurgery.dto.DentistRequest;
import edu.miu.ads.dentalsurgery.dto.PatientRequest;
import edu.miu.ads.dentalsurgery.dto.TreatmentRequest;
import edu.miu.ads.dentalsurgery.dto.WaitlistEntryCreateRequest;
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

import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class WaitlistControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void waitlistEndpointsShouldRejectAnonymousRequests() throws Exception {
        mockMvc.perform(get("/api/v1/waitlist"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void createWaitlistEntryAndListShouldWork() throws Exception {
        long patientId = createPatient("Ada", "Lovelace");
        long treatmentId = createTreatment("WL-TREAT-1", "Cleaning", 30);
        LocalDateTime preferredStart = LocalDateTime.now().plusDays(1).withSecond(0).withNano(0);
        LocalDateTime preferredEnd = preferredStart.plusHours(2);

        WaitlistEntryCreateRequest request = new WaitlistEntryCreateRequest(
                patientId,
                treatmentId,
                preferredStart,
                preferredEnd,
                8,
                "Patient can come quickly");

        mockMvc.perform(post("/api/v1/waitlist")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.patientId").value(patientId))
                .andExpect(jsonPath("$.treatmentId").value(treatmentId))
                .andExpect(jsonPath("$.priority").value(8))
                .andExpect(jsonPath("$.active").value(true));

        mockMvc.perform(get("/api/v1/waitlist"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].patientId", hasItem((int) patientId)));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void suggestionsForCancelledAppointmentShouldReturnRankedCandidates() throws Exception {
        long dentistId = createDentist();
        long fittingTreatmentId = createTreatment("WL-TREAT-MATCH", "Root Canal", 60);
        long nonFittingTreatmentId = createTreatment("WL-TREAT-OTHER", "Whitening", 45);
        long cancelledAppointmentId = createCancelledAppointment(dentistId, fittingTreatmentId);

        long firstPatientId = createPatient("Grace", "Hopper");
        long secondPatientId = createPatient("Alan", "Turing");

        LocalDateTime cancelledStart = readAppointmentStart(cancelledAppointmentId);

        WaitlistEntryCreateRequest topCandidate = new WaitlistEntryCreateRequest(
                firstPatientId,
                fittingTreatmentId,
                cancelledStart.minusMinutes(30),
                cancelledStart.plusMinutes(30),
                9,
                "Exact fit and high priority");

        WaitlistEntryCreateRequest lowerCandidate = new WaitlistEntryCreateRequest(
                secondPatientId,
                nonFittingTreatmentId,
                cancelledStart.plusHours(5),
                cancelledStart.plusHours(7),
                4,
                "Different treatment and weak time match");

        mockMvc.perform(post("/api/v1/waitlist")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(topCandidate)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/v1/waitlist")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(lowerCandidate)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/v1/waitlist/suggestions")
                        .param("appointmentId", String.valueOf(cancelledAppointmentId))
                        .param("limit", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].patientId").value(firstPatientId))
                .andExpect(jsonPath("$[0].treatmentFitScore").value(50))
                .andExpect(jsonPath("$[0].timeWindowFitScore").value(30))
                .andExpect(jsonPath("$[0].priorityScore").value(18))
                .andExpect(jsonPath("$[0].score").value(98))
                .andExpect(jsonPath("$[0].explanation").exists())
                .andExpect(jsonPath("$[1].patientId").value(secondPatientId))
                .andExpect(jsonPath("$[1].score").value(8));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void suggestionsForScheduledAppointmentShouldBeRejected() throws Exception {
        long dentistId = createDentist();
        long treatmentId = createTreatment("WL-TREAT-SCHED", "Extraction", 30);
        long appointmentId = createScheduledAppointment(dentistId, treatmentId);

        mockMvc.perform(get("/api/v1/waitlist/suggestions")
                        .param("appointmentId", String.valueOf(appointmentId)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void deactivatedWaitlistEntryShouldNotAppearInSuggestions() throws Exception {
        long dentistId = createDentist();
        long treatmentId = createTreatment("WL-TREAT-DEACT", "Filling", 30);
        long cancelledAppointmentId = createCancelledAppointment(dentistId, treatmentId);

        long activePatientId = createPatient("Katherine", "Johnson");
        long toDeactivatePatientId = createPatient("Dorothy", "Vaughan");
        LocalDateTime cancelledStart = readAppointmentStart(cancelledAppointmentId);

        WaitlistEntryCreateRequest activeRequest = new WaitlistEntryCreateRequest(
                activePatientId,
                treatmentId,
                cancelledStart.minusMinutes(15),
                cancelledStart.plusMinutes(15),
                8,
                "Still active");

        WaitlistEntryCreateRequest deactivateRequest = new WaitlistEntryCreateRequest(
                toDeactivatePatientId,
                treatmentId,
                cancelledStart.minusMinutes(15),
                cancelledStart.plusMinutes(15),
                9,
                "Will be deactivated");

        mockMvc.perform(post("/api/v1/waitlist")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(activeRequest)))
                .andExpect(status().isCreated());

        MvcResult deactivateCreateResult = mockMvc.perform(post("/api/v1/waitlist")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(deactivateRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        long deactivatedEntryId = objectMapper.readTree(deactivateCreateResult.getResponse().getContentAsString()).get("id").asLong();

        mockMvc.perform(put("/api/v1/waitlist/{id}/deactivate", deactivatedEntryId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(false));

        mockMvc.perform(get("/api/v1/waitlist/suggestions")
                        .param("appointmentId", String.valueOf(cancelledAppointmentId))
                        .param("limit", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].patientId", hasItem((int) activePatientId)))
                .andExpect(jsonPath("$[*].patientId", org.hamcrest.Matchers.not(hasItem((int) toDeactivatePatientId))));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void invalidPreferredWindowShouldBeRejected() throws Exception {
        long patientId = createPatient("Rosalind", "Franklin");
        long treatmentId = createTreatment("WL-TREAT-WINDOW", "Cleaning", 30);
        LocalDateTime start = LocalDateTime.now().plusDays(2).withSecond(0).withNano(0);
        LocalDateTime endBeforeStart = start.minusHours(1);

        WaitlistEntryCreateRequest request = new WaitlistEntryCreateRequest(
                patientId,
                treatmentId,
                start,
                endBeforeStart,
                6,
                "Invalid window");

        mockMvc.perform(post("/api/v1/waitlist")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    private long createScheduledAppointment(long dentistId, long treatmentId) throws Exception {
        long patientId = createPatient("Marie", "Curie");
        LocalDateTime startAt = LocalDateTime.now().plusDays(3).withSecond(0).withNano(0);

        AppointmentRequest request = new AppointmentRequest(patientId, dentistId, treatmentId, startAt);
        MvcResult result = mockMvc.perform(post("/api/v1/appointments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        return objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();
    }

    private long createCancelledAppointment(long dentistId, long treatmentId) throws Exception {
        long appointmentId = createScheduledAppointment(dentistId, treatmentId);
        AppointmentCancelRequest cancelRequest = new AppointmentCancelRequest("Patient unavailable");

        mockMvc.perform(put("/api/v1/appointments/{id}/cancel", appointmentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cancelRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));

        return appointmentId;
    }

    private LocalDateTime readAppointmentStart(long appointmentId) throws Exception {
        MvcResult result = mockMvc.perform(get("/api/v1/appointments/{id}", appointmentId))
                .andExpect(status().isOk())
                .andReturn();
        return LocalDateTime.parse(objectMapper.readTree(result.getResponse().getContentAsString()).get("startAt").asText());
    }

    private long createPatient(String firstName, String lastName) throws Exception {
        String suffix = String.valueOf(System.nanoTime());
        PatientRequest request = new PatientRequest(firstName, lastName, "+1516" + suffix.substring(suffix.length() - 7), firstName.toLowerCase() + "." + suffix + "@example.com");
        MvcResult result = mockMvc.perform(post("/api/v1/patients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();
    }

    private long createDentist() throws Exception {
        String suffix = String.valueOf(System.nanoTime());
        DentistRequest request = new DentistRequest("Nina", "Shaw", "General Dentistry", "LIC-WL-" + suffix);
        MvcResult result = mockMvc.perform(post("/api/v1/dentists")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();
    }

    private long createTreatment(String codePrefix, String name, int durationMinutes) throws Exception {
        String suffix = String.valueOf(System.nanoTime());
        TreatmentRequest request = new TreatmentRequest(
                codePrefix + "-" + suffix,
                name,
                name + " procedure",
                new BigDecimal("120.00"),
                durationMinutes,
                true);
        MvcResult result = mockMvc.perform(post("/api/v1/treatments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();
    }
}