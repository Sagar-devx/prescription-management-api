package com.medical.prescription.dto.response;

import com.medical.prescription.entity.Prescription;
import com.medical.prescription.entity.PrescriptionStatus;

import java.time.LocalDate;
import java.util.List;

public record PrescriptionResponse(
        Long id,
        PrescriberResponse prescriber,
        ClientResponse client,
        LocalDate recordDate,
        String diagnosis,
        PrescriptionStatus status,
        String idempotencyKey,
        List<PrescriptionEntryResponse> entries
) {
    public static PrescriptionResponse from(Prescription p) {
        List<PrescriptionEntryResponse> entryResponses = p.getEntries().stream()
                .map(PrescriptionEntryResponse::from)
                .toList();
        return new PrescriptionResponse(
                p.getId(),
                PrescriberResponse.from(p.getPrescriber()),
                ClientResponse.from(p.getClient()),
                p.getRecordDate(),
                p.getDiagnosis(),
                p.getStatus(),
                p.getIdempotencyKey(),
                entryResponses
        );
    }
}
