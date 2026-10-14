package com.medical.prescription.dto.response;

import com.medical.prescription.entity.Client;

import java.time.LocalDate;

public record ClientResponse(Long id, String name, LocalDate dateOfBirth, String phoneNumber) {

    public static ClientResponse from(Client c) {
        return new ClientResponse(c.getId(), c.getName(), c.getDateOfBirth(), c.getPhoneNumber());
    }
}
