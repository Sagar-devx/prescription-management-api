package com.medical.prescription.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "prescribers")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Prescriber {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "registration_number", nullable = false, unique = true, length = 50)
    private String registrationNumber;

    @Column(name = "phone_number", nullable = false, length = 20)
    private String phoneNumber;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Prescriber that = (Prescriber) o;
        return registrationNumber != null && registrationNumber.equals(that.registrationNumber);
    }

    @Override
    public int hashCode() {
        return registrationNumber != null ? registrationNumber.hashCode() : 0;
    }
}
