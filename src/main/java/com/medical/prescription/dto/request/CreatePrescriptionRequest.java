package com.medical.prescription.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.List;

public record CreatePrescriptionRequest(
        @NotNull(message = "Prescriber ID is required") Long prescriberId,
        @NotNull(message = "Client ID is required") Long clientId,
        @NotNull(message = "Record date is required") LocalDate recordDate,
        @NotBlank(message = "Diagnosis is required") String diagnosis,
        @NotEmpty(message = "At least one entry is required")
        @Valid List<PrescriptionEntryRequest> entries,
        @NotBlank(message = "Idempotency key is required") String idempotencyKey
) {}
