package edu.miu.ads.dentalsurgery.repository;

import java.util.Optional;

import edu.miu.ads.dentalsurgery.domain.Dentist;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DentistRepository extends JpaRepository<Dentist, Long> {
    Optional<Dentist> findByLicenseNumber(String licenseNumber);
}
