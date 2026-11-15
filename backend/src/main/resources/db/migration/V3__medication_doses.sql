CREATE TABLE medication_doses (
    id BIGSERIAL PRIMARY KEY,
    medication_id BIGINT NOT NULL,
    dose_date DATE NOT NULL,
    scheduled_time VARCHAR(5) NOT NULL,
    status VARCHAR(32) NOT NULL DEFAULT 'PENDING',
    status_updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_dose_medication FOREIGN KEY (medication_id) REFERENCES medications(id),
    CONSTRAINT uk_medication_dose UNIQUE (medication_id, dose_date, scheduled_time)
);
