package com.eldercare360.backend.config;

import com.eldercare360.backend.model.AppUser;
import com.eldercare360.backend.model.Appointment;
import com.eldercare360.backend.model.AppointmentStatus;
import com.eldercare360.backend.model.Medication;
import com.eldercare360.backend.model.MedicationStatus;
import com.eldercare360.backend.model.MedicalRecord;
import com.eldercare360.backend.model.Role;
import com.eldercare360.backend.repository.AppointmentRepository;
import com.eldercare360.backend.repository.AppUserRepository;
import com.eldercare360.backend.repository.MedicalRecordRepository;
import com.eldercare360.backend.repository.MedicationRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Component
public class DbSeeder implements ApplicationRunner {
    private final AppUserRepository appUserRepository;
    private final MedicationRepository medicationRepository;
    private final MedicalRecordRepository medicalRecordRepository;
    private final AppointmentRepository appointmentRepository;
    private final PasswordEncoder passwordEncoder;

    public DbSeeder(AppUserRepository appUserRepository,
                    MedicationRepository medicationRepository,
                    MedicalRecordRepository medicalRecordRepository,
                    AppointmentRepository appointmentRepository,
                    PasswordEncoder passwordEncoder) {
        this.appUserRepository = appUserRepository;
        this.medicationRepository = medicationRepository;
        this.medicalRecordRepository = medicalRecordRepository;
        this.appointmentRepository = appointmentRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (appUserRepository.count() > 0) {
            return;
        }

        AppUser elder = new AppUser();
        elder.setFullName("Eleanor Brooks");
        elder.setEmail("elder@example.com");
        elder.setPassword(passwordEncoder.encode("Password123"));
        elder.setPhone("+1-555-0184");
        elder.setRole(Role.ELDERLY);
        appUserRepository.save(elder);

        AppUser caregiver = new AppUser();
        caregiver.setFullName("Maria Chen");
        caregiver.setEmail("caregiver@example.com");
        caregiver.setPassword(passwordEncoder.encode("Password123"));
        caregiver.setPhone("+1-555-0185");
        caregiver.setRole(Role.CAREGIVER);
        appUserRepository.save(caregiver);

        Medication medication = new Medication();
        medication.setUser(elder);
        medication.setName("Aspirin");
        medication.setDosage("81mg");
        medication.setScheduleTime("08:00 AM");
        medication.setFrequency("Daily");
        medication.setNotes("Take after breakfast");
        medication.setStatus(MedicationStatus.PENDING);
        medicationRepository.save(medication);

        Medication medication2 = new Medication();
        medication2.setUser(elder);
        medication2.setName("Vitamin D");
        medication2.setDosage("1000 IU");
        medication2.setScheduleTime("06:30 PM");
        medication2.setFrequency("Daily");
        medication2.setNotes("With dinner");
        medication2.setStatus(MedicationStatus.TAKEN);
        medicationRepository.save(medication2);

        MedicalRecord record = new MedicalRecord();
        record.setUser(elder);
        record.setTitle("Annual Checkup");
        record.setDocumentType("Prescription");
        record.setFileName("annual-checkup.pdf");
        record.setSummary("Routine follow-up on blood pressure and vitamins.");
        record.setExtractedText("Medication: Aspirin 81mg daily. Vitamin D 1000 IU daily. Follow up in 3 months.");
        medicalRecordRepository.save(record);

        Appointment appointment = new Appointment();
        appointment.setUser(elder);
        appointment.setTitle("Cardiology Review");
        appointment.setDoctorName("Dr. Patel");
        appointment.setLocation("City Clinic, Room 2");
        appointment.setAppointmentTime(Instant.now().plus(7, ChronoUnit.DAYS));
        appointment.setNotes("Blood pressure monitoring");
        appointment.setStatus(AppointmentStatus.SCHEDULED);
        appointmentRepository.save(appointment);
    }
}
