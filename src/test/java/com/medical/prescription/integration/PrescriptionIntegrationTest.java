package com.medical.prescription.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.medical.prescription.dto.request.*;
import com.medical.prescription.entity.PrescriptionStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureMockMvc
class PrescriptionIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("End-to-End Integration Flow: Login, Create Prescriber & Client, Create Prescription with Idempotency, Manage Entries, Update & Delete")
    void testEndToEndPrescriptionLifecycle() throws Exception {
        // Step 1: Login as seeded admin user (Admin@123 from V5 Flyway migration)
        LoginRequest loginRequest = new LoginRequest("admin", "Admin@123");
        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.token").isString())
                .andReturn();

        String token = objectMapper.readTree(loginResult.getResponse().getContentAsString())
                .path("data").path("token").asText();
        String authHeader = "Bearer " + token;

        // Step 2: Create a Prescriber as ADMIN
        String regNumber = "REG-" + UUID.randomUUID().toString().substring(0, 8);
        CreatePrescriberRequest prescriberReq = new CreatePrescriberRequest(
                "Dr. Gregory House",
                regNumber,
                "+1-555-123-4567"
        );
        MvcResult prescriberResult = mockMvc.perform(post("/api/prescribers")
                        .header(HttpHeaders.AUTHORIZATION, authHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(prescriberReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("Dr. Gregory House"))
                .andReturn();

        long prescriberId = objectMapper.readTree(prescriberResult.getResponse().getContentAsString())
                .path("data").path("id").asLong();

        // Step 3: Create a Client
        CreateClientRequest clientReq = new CreateClientRequest(
                "James Wilson",
                LocalDate.of(1975, 4, 12),
                "+1-555-987-6543"
        );
        MvcResult clientResult = mockMvc.perform(post("/api/clients")
                        .header(HttpHeaders.AUTHORIZATION, authHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(clientReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("James Wilson"))
                .andReturn();

        long clientId = objectMapper.readTree(clientResult.getResponse().getContentAsString())
                .path("data").path("id").asLong();

        // Step 4: Create a Prescription with medication entries
        String idempotencyKey = UUID.randomUUID().toString();
        PrescriptionEntryRequest entry1 = new PrescriptionEntryRequest(
                "Amoxicillin 500mg",
                new BigDecimal("30.00"),
                "Three times daily",
                10,
                "Take with meals"
        );
        CreatePrescriptionRequest createRxReq = new CreatePrescriptionRequest(
                prescriberId,
                clientId,
                LocalDate.now(),
                "Acute Sinusitis",
                List.of(entry1),
                idempotencyKey
        );

        MvcResult rxResult = mockMvc.perform(post("/api/prescriptions")
                        .header(HttpHeaders.AUTHORIZATION, authHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRxReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.diagnosis").value("Acute Sinusitis"))
                .andExpect(jsonPath("$.data.status").value("DRAFT"))
                .andExpect(jsonPath("$.data.entries").isArray())
                .andExpect(jsonPath("$.data.entries[0].entryName").value("Amoxicillin 500mg"))
                .andReturn();

        long prescriptionId = objectMapper.readTree(rxResult.getResponse().getContentAsString())
                .path("data").path("id").asLong();

        // Step 5: Test Idempotency - submit the exact same request again with the same idempotency key
        MvcResult duplicateResult = mockMvc.perform(post("/api/prescriptions")
                        .header(HttpHeaders.AUTHORIZATION, authHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRxReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(prescriptionId))
                .andExpect(jsonPath("$.data.idempotencyKey").value(idempotencyKey))
                .andReturn();

        long duplicateRxId = objectMapper.readTree(duplicateResult.getResponse().getContentAsString())
                .path("data").path("id").asLong();
        assertThat(duplicateRxId).isEqualTo(prescriptionId);

        // Step 6: Fetch prescription by ID
        mockMvc.perform(get("/api/prescriptions/" + prescriptionId)
                        .header(HttpHeaders.AUTHORIZATION, authHeader))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(prescriptionId))
                .andExpect(jsonPath("$.data.prescriber.name").value("Dr. Gregory House"))
                .andExpect(jsonPath("$.data.client.name").value("James Wilson"))
                .andExpect(jsonPath("$.data.entries[0].entryName").value("Amoxicillin 500mg"));

        // Step 7: Add a second medication entry
        PrescriptionEntryRequest entry2 = new PrescriptionEntryRequest(
                "Cetirizine 10mg",
                new BigDecimal("14.00"),
                "Once daily at bedtime",
                14,
                "For allergic rhinitis symptoms"
        );
        MvcResult addEntryResult = mockMvc.perform(post("/api/prescriptions/" + prescriptionId + "/entries")
                        .header(HttpHeaders.AUTHORIZATION, authHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(entry2)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.entries").isArray())
                .andReturn();

        JsonNode entriesNode = objectMapper.readTree(addEntryResult.getResponse().getContentAsString())
                .path("data").path("entries");
        assertThat(entriesNode.size()).isEqualTo(2);
        long addedEntryId = entriesNode.get(1).path("id").asLong();

        // Step 8: Update prescription status to ACTIVE and update diagnosis
        UpdatePrescriptionRequest updateReq = new UpdatePrescriptionRequest(
                "Acute Bacterial Sinusitis confirmed",
                PrescriptionStatus.ACTIVE
        );
        mockMvc.perform(put("/api/prescriptions/" + prescriptionId)
                        .header(HttpHeaders.AUTHORIZATION, authHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("ACTIVE"))
                .andExpect(jsonPath("$.data.diagnosis").value("Acute Bacterial Sinusitis confirmed"));

        // Step 9: Remove the added medication entry
        mockMvc.perform(delete("/api/prescriptions/" + prescriptionId + "/entries/" + addedEntryId)
                        .header(HttpHeaders.AUTHORIZATION, authHeader))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.entries").isArray());

        // Step 10: Delete prescription (ADMIN only)
        mockMvc.perform(delete("/api/prescriptions/" + prescriptionId)
                        .header(HttpHeaders.AUTHORIZATION, authHeader))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        // Step 11: Verify prescription is now gone (404 Not Found)
        mockMvc.perform(get("/api/prescriptions/" + prescriptionId)
                        .header(HttpHeaders.AUTHORIZATION, authHeader))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }
}
