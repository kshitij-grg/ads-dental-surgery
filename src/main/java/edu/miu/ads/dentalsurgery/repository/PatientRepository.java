package edu.miu.ads.dentalsurgery.repository;

import edu.miu.ads.dentalsurgery.domain.Patient;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PatientRepository extends JpaRepository<Patient, Long> {
}
