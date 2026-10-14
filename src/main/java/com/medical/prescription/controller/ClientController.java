package com.medical.prescription.controller;

import com.medical.prescription.dto.request.CreateClientRequest;
import com.medical.prescription.dto.response.ApiResponse;
import com.medical.prescription.dto.response.ClientResponse;
import com.medical.prescription.dto.response.PagedResponse;
import com.medical.prescription.service.ClientService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/clients")
@Tag(name = "Clients", description = "Endpoints for managing client/patient profiles")
public class ClientController {

    private final ClientService clientService;

    public ClientController(ClientService clientService) {
        this.clientService = clientService;
    }

    @GetMapping
    @Operation(summary = "Get all clients (paginated)")
    public ResponseEntity<ApiResponse<PagedResponse<ClientResponse>>> getAll(
            @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.ASC) Pageable pageable) {
        Page<ClientResponse> page = clientService.findAll(pageable);
        return ResponseEntity.ok(ApiResponse.success(PagedResponse.from(page)));
    }

    @PostMapping
    @Operation(summary = "Create a new client")
    public ResponseEntity<ApiResponse<ClientResponse>> create(
            @Valid @RequestBody CreateClientRequest request) {
        ClientResponse response = clientService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Client created successfully"));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get client by ID")
    public ResponseEntity<ApiResponse<ClientResponse>> getById(@PathVariable Long id) {
        ClientResponse response = clientService.findById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update client details")
    public ResponseEntity<ApiResponse<ClientResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody CreateClientRequest request) {
        ClientResponse response = clientService.update(id, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Client updated successfully"));
    }
}
