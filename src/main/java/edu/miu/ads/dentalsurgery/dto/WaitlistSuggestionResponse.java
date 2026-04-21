package edu.miu.ads.dentalsurgery.dto;

import java.time.LocalDateTime;

public record WaitlistSuggestionResponse(
        Long waitlistEntryId,
        Long patientId,
        String patientName,
        Long requestedTreatmentId,
        String requestedTreatmentCode,
        String requestedTreatmentName,
        Integer priority,
        LocalDateTime preferredStartAt,
        LocalDateTime preferredEndAt,
        int score,
        int treatmentFitScore,
        int timeWindowFitScore,
        int priorityScore,
        String explanation) {
}