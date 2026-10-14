package com.medical.prescription.service;

import com.medical.prescription.dto.request.CreatePrescriberRequest;
import com.medical.prescription.dto.response.PrescriberResponse;
import com.medical.prescription.entity.Prescriber;
import com.medical.prescription.exception.DuplicateResourceException;
import com.medical.prescription.exception.ResourceNotFoundException;
import com.medical.prescription.repository.PrescriberRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PrescriberService {

    private final PrescriberRepository prescriberRepository;

    public PrescriberService(PrescriberRepository prescriberRepository) {
        this.prescriberRepository = prescriberRepository;
    }

    @Transactional(readOnly = true)
    public Page<PrescriberResponse> findAll(Pageable pageable) {
        return prescriberRepository.findAll(pageable).map(PrescriberResponse::from);
    }

    @Transactional(readOnly = true)
    public PrescriberResponse findById(Long id) {
        return prescriberRepository.findById(id)
                .map(PrescriberResponse::from)
                .orElseThrow(() -> new ResourceNotFoundException("Prescriber", "id", id));
    }

    @Transactional
    public PrescriberResponse create(CreatePrescriberRequest request) {
        if (prescriberRepository.existsByRegistrationNumber(request.registrationNumber())) {
            throw new DuplicateResourceException("Prescriber", "registrationNumber", request.registrationNumber());
        }

        Prescriber prescriber = Prescriber.builder()
                .name(request.name())
                .registrationNumber(request.registrationNumber())
                .phoneNumber(request.phoneNumber())
                .build();

        return PrescriberResponse.from(prescriberRepository.save(prescriber));
    }

    @Transactional
    public PrescriberResponse update(Long id, CreatePrescriberRequest request) {
        Prescriber prescriber = prescriberRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Prescriber", "id", id));

        // Check for duplicate registration number only if it's changing
        if (!prescriber.getRegistrationNumber().equals(request.registrationNumber())
                && prescriberRepository.existsByRegistrationNumber(request.registrationNumber())) {
            throw new DuplicateResourceException("Prescriber", "registrationNumber", request.registrationNumber());
        }

        prescriber.setName(request.name());
        prescriber.setRegistrationNumber(request.registrationNumber());
        prescriber.setPhoneNumber(request.phoneNumber());

        return PrescriberResponse.from(prescriberRepository.save(prescriber));
    }

    @Transactional
    public void delete(Long id) {
        if (!prescriberRepository.existsById(id)) {
            throw new ResourceNotFoundException("Prescriber", "id", id);
        }
        prescriberRepository.deleteById(id);
    }
}
