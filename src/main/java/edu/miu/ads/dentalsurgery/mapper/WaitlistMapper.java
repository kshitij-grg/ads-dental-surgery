package edu.miu.ads.dentalsurgery.mapper;

import edu.miu.ads.dentalsurgery.domain.WaitlistEntry;
import edu.miu.ads.dentalsurgery.dto.WaitlistEntryResponse;
import org.springframework.stereotype.Component;

@Component
public class WaitlistMapper {

    public WaitlistEntryResponse toResponse(WaitlistEntry entry) {
        return new WaitlistEntryResponse(
                entry.getId(),
                entry.getPatient().getId(),
                entry.getPatient().getFirstName() + " " + entry.getPatient().getLastName(),
                entry.getTreatment().getId(),
                entry.getTreatment().getCode(),
                entry.getTreatment().getName(),
                entry.getPreferredStartAt(),
                entry.getPreferredEndAt(),
                entry.getPriority(),
                entry.getNotes(),
                entry.isActive(),
                entry.getCreatedAt(),
                entry.getUpdatedAt());
    }
}