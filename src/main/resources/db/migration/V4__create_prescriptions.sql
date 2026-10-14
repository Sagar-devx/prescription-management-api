CREATE TABLE prescriptions (
    id BIGSERIAL PRIMARY KEY,
    prescriber_id BIGINT NOT NULL REFERENCES prescribers(id),
    client_id BIGINT NOT NULL REFERENCES clients(id),
    record_date DATE NOT NULL,
    diagnosis TEXT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT' CHECK (status IN ('DRAFT', 'ACTIVE', 'CANCELLED')),
    idempotency_key VARCHAR(36) UNIQUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE prescription_entries (
    id BIGSERIAL PRIMARY KEY,
    prescription_id BIGINT NOT NULL REFERENCES prescriptions(id) ON DELETE CASCADE,
    entry_name VARCHAR(200) NOT NULL,
    quantity NUMERIC(10,2) NOT NULL CHECK (quantity > 0),
    frequency VARCHAR(100) NOT NULL,
    duration_days INTEGER NOT NULL CHECK (duration_days > 0),
    instructions TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_prescriptions_prescriber ON prescriptions(prescriber_id);
CREATE INDEX idx_prescriptions_client ON prescriptions(client_id);
CREATE INDEX idx_prescriptions_record_date ON prescriptions(record_date);
