package com.medical.prescription.dto.response;

import com.medical.prescription.entity.Prescriber;

public record PrescriberResponse(Long id, String name, String registrationNumber, String phoneNumber) {

    public static PrescriberResponse from(Prescriber p) {
        return new PrescriberResponse(p.getId(), p.getName(), p.getRegistrationNumber(), p.getPhoneNumber());
    }
}
