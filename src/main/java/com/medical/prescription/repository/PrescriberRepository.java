package com.medical.prescription.repository;

import com.medical.prescription.entity.Prescriber;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PrescriberRepository extends JpaRepository<Prescriber, Long> {
    boolean existsByRegistrationNumber(String registrationNumber);
    Optional<Prescriber> findByRegistrationNumber(String registrationNumber);
}
