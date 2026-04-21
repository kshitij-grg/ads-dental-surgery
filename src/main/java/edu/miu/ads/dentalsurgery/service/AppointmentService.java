package edu.miu.ads.dentalsurgery.service;

import java.time.LocalDateTime;
import java.util.List;

import edu.miu.ads.dentalsurgery.domain.Appointment;
import edu.miu.ads.dentalsurgery.domain.AppointmentStatus;
import edu.miu.ads.dentalsurgery.domain.Dentist;
import edu.miu.ads.dentalsurgery.domain.Patient;
import edu.miu.ads.dentalsurgery.domain.Treatment;
import edu.miu.ads.dentalsurgery.dto.AppointmentCancelRequest;
import edu.miu.ads.dentalsurgery.dto.AppointmentRequest;
import edu.miu.ads.dentalsurgery.dto.AppointmentRescheduleRequest;
import edu.miu.ads.dentalsurgery.dto.AppointmentResponse;
import edu.miu.ads.dentalsurgery.exception.ResourceNotFoundException;
import edu.miu.ads.dentalsurgery.mapper.AppointmentMapper;
import edu.miu.ads.dentalsurgery.repository.AppointmentRepository;
import edu.miu.ads.dentalsurgery.repository.DentistRepository;
import edu.miu.ads.dentalsurgery.repository.PatientRepository;
import edu.miu.ads.dentalsurgery.repository.TreatmentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final PatientRepository patientRepository;
    private final DentistRepository dentistRepository;
    private final TreatmentRepository treatmentRepository;
    private final AppointmentMapper appointmentMapper;

    public AppointmentService(
            AppointmentRepository appointmentRepository,
            PatientRepository patientRepository,
            DentistRepository dentistRepository,
            TreatmentRepository treatmentRepository,
            AppointmentMapper appointmentMapper) {
        this.appointmentRepository = appointmentRepository;
        this.patientRepository = patientRepository;
        this.dentistRepository = dentistRepository;
        this.treatmentRepository = treatmentRepository;
        this.appointmentMapper = appointmentMapper;
    }

    @Transactional(readOnly = true)
    public List<AppointmentResponse> findAll() {
        return appointmentRepository.findAllByOrderByStartAtAsc()
                .stream()
                .map(appointmentMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public AppointmentResponse findById(Long id) {
        return appointmentRepository.findDetailedById(id)
                .map(appointmentMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found: " + id));
    }

    public AppointmentResponse create(AppointmentRequest request) {
        Patient patient = patientRepository.findById(request.patientId())
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found: " + request.patientId()));
        Dentist dentist = dentistRepository.findById(request.dentistId())
                .orElseThrow(() -> new ResourceNotFoundException("Dentist not found: " + request.dentistId()));
        Treatment treatment = treatmentRepository.findById(request.treatmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Treatment not found: " + request.treatmentId()));

        LocalDateTime endAt = request.startAt().plusMinutes(treatment.getDurationMinutes());
        ensureNoScheduleConflict(dentist.getId(), request.startAt(), endAt, null);

        Appointment appointment = new Appointment();
        appointment.setPatient(patient);
        appointment.setDentist(dentist);
        appointment.setTreatment(treatment);
        appointment.setStartAt(request.startAt());
        appointment.setEndAt(endAt);
        appointment.setStatus(AppointmentStatus.SCHEDULED);

        return appointmentMapper.toResponse(appointmentRepository.save(appointment));
    }

    public AppointmentResponse reschedule(Long id, AppointmentRescheduleRequest request) {
        Appointment appointment = loadAppointment(id);
        ensureLifecycleStatus(appointment, AppointmentStatus.SCHEDULED, "Only scheduled appointments can be rescheduled");

        LocalDateTime newEndAt = request.startAt().plusMinutes(appointment.getTreatment().getDurationMinutes());
        ensureNoScheduleConflict(appointment.getDentist().getId(), request.startAt(), newEndAt, appointment.getId());

        appointment.setStartAt(request.startAt());
        appointment.setEndAt(newEndAt);
        return appointmentMapper.toResponse(appointmentRepository.save(appointment));
    }

    public AppointmentResponse cancel(Long id, AppointmentCancelRequest request) {
        Appointment appointment = loadAppointment(id);
        ensureLifecycleStatus(appointment, AppointmentStatus.SCHEDULED, "Only scheduled appointments can be cancelled");

        appointment.setStatus(AppointmentStatus.CANCELLED);
        appointment.setCancellationReason(request.cancellationReason());
        return appointmentMapper.toResponse(appointmentRepository.save(appointment));
    }

    public AppointmentResponse complete(Long id) {
        Appointment appointment = loadAppointment(id);
        ensureLifecycleStatus(appointment, AppointmentStatus.SCHEDULED, "Only scheduled appointments can be completed");

        appointment.setStatus(AppointmentStatus.COMPLETED);
        return appointmentMapper.toResponse(appointmentRepository.save(appointment));
    }

    private Appointment loadAppointment(Long id) {
        return appointmentRepository.findDetailedById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found: " + id));
    }

    private void ensureNoScheduleConflict(Long dentistId, LocalDateTime startAt, LocalDateTime endAt, Long appointmentId) {
        boolean conflict = appointmentRepository.existsOverlappingAppointment(dentistId, startAt, endAt, appointmentId);
        if (conflict) {
            throw new IllegalArgumentException("Dentist already has an overlapping appointment in that time window");
        }
    }

    private void ensureLifecycleStatus(Appointment appointment, AppointmentStatus expectedStatus, String message) {
        if (appointment.getStatus() != expectedStatus) {
            throw new IllegalArgumentException(message);
        }
    }
}
