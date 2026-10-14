package com.medical.prescription.dto.request;

import com.medical.prescription.entity.PrescriptionStatus;

public record UpdatePrescriptionRequest(
        String diagnosis,
        PrescriptionStatus status
) {}
