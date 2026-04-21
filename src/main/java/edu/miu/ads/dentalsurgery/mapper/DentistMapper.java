package edu.miu.ads.dentalsurgery.mapper;

import edu.miu.ads.dentalsurgery.domain.Dentist;
import edu.miu.ads.dentalsurgery.dto.DentistRequest;
import edu.miu.ads.dentalsurgery.dto.DentistResponse;
import org.springframework.stereotype.Component;

@Component
public class DentistMapper {

    public Dentist toEntity(DentistRequest request) {
        Dentist dentist = new Dentist();
        updateEntity(dentist, request);
        return dentist;
    }

    public void updateEntity(Dentist dentist, DentistRequest request) {
        dentist.setFirstName(request.firstName());
        dentist.setLastName(request.lastName());
        dentist.setSpecialty(request.specialty());
        dentist.setLicenseNumber(request.licenseNumber());
    }

    public DentistResponse toResponse(Dentist dentist) {
        return new DentistResponse(
                dentist.getId(),
                dentist.getFirstName(),
                dentist.getLastName(),
                dentist.getSpecialty(),
                dentist.getLicenseNumber(),
                dentist.getCreatedAt(),
                dentist.getUpdatedAt());
    }
}
