package com.medical.prescription.dto.request;

import jakarta.validation.constraints.NotBlank;

public record CreatePrescriberRequest(
        @NotBlank(message = "Name is required") String name,
        @NotBlank(message = "Registration number is required") String registrationNumber,
        @NotBlank(message = "Phone number is required") String phoneNumber
) {}
