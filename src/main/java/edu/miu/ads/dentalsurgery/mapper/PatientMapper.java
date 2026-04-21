package edu.miu.ads.dentalsurgery.mapper;

import edu.miu.ads.dentalsurgery.domain.Patient;
import edu.miu.ads.dentalsurgery.dto.PatientRequest;
import edu.miu.ads.dentalsurgery.dto.PatientResponse;
import org.springframework.stereotype.Component;

@Component
public class PatientMapper {

    public Patient toEntity(PatientRequest request) {
        Patient patient = new Patient();
        updateEntity(patient, request);
        return patient;
    }

    public void updateEntity(Patient patient, PatientRequest request) {
        patient.setFirstName(request.firstName());
        patient.setLastName(request.lastName());
        patient.setPhone(request.phone());
        patient.setEmail(request.email());
    }

    public PatientResponse toResponse(Patient patient) {
        return new PatientResponse(
                patient.getId(),
                patient.getFirstName(),
                patient.getLastName(),
                patient.getPhone(),
                patient.getEmail(),
                patient.getCreatedAt(),
                patient.getUpdatedAt());
    }
}
