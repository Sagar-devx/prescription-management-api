package com.medical.prescription.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record PrescriptionEntryRequest(
        @NotBlank(message = "Entry name is required") String entryName,

        @NotNull(message = "Quantity is required")
        @DecimalMin(value = "0.01", message = "Quantity must be greater than 0")
        BigDecimal quantity,

        @NotBlank(message = "Frequency is required") String frequency,

        @Min(value = 1, message = "Duration days must be greater than 0") int durationDays,

        String instructions
) {}
