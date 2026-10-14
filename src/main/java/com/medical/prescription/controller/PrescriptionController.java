package com.medical.prescription.controller;

import com.medical.prescription.dto.request.CreatePrescriptionRequest;
import com.medical.prescription.dto.request.PrescriptionEntryRequest;
import com.medical.prescription.dto.request.UpdatePrescriptionRequest;
import com.medical.prescription.dto.response.ApiResponse;
import com.medical.prescription.dto.response.PagedResponse;
import com.medical.prescription.dto.response.PrescriptionResponse;
import com.medical.prescription.service.PrescriptionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/prescriptions")
@Tag(name = "Prescriptions", description = "Endpoints for managing medical prescriptions and line item entries")
public class PrescriptionController {

    private final PrescriptionService prescriptionService;

    public PrescriptionController(PrescriptionService prescriptionService) {
        this.prescriptionService = prescriptionService;
    }

    @GetMapping
    @Operation(summary = "Get all prescriptions (paginated and sortable)")
    public ResponseEntity<ApiResponse<PagedResponse<PrescriptionResponse>>> getAll(
            @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.ASC) Pageable pageable) {
        PagedResponse<PrescriptionResponse> response = prescriptionService.getAllPrescriptions(pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping
    @Operation(summary = "Create a new prescription with medication entries atomically")
    public ResponseEntity<ApiResponse<PrescriptionResponse>> create(
            @Valid @RequestBody CreatePrescriptionRequest request) {
        PrescriptionResponse response = prescriptionService.createPrescription(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Prescription created successfully"));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get prescription by ID with all medication entries")
    public ResponseEntity<ApiResponse<PrescriptionResponse>> getById(@PathVariable Long id) {
        PrescriptionResponse response = prescriptionService.getPrescriptionById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update prescription status and diagnosis (Admin only)")
    public ResponseEntity<ApiResponse<PrescriptionResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdatePrescriptionRequest request) {
        PrescriptionResponse response = prescriptionService.updatePrescriptionStatus(id, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Prescription updated successfully"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete prescription by ID (Admin only)")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        prescriptionService.deletePrescription(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Prescription deleted successfully"));
    }

    @PostMapping("/{id}/entries")
    @Operation(summary = "Add a medication entry to an existing prescription")
    public ResponseEntity<ApiResponse<PrescriptionResponse>> addEntry(
            @PathVariable Long id,
            @Valid @RequestBody PrescriptionEntryRequest request) {
        PrescriptionResponse response = prescriptionService.addEntry(id, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Prescription entry added successfully"));
    }

    @DeleteMapping("/{id}/entries/{entryId}")
    @Operation(summary = "Remove a medication entry from a prescription")
    public ResponseEntity<ApiResponse<PrescriptionResponse>> removeEntry(
            @PathVariable Long id,
            @PathVariable Long entryId) {
        PrescriptionResponse response = prescriptionService.removeEntry(id, entryId);
        return ResponseEntity.ok(ApiResponse.success(response, "Prescription entry removed successfully"));
    }
}
