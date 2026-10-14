package com.medical.prescription.dto.response;

import com.medical.prescription.entity.PrescriptionEntry;

import java.math.BigDecimal;

public record PrescriptionEntryResponse(
        Long id,
        String entryName,
        BigDecimal quantity,
        String frequency,
        int durationDays,
        String instructions
) {
    public static PrescriptionEntryResponse from(PrescriptionEntry e) {
        return new PrescriptionEntryResponse(
                e.getId(),
                e.getEntryName(),
                e.getQuantity(),
                e.getFrequency(),
                e.getDurationDays(),
                e.getInstructions()
        );
    }
}
