package com.medical.prescription.dto.request;

import java.time.LocalDate;

public record CreateClientRequest(
        String name,
        LocalDate dateOfBirth,
        String phoneNumber
) {}
