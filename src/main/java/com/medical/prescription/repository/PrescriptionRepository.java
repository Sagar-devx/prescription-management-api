package com.medical.prescription.repository;

import com.medical.prescription.entity.Prescription;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PrescriptionRepository extends JpaRepository<Prescription, Long> {

    Optional<Prescription> findByIdempotencyKey(String idempotencyKey);

    @EntityGraph(attributePaths = {"prescriber", "client", "entries"})
    Optional<Prescription> findWithEntriesById(Long id);

    @EntityGraph(attributePaths = {"prescriber", "client"})
    Page<Prescription> findAll(Pageable pageable);
}
