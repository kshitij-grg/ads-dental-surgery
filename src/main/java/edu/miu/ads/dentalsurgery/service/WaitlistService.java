package edu.miu.ads.dentalsurgery.service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;

import edu.miu.ads.dentalsurgery.domain.Appointment;
import edu.miu.ads.dentalsurgery.domain.AppointmentStatus;
import edu.miu.ads.dentalsurgery.domain.Patient;
import edu.miu.ads.dentalsurgery.domain.Treatment;
import edu.miu.ads.dentalsurgery.domain.WaitlistEntry;
import edu.miu.ads.dentalsurgery.dto.WaitlistEntryCreateRequest;
import edu.miu.ads.dentalsurgery.dto.WaitlistEntryResponse;
import edu.miu.ads.dentalsurgery.dto.WaitlistSuggestionResponse;
import edu.miu.ads.dentalsurgery.exception.ResourceNotFoundException;
import edu.miu.ads.dentalsurgery.mapper.WaitlistMapper;
import edu.miu.ads.dentalsurgery.repository.AppointmentRepository;
import edu.miu.ads.dentalsurgery.repository.PatientRepository;
import edu.miu.ads.dentalsurgery.repository.TreatmentRepository;
import edu.miu.ads.dentalsurgery.repository.WaitlistEntryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class WaitlistService {

    private final WaitlistEntryRepository waitlistEntryRepository;
    private final PatientRepository patientRepository;
    private final TreatmentRepository treatmentRepository;
    private final AppointmentRepository appointmentRepository;
    private final WaitlistMapper waitlistMapper;

    public WaitlistService(
            WaitlistEntryRepository waitlistEntryRepository,
            PatientRepository patientRepository,
            TreatmentRepository treatmentRepository,
            AppointmentRepository appointmentRepository,
            WaitlistMapper waitlistMapper) {
        this.waitlistEntryRepository = waitlistEntryRepository;
        this.patientRepository = patientRepository;
        this.treatmentRepository = treatmentRepository;
        this.appointmentRepository = appointmentRepository;
        this.waitlistMapper = waitlistMapper;
    }

    @Transactional(readOnly = true)
    public List<WaitlistEntryResponse> findAll() {
        return waitlistEntryRepository.findAllDetailedOrderByPriority()
                .stream()
                .map(waitlistMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public WaitlistEntryResponse findById(Long id) {
        return waitlistEntryRepository.findDetailedById(id)
                .map(waitlistMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Waitlist entry not found: " + id));
    }

    public WaitlistEntryResponse create(WaitlistEntryCreateRequest request) {
        validatePreferredWindow(request.preferredStartAt(), request.preferredEndAt());

        Patient patient = patientRepository.findById(request.patientId())
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found: " + request.patientId()));
        Treatment treatment = treatmentRepository.findById(request.treatmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Treatment not found: " + request.treatmentId()));

        WaitlistEntry entry = new WaitlistEntry();
        entry.setPatient(patient);
        entry.setTreatment(treatment);
        entry.setPreferredStartAt(request.preferredStartAt());
        entry.setPreferredEndAt(request.preferredEndAt());
        entry.setPriority(request.priority());
        entry.setNotes(request.notes());
        entry.setActive(true);

        return waitlistMapper.toResponse(waitlistEntryRepository.save(entry));
    }

    public WaitlistEntryResponse deactivate(Long id) {
        WaitlistEntry entry = waitlistEntryRepository.findDetailedById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Waitlist entry not found: " + id));

        entry.setActive(false);
        return waitlistMapper.toResponse(waitlistEntryRepository.save(entry));
    }

    @Transactional(readOnly = true)
    public List<WaitlistSuggestionResponse> suggestForCancelledAppointment(Long appointmentId, int limit) {
        Appointment appointment = appointmentRepository.findDetailedById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found: " + appointmentId));

        if (appointment.getStatus() != AppointmentStatus.CANCELLED) {
            throw new IllegalArgumentException("Suggestions are only available for cancelled appointments");
        }

        return waitlistEntryRepository.findAllActiveDetailed()
                .stream()
                .map(entry -> buildSuggestion(entry, appointment))
                .sorted(Comparator
                        .comparingInt(WaitlistSuggestionResponse::score)
                        .reversed()
                        .thenComparing(WaitlistSuggestionResponse::priority, Comparator.reverseOrder()))
                .limit(limit)
                .toList();
    }

    private WaitlistSuggestionResponse buildSuggestion(WaitlistEntry entry, Appointment appointment) {
        int treatmentFitScore = calculateTreatmentFitScore(entry, appointment);
        int timeWindowFitScore = calculateTimeWindowFitScore(entry, appointment.getStartAt());
        int priorityScore = calculatePriorityScore(entry.getPriority());
        int total = treatmentFitScore + timeWindowFitScore + priorityScore;

        String explanation = "Treatment fit=" + treatmentFitScore
                + ", time-window fit=" + timeWindowFitScore
                + ", priority=" + priorityScore;

        return new WaitlistSuggestionResponse(
                entry.getId(),
                entry.getPatient().getId(),
                entry.getPatient().getFirstName() + " " + entry.getPatient().getLastName(),
                entry.getTreatment().getId(),
                entry.getTreatment().getCode(),
                entry.getTreatment().getName(),
                entry.getPriority(),
                entry.getPreferredStartAt(),
                entry.getPreferredEndAt(),
                total,
                treatmentFitScore,
                timeWindowFitScore,
                priorityScore,
                explanation);
    }

    private int calculateTreatmentFitScore(WaitlistEntry entry, Appointment appointment) {
        return entry.getTreatment().getId().equals(appointment.getTreatment().getId()) ? 50 : 0;
    }

    private int calculateTimeWindowFitScore(WaitlistEntry entry, LocalDateTime cancelledStartAt) {
        LocalDateTime preferredStart = entry.getPreferredStartAt();
        LocalDateTime preferredEnd = entry.getPreferredEndAt();

        if (preferredStart == null || preferredEnd == null) {
            return 10;
        }
        if (!cancelledStartAt.isBefore(preferredStart) && !cancelledStartAt.isAfter(preferredEnd)) {
            return 30;
        }

        long deltaToStart = Math.abs(ChronoUnit.MINUTES.between(cancelledStartAt, preferredStart));
        long deltaToEnd = Math.abs(ChronoUnit.MINUTES.between(cancelledStartAt, preferredEnd));
        long minDelta = Math.min(deltaToStart, deltaToEnd);

        if (minDelta <= 60) {
            return 20;
        }
        if (minDelta <= 180) {
            return 10;
        }
        return 0;
    }

    private int calculatePriorityScore(Integer priority) {
        int clamped = Math.max(1, Math.min(10, priority));
        return clamped * 2;
    }

    private void validatePreferredWindow(LocalDateTime preferredStartAt, LocalDateTime preferredEndAt) {
        if (preferredStartAt != null && preferredEndAt != null && preferredEndAt.isBefore(preferredStartAt)) {
            throw new IllegalArgumentException("preferredEndAt must be after or equal to preferredStartAt");
        }
    }
}