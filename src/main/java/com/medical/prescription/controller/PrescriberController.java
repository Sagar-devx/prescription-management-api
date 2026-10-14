package com.medical.prescription.controller;

import com.medical.prescription.dto.request.CreatePrescriberRequest;
import com.medical.prescription.dto.response.ApiResponse;
import com.medical.prescription.dto.response.PagedResponse;
import com.medical.prescription.dto.response.PrescriberResponse;
import com.medical.prescription.service.PrescriberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/prescribers")
@Tag(name = "Prescribers", description = "Endpoints for managing medical prescribers/doctors")
public class PrescriberController {

    private final PrescriberService prescriberService;

    public PrescriberController(PrescriberService prescriberService) {
        this.prescriberService = prescriberService;
    }

    @GetMapping
    @Operation(summary = "Get all prescribers (paginated)")
    public ResponseEntity<ApiResponse<PagedResponse<PrescriberResponse>>> getAll(
            @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.ASC) Pageable pageable) {
        Page<PrescriberResponse> page = prescriberService.findAll(pageable);
        return ResponseEntity.ok(ApiResponse.success(PagedResponse.from(page)));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create a new prescriber (Admin only)")
    public ResponseEntity<ApiResponse<PrescriberResponse>> create(
            @Valid @RequestBody CreatePrescriberRequest request) {
        PrescriberResponse response = prescriberService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Prescriber created successfully"));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get prescriber by ID")
    public ResponseEntity<ApiResponse<PrescriberResponse>> getById(@PathVariable Long id) {
        PrescriberResponse response = prescriberService.findById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update prescriber details (Admin only)")
    public ResponseEntity<ApiResponse<PrescriberResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody CreatePrescriberRequest request) {
        PrescriberResponse response = prescriberService.update(id, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Prescriber updated successfully"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete prescriber by ID (Admin only)")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        prescriberService.delete(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Prescriber deleted successfully"));
    }
}
