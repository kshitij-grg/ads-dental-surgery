package edu.miu.ads.dentalsurgery.repository;

import java.util.List;
import java.util.Optional;

import edu.miu.ads.dentalsurgery.domain.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface InvoiceRepository extends JpaRepository<Invoice, Long> {

    List<Invoice> findAllByOrderByCreatedAtDesc();

    Optional<Invoice> findByAppointmentId(Long appointmentId);

    @Query("""
            select i
            from Invoice i
            join fetch i.appointment a
            join fetch a.patient
            join fetch a.dentist
            join fetch a.treatment
            where i.id = :id
            """)
    Optional<Invoice> findDetailedById(@Param("id") Long id);
}
