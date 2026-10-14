package com.medical.prescription.service;

import com.medical.prescription.dto.request.CreatePrescriptionRequest;
import com.medical.prescription.dto.request.PrescriptionEntryRequest;
import com.medical.prescription.dto.request.UpdatePrescriptionRequest;
import com.medical.prescription.dto.response.PagedResponse;
import com.medical.prescription.dto.response.PrescriptionResponse;
import com.medical.prescription.entity.Client;
import com.medical.prescription.entity.Prescriber;
import com.medical.prescription.entity.Prescription;
import com.medical.prescription.entity.PrescriptionEntry;
import com.medical.prescription.entity.PrescriptionStatus;
import com.medical.prescription.exception.ResourceNotFoundException;
import com.medical.prescription.repository.ClientRepository;
import com.medical.prescription.repository.PrescriberRepository;
import com.medical.prescription.repository.PrescriptionRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Optional;

@Service
public class PrescriptionService {

    private final PrescriptionRepository prescriptionRepository;
    private final PrescriberRepository prescriberRepository;
    private final ClientRepository clientRepository;

    public PrescriptionService(PrescriptionRepository prescriptionRepository,
                               PrescriberRepository prescriberRepository,
                               ClientRepository clientRepository) {
        this.prescriptionRepository = prescriptionRepository;
        this.prescriberRepository = prescriberRepository;
        this.clientRepository = clientRepository;
    }

    @Transactional
    public PrescriptionResponse createPrescription(CreatePrescriptionRequest req) {
        // Idempotency check: if already processed, return existing prescription
        if (req.idempotencyKey() != null && !req.idempotencyKey().isBlank()) {
            Optional<Prescription> existing = prescriptionRepository.findByIdempotencyKey(req.idempotencyKey());
            if (existing.isPresent()) {
                return PrescriptionResponse.from(existing.get());
            }
        }

        Prescriber prescriber = prescriberRepository.findById(req.prescriberId())
                .orElseThrow(() -> new ResourceNotFoundException("Prescriber", "id", req.prescriberId()));

        Client client = clientRepository.findById(req.clientId())
                .orElseThrow(() -> new ResourceNotFoundException("Client", "id", req.clientId()));

        Prescription prescription = Prescription.builder()
                .prescriber(prescriber)
                .client(client)
                .recordDate(req.recordDate())
                .diagnosis(req.diagnosis())
                .status(PrescriptionStatus.DRAFT)
                .idempotencyKey(req.idempotencyKey())
                .entries(new ArrayList<>())
                .build();

        if (req.entries() != null) {
            for (PrescriptionEntryRequest entryReq : req.entries()) {
                PrescriptionEntry entry = PrescriptionEntry.builder()
                        .entryName(entryReq.entryName())
                        .quantity(entryReq.quantity())
                        .frequency(entryReq.frequency())
                        .durationDays(entryReq.durationDays())
                        .instructions(entryReq.instructions())
                        .build();
                prescription.addEntry(entry);
            }
        }

        Prescription saved = prescriptionRepository.save(prescription);
        return PrescriptionResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public PrescriptionResponse getPrescriptionById(Long id) {
        Prescription prescription = prescriptionRepository.findWithEntriesById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Prescription", "id", id));
        return PrescriptionResponse.from(prescription);
    }

    @Transactional(readOnly = true)
    public PagedResponse<PrescriptionResponse> getAllPrescriptions(Pageable pageable) {
        Page<PrescriptionResponse> page = prescriptionRepository.findAll(pageable)
                .map(PrescriptionResponse::from);
        return PagedResponse.from(page);
    }

    @Transactional
    public PrescriptionResponse updatePrescriptionStatus(Long id, UpdatePrescriptionRequest req) {
        Prescription prescription = prescriptionRepository.findWithEntriesById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Prescription", "id", id));

        if (req.status() != null) {
            prescription.setStatus(req.status());
        }
        if (req.diagnosis() != null && !req.diagnosis().isBlank()) {
            prescription.setDiagnosis(req.diagnosis());
        }

        Prescription saved = prescriptionRepository.save(prescription);
        return PrescriptionResponse.from(saved);
    }

    @Transactional
    public void deletePrescription(Long id) {
        if (!prescriptionRepository.existsById(id)) {
            throw new ResourceNotFoundException("Prescription", "id", id);
        }
        prescriptionRepository.deleteById(id);
    }

    @Transactional
    public PrescriptionResponse addEntry(Long prescriptionId, PrescriptionEntryRequest req) {
        Prescription prescription = prescriptionRepository.findWithEntriesById(prescriptionId)
                .orElseThrow(() -> new ResourceNotFoundException("Prescription", "id", prescriptionId));

        PrescriptionEntry entry = PrescriptionEntry.builder()
                .entryName(req.entryName())
                .quantity(req.quantity())
                .frequency(req.frequency())
                .durationDays(req.durationDays())
                .instructions(req.instructions())
                .build();

        prescription.addEntry(entry);
        Prescription saved = prescriptionRepository.save(prescription);
        return PrescriptionResponse.from(saved);
    }

    @Transactional
    public PrescriptionResponse removeEntry(Long prescriptionId, Long entryId) {
        Prescription prescription = prescriptionRepository.findWithEntriesById(prescriptionId)
                .orElseThrow(() -> new ResourceNotFoundException("Prescription", "id", prescriptionId));

        PrescriptionEntry entry = prescription.getEntries().stream()
                .filter(e -> entryId.equals(e.getId()))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("PrescriptionEntry", "id", entryId));

        prescription.removeEntry(entry);
        Prescription saved = prescriptionRepository.save(prescription);
        return PrescriptionResponse.from(saved);
    }
}
