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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PrescriptionServiceTest {

    @Mock
    private PrescriptionRepository prescriptionRepository;

    @Mock
    private PrescriberRepository prescriberRepository;

    @Mock
    private ClientRepository clientRepository;

    @InjectMocks
    private PrescriptionService prescriptionService;

    private Prescriber testPrescriber;
    private Client testClient;
    private Prescription testPrescription;
    private PrescriptionEntry testEntry;
    private CreatePrescriptionRequest createRequest;

    @BeforeEach
    void setUp() {
        testPrescriber = Prescriber.builder()
                .id(1L)
                .name("Dr. Alice Smith")
                .registrationNumber("MED-12345")
                .phoneNumber("+1-555-0100")
                .build();

        testClient = Client.builder()
                .id(1L)
                .name("John Doe")
                .dateOfBirth(LocalDate.of(1985, 5, 20))
                .phoneNumber("+1-555-0200")
                .build();

        testEntry = PrescriptionEntry.builder()
                .id(1L)
                .entryName("Amoxicillin 500mg")
                .quantity(new BigDecimal("30.00"))
                .frequency("Three times daily")
                .durationDays(10)
                .instructions("Take with food")
                .build();

        testPrescription = Prescription.builder()
                .id(1L)
                .prescriber(testPrescriber)
                .client(testClient)
                .recordDate(LocalDate.now())
                .diagnosis("Bacterial Infection")
                .status(PrescriptionStatus.DRAFT)
                .idempotencyKey("test-key-uuid-001")
                .entries(new ArrayList<>(List.of(testEntry)))
                .build();
        testEntry.setPrescription(testPrescription);

        PrescriptionEntryRequest entryRequest = new PrescriptionEntryRequest(
                "Amoxicillin 500mg",
                new BigDecimal("30.00"),
                "Three times daily",
                10,
                "Take with food"
        );

        createRequest = new CreatePrescriptionRequest(
                1L,
                1L,
                LocalDate.now(),
                "Bacterial Infection",
                List.of(entryRequest),
                "test-key-uuid-001"
        );
    }

    @Test
    @DisplayName("testCreatePrescription_Success: Successfully creates prescription with entries")
    void testCreatePrescription_Success() {
        when(prescriptionRepository.findByIdempotencyKey("test-key-uuid-001")).thenReturn(Optional.empty());
        when(prescriberRepository.findById(1L)).thenReturn(Optional.of(testPrescriber));
        when(clientRepository.findById(1L)).thenReturn(Optional.of(testClient));
        when(prescriptionRepository.save(any(Prescription.class))).thenReturn(testPrescription);

        PrescriptionResponse response = prescriptionService.createPrescription(createRequest);

        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.diagnosis()).isEqualTo("Bacterial Infection");
        assertThat(response.prescriber().name()).isEqualTo("Dr. Alice Smith");
        assertThat(response.client().name()).isEqualTo("John Doe");
        assertThat(response.entries()).hasSize(1);
        assertThat(response.entries().get(0).entryName()).isEqualTo("Amoxicillin 500mg");

        verify(prescriptionRepository).findByIdempotencyKey("test-key-uuid-001");
        verify(prescriberRepository).findById(1L);
        verify(clientRepository).findById(1L);
        verify(prescriptionRepository).save(any(Prescription.class));
    }

    @Test
    @DisplayName("testCreatePrescription_Idempotent_ReturnsExisting: Returns existing prescription when idempotency key exists")
    void testCreatePrescription_Idempotent_ReturnsExisting() {
        when(prescriptionRepository.findByIdempotencyKey("test-key-uuid-001")).thenReturn(Optional.of(testPrescription));

        PrescriptionResponse response = prescriptionService.createPrescription(createRequest);

        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.idempotencyKey()).isEqualTo("test-key-uuid-001");

        // Verify prescriber/client lookups and save were NOT called
        verify(prescriptionRepository).findByIdempotencyKey("test-key-uuid-001");
        verifyNoInteractions(prescriberRepository);
        verifyNoInteractions(clientRepository);
        verify(prescriptionRepository, never()).save(any(Prescription.class));
    }

    @Test
    @DisplayName("testCreatePrescription_PrescriberNotFound_ThrowsException: Throws ResourceNotFoundException when prescriber missing")
    void testCreatePrescription_PrescriberNotFound_ThrowsException() {
        when(prescriptionRepository.findByIdempotencyKey("test-key-uuid-001")).thenReturn(Optional.empty());
        when(prescriberRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> prescriptionService.createPrescription(createRequest))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Prescriber")
                .hasMessageContaining("1");

        verify(prescriptionRepository, never()).save(any(Prescription.class));
    }

    @Test
    @DisplayName("testCreatePrescription_ClientNotFound_ThrowsException: Throws ResourceNotFoundException when client missing")
    void testCreatePrescription_ClientNotFound_ThrowsException() {
        when(prescriptionRepository.findByIdempotencyKey("test-key-uuid-001")).thenReturn(Optional.empty());
        when(prescriberRepository.findById(1L)).thenReturn(Optional.of(testPrescriber));
        when(clientRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> prescriptionService.createPrescription(createRequest))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Client")
                .hasMessageContaining("1");

        verify(prescriptionRepository, never()).save(any(Prescription.class));
    }

    @Test
    @DisplayName("testGetPrescriptionById_Success: Returns prescription by ID")
    void testGetPrescriptionById_Success() {
        when(prescriptionRepository.findWithEntriesById(1L)).thenReturn(Optional.of(testPrescription));

        PrescriptionResponse response = prescriptionService.getPrescriptionById(1L);

        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(1L);
        verify(prescriptionRepository).findWithEntriesById(1L);
    }

    @Test
    @DisplayName("testGetPrescriptionById_NotFound: Throws ResourceNotFoundException when ID not found")
    void testGetPrescriptionById_NotFound() {
        when(prescriptionRepository.findWithEntriesById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> prescriptionService.getPrescriptionById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Prescription");
    }

    @Test
    @DisplayName("testGetAllPrescriptions: Returns paginated response")
    void testGetAllPrescriptions() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Prescription> page = new PageImpl<>(List.of(testPrescription), pageable, 1);
        when(prescriptionRepository.findAll(pageable)).thenReturn(page);

        PagedResponse<PrescriptionResponse> result = prescriptionService.getAllPrescriptions(pageable);

        assertThat(result).isNotNull();
        assertThat(result.totalElements()).isEqualTo(1);
        assertThat(result.content()).hasSize(1);
    }

    @Test
    @DisplayName("testUpdatePrescriptionStatus: Updates status and diagnosis")
    void testUpdatePrescriptionStatus() {
        UpdatePrescriptionRequest updateReq = new UpdatePrescriptionRequest("Updated diagnosis", PrescriptionStatus.ACTIVE);
        when(prescriptionRepository.findWithEntriesById(1L)).thenReturn(Optional.of(testPrescription));
        when(prescriptionRepository.save(any(Prescription.class))).thenReturn(testPrescription);

        PrescriptionResponse response = prescriptionService.updatePrescriptionStatus(1L, updateReq);

        assertThat(response).isNotNull();
        assertThat(testPrescription.getStatus()).isEqualTo(PrescriptionStatus.ACTIVE);
        assertThat(testPrescription.getDiagnosis()).isEqualTo("Updated diagnosis");
    }

    @Test
    @DisplayName("testDeletePrescription: Deletes existing prescription")
    void testDeletePrescription() {
        when(prescriptionRepository.existsById(1L)).thenReturn(true);
        doNothing().when(prescriptionRepository).deleteById(1L);

        prescriptionService.deletePrescription(1L);

        verify(prescriptionRepository).deleteById(1L);
    }

    @Test
    @DisplayName("testDeletePrescription_NotFound: Throws ResourceNotFoundException")
    void testDeletePrescription_NotFound() {
        when(prescriptionRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> prescriptionService.deletePrescription(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("testAddEntry: Appends entry to prescription")
    void testAddEntry() {
        PrescriptionEntryRequest newEntryReq = new PrescriptionEntryRequest(
                "Ibuprofen 400mg",
                new BigDecimal("20.00"),
                "Twice daily",
                5,
                "After meals"
        );
        when(prescriptionRepository.findWithEntriesById(1L)).thenReturn(Optional.of(testPrescription));
        when(prescriptionRepository.save(any(Prescription.class))).thenReturn(testPrescription);

        PrescriptionResponse response = prescriptionService.addEntry(1L, newEntryReq);

        assertThat(response).isNotNull();
        assertThat(testPrescription.getEntries()).hasSize(2);
    }

    @Test
    @DisplayName("testRemoveEntry: Removes entry from prescription")
    void testRemoveEntry() {
        when(prescriptionRepository.findWithEntriesById(1L)).thenReturn(Optional.of(testPrescription));
        when(prescriptionRepository.save(any(Prescription.class))).thenReturn(testPrescription);

        PrescriptionResponse response = prescriptionService.removeEntry(1L, 1L);

        assertThat(response).isNotNull();
        assertThat(testPrescription.getEntries()).isEmpty();
    }
}
