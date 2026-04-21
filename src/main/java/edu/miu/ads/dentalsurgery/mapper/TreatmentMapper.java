package edu.miu.ads.dentalsurgery.mapper;

import edu.miu.ads.dentalsurgery.domain.Treatment;
import edu.miu.ads.dentalsurgery.dto.TreatmentRequest;
import edu.miu.ads.dentalsurgery.dto.TreatmentResponse;
import org.springframework.stereotype.Component;

@Component
public class TreatmentMapper {

    public Treatment toEntity(TreatmentRequest request) {
        Treatment treatment = new Treatment();
        updateEntity(treatment, request);
        return treatment;
    }

    public void updateEntity(Treatment treatment, TreatmentRequest request) {
        treatment.setCode(request.code());
        treatment.setName(request.name());
        treatment.setDescription(request.description());
        treatment.setBaseCost(request.baseCost());
        treatment.setDurationMinutes(request.durationMinutes());
        treatment.setActive(request.active());
    }

    public TreatmentResponse toResponse(Treatment treatment) {
        return new TreatmentResponse(
                treatment.getId(),
                treatment.getCode(),
                treatment.getName(),
                treatment.getDescription(),
                treatment.getBaseCost(),
                treatment.getDurationMinutes(),
                treatment.isActive(),
                treatment.getCreatedAt(),
                treatment.getUpdatedAt());
    }
}
