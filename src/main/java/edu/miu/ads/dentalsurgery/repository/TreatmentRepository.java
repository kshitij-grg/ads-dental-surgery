package edu.miu.ads.dentalsurgery.repository;

import java.util.Optional;

import edu.miu.ads.dentalsurgery.domain.Treatment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TreatmentRepository extends JpaRepository<Treatment, Long> {
    Optional<Treatment> findByCode(String code);
}
