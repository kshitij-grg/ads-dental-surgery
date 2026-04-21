package edu.miu.ads.dentalsurgery.mapper;

import edu.miu.ads.dentalsurgery.domain.Appointment;
import edu.miu.ads.dentalsurgery.dto.AppointmentResponse;
import org.springframework.stereotype.Component;

@Component
public class AppointmentMapper {

    public AppointmentResponse toResponse(Appointment appointment) {
        return new AppointmentResponse(
                appointment.getId(),
                appointment.getPatient().getId(),
                appointment.getPatient().getFirstName() + " " + appointment.getPatient().getLastName(),
                appointment.getDentist().getId(),
                appointment.getDentist().getFirstName() + " " + appointment.getDentist().getLastName(),
                appointment.getTreatment().getId(),
                appointment.getTreatment().getCode(),
                appointment.getTreatment().getName(),
                appointment.getStartAt(),
                appointment.getEndAt(),
                appointment.getStatus().name(),
                appointment.getCancellationReason(),
                appointment.getCreatedAt(),
                appointment.getUpdatedAt());
    }
}
