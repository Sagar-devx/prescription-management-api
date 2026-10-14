package com.medical.prescription.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "prescriptions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Prescription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "prescriber_id", nullable = false)
    private Prescriber prescriber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    @Column(name = "record_date", nullable = false)
    private LocalDate recordDate;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String diagnosis;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private PrescriptionStatus status = PrescriptionStatus.DRAFT;

    @Column(name = "idempotency_key", unique = true, length = 36)
    private String idempotencyKey;

    @OneToMany(mappedBy = "prescription", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<PrescriptionEntry> entries = new ArrayList<>();

    /**
     * Bi-directional relationship helper — always use this to add entries.
     */
    public void addEntry(PrescriptionEntry entry) {
        entry.setPrescription(this);
        this.entries.add(entry);
    }

    /**
     * Remove entry and sever back-reference.
     */
    public void removeEntry(PrescriptionEntry entry) {
        this.entries.remove(entry);
        entry.setPrescription(null);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Prescription that = (Prescription) o;
        return idempotencyKey != null && idempotencyKey.equals(that.idempotencyKey);
    }

    @Override
    public int hashCode() {
        return idempotencyKey != null ? idempotencyKey.hashCode() : 0;
    }
}
