package com.medical.prescription.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.medical.prescription.dto.request.CreatePrescriptionRequest;
import com.medical.prescription.dto.request.PrescriptionEntryRequest;
import com.medical.prescription.dto.request.UpdatePrescriptionRequest;
import com.medical.prescription.dto.response.ClientResponse;
import com.medical.prescription.dto.response.PagedResponse;
import com.medical.prescription.dto.response.PrescriberResponse;
import com.medical.prescription.dto.response.PrescriptionEntryResponse;
import com.medical.prescription.dto.response.PrescriptionResponse;
import com.medical.prescription.entity.PrescriptionStatus;
import com.medical.prescription.exception.GlobalExceptionHandler;
import com.medical.prescription.exception.ResourceNotFoundException;
import com.medical.prescription.security.JwtTokenProvider;
import com.medical.prescription.security.UserDetailsServiceImpl;
import com.medical.prescription.service.PrescriptionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = PrescriptionController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class PrescriptionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PrescriptionService prescriptionService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private UserDetailsServiceImpl userDetailsService;

    private PrescriptionResponse sampleResponse;
    private CreatePrescriptionRequest sampleCreateRequest;

    @BeforeEach
    void setUp() {
        PrescriberResponse prescriberResp = new PrescriberResponse(1L, "Dr. Alice Smith", "MED-12345", "+1-555-0100");
        ClientResponse clientResp = new ClientResponse(1L, "John Doe", LocalDate.of(1985, 5, 20), "+1-555-0200");
        PrescriptionEntryResponse entryResp = new PrescriptionEntryResponse(
                1L, "Amoxicillin 500mg", new BigDecimal("30.00"), "Three times daily", 10, "Take with food"
        );

        sampleResponse = new PrescriptionResponse(
                1L,
                prescriberResp,
                clientResp,
                LocalDate.of(2026, 10, 14),
                "Bacterial Infection",
                PrescriptionStatus.DRAFT,
                "test-idem-key-101",
                List.of(entryResp)
        );

        PrescriptionEntryRequest entryReq = new PrescriptionEntryRequest(
                "Amoxicillin 500mg",
                new BigDecimal("30.00"),
                "Three times daily",
                10,
                "Take with food"
        );

        sampleCreateRequest = new CreatePrescriptionRequest(
                1L,
                1L,
                LocalDate.of(2026, 10, 14),
                "Bacterial Infection",
                List.of(entryReq),
                "test-idem-key-101"
        );
    }

    @Test
    @DisplayName("POST /api/prescriptions: Returns 201 Created and PrescriptionResponse")
    void testCreatePrescription_Success() throws Exception {
        when(prescriptionService.createPrescription(any(CreatePrescriptionRequest.class))).thenReturn(sampleResponse);

        mockMvc.perform(post("/api/prescriptions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleCreateRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.diagnosis").value("Bacterial Infection"))
                .andExpect(jsonPath("$.data.prescriber.name").value("Dr. Alice Smith"))
                .andExpect(jsonPath("$.data.client.name").value("John Doe"))
                .andExpect(jsonPath("$.data.entries[0].entryName").value("Amoxicillin 500mg"));

        verify(prescriptionService).createPrescription(any(CreatePrescriptionRequest.class));
    }

    @Test
    @DisplayName("POST /api/prescriptions: Returns 400 Bad Request on invalid payload")
    void testCreatePrescription_ValidationFailure() throws Exception {
        // Missing prescriberId, clientId, recordDate, etc.
        CreatePrescriptionRequest invalidRequest = new CreatePrescriptionRequest(
                null, null, null, "", List.of(), ""
        );

        mockMvc.perform(post("/api/prescriptions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errors").isArray());

        verifyNoInteractions(prescriptionService);
    }

    @Test
    @DisplayName("GET /api/prescriptions/{id}: Returns 200 OK with prescription")
    void testGetPrescriptionById_Success() throws Exception {
        when(prescriptionService.getPrescriptionById(1L)).thenReturn(sampleResponse);

        mockMvc.perform(get("/api/prescriptions/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.status").value("DRAFT"));

        verify(prescriptionService).getPrescriptionById(1L);
    }

    @Test
    @DisplayName("GET /api/prescriptions/{id}: Returns 404 Not Found when ID missing")
    void testGetPrescriptionById_NotFound() throws Exception {
        when(prescriptionService.getPrescriptionById(99L))
                .thenThrow(new ResourceNotFoundException("Prescription", "id", 99L));

        mockMvc.perform(get("/api/prescriptions/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Prescription not found with id: '99'"));

        verify(prescriptionService).getPrescriptionById(99L);
    }

    @Test
    @DisplayName("GET /api/prescriptions: Returns 200 OK with paginated list")
    void testGetAllPrescriptions() throws Exception {
        PagedResponse<PrescriptionResponse> paged = new PagedResponse<>(
                List.of(sampleResponse), 0, 20, 1, 1, true
        );
        when(prescriptionService.getAllPrescriptions(any())).thenReturn(paged);

        mockMvc.perform(get("/api/prescriptions?page=0&size=20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].id").value(1))
                .andExpect(jsonPath("$.data.totalElements").value(1));

        verify(prescriptionService).getAllPrescriptions(any());
    }

    @Test
    @DisplayName("PUT /api/prescriptions/{id}: Returns 200 OK with updated prescription")
    void testUpdatePrescription() throws Exception {
        UpdatePrescriptionRequest updateReq = new UpdatePrescriptionRequest("Updated Diagnosis", PrescriptionStatus.ACTIVE);
        when(prescriptionService.updatePrescriptionStatus(eq(1L), any(UpdatePrescriptionRequest.class)))
                .thenReturn(sampleResponse);

        mockMvc.perform(put("/api/prescriptions/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(prescriptionService).updatePrescriptionStatus(eq(1L), any(UpdatePrescriptionRequest.class));
    }

    @Test
    @DisplayName("DELETE /api/prescriptions/{id}: Returns 200 OK")
    void testDeletePrescription() throws Exception {
        doNothing().when(prescriptionService).deletePrescription(1L);

        mockMvc.perform(delete("/api/prescriptions/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Prescription deleted successfully"));

        verify(prescriptionService).deletePrescription(1L);
    }

    @Test
    @DisplayName("POST /api/prescriptions/{id}/entries: Returns 201 Created")
    void testAddEntry() throws Exception {
        PrescriptionEntryRequest entryReq = new PrescriptionEntryRequest(
                "Paracetamol 500mg",
                new BigDecimal("10.00"),
                "As needed",
                3,
                "For pain relief"
        );
        when(prescriptionService.addEntry(eq(1L), any(PrescriptionEntryRequest.class)))
                .thenReturn(sampleResponse);

        mockMvc.perform(post("/api/prescriptions/1/entries")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(entryReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true));

        verify(prescriptionService).addEntry(eq(1L), any(PrescriptionEntryRequest.class));
    }

    @Test
    @DisplayName("DELETE /api/prescriptions/{id}/entries/{entryId}: Returns 200 OK")
    void testRemoveEntry() throws Exception {
        when(prescriptionService.removeEntry(1L, 1L)).thenReturn(sampleResponse);

        mockMvc.perform(delete("/api/prescriptions/1/entries/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Prescription entry removed successfully"));

        verify(prescriptionService).removeEntry(1L, 1L);
    }
}
