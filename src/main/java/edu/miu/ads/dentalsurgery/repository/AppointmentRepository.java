package edu.miu.ads.dentalsurgery.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import edu.miu.ads.dentalsurgery.domain.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    List<Appointment> findAllByOrderByStartAtAsc();

    @Query("""
            select case when count(a) > 0 then true else false end
            from Appointment a
            where a.dentist.id = :dentistId
              and a.status <> edu.miu.ads.dentalsurgery.domain.AppointmentStatus.CANCELLED
              and (:appointmentId is null or a.id <> :appointmentId)
              and a.startAt < :endAt
              and a.endAt > :startAt
            """)
    boolean existsOverlappingAppointment(
            @Param("dentistId") Long dentistId,
            @Param("startAt") LocalDateTime startAt,
            @Param("endAt") LocalDateTime endAt,
            @Param("appointmentId") Long appointmentId);

    @Query("""
            select a
            from Appointment a
            join fetch a.patient
            join fetch a.dentist
            join fetch a.treatment
            where a.id = :id
            """)
    Optional<Appointment> findDetailedById(@Param("id") Long id);
}
