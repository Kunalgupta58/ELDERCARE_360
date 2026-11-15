package com.eldercare360.backend.model;

import jakarta.persistence.*;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(
        name = "medication_doses",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_medication_dose",
                columnNames = {"medication_id", "dose_date", "scheduled_time"}
        )
)
public class MedicationDose {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "medication_id", nullable = false)
    private Medication medication;

    @Column(name = "dose_date", nullable = false)
    private LocalDate doseDate;

    @Column(name = "scheduled_time", nullable = false, length = 5)
    private String scheduledTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MedicationStatus status = MedicationStatus.PENDING;

    @Column(name = "status_updated_at", nullable = false)
    private Instant statusUpdatedAt = Instant.now();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Medication getMedication() { return medication; }
    public void setMedication(Medication medication) { this.medication = medication; }
    public LocalDate getDoseDate() { return doseDate; }
    public void setDoseDate(LocalDate doseDate) { this.doseDate = doseDate; }
    public String getScheduledTime() { return scheduledTime; }
    public void setScheduledTime(String scheduledTime) { this.scheduledTime = scheduledTime; }
    public MedicationStatus getStatus() { return status; }
    public void setStatus(MedicationStatus status) { this.status = status; }
    public Instant getStatusUpdatedAt() { return statusUpdatedAt; }
    public void setStatusUpdatedAt(Instant statusUpdatedAt) { this.statusUpdatedAt = statusUpdatedAt; }
}
