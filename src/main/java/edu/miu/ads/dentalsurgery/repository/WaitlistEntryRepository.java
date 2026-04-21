package edu.miu.ads.dentalsurgery.repository;

import java.util.List;
import java.util.Optional;

import edu.miu.ads.dentalsurgery.domain.WaitlistEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface WaitlistEntryRepository extends JpaRepository<WaitlistEntry, Long> {

    @Query("""
            select w
            from WaitlistEntry w
            join fetch w.patient
            join fetch w.treatment
            where w.active = true
            """)
    List<WaitlistEntry> findAllActiveDetailed();

    @Query("""
            select w
            from WaitlistEntry w
            join fetch w.patient
            join fetch w.treatment
            order by w.active desc, w.priority desc, w.createdAt asc
            """)
    List<WaitlistEntry> findAllDetailedOrderByPriority();

    @Query("""
            select w
            from WaitlistEntry w
            join fetch w.patient
            join fetch w.treatment
            where w.id = :id
            """)
    Optional<WaitlistEntry> findDetailedById(@Param("id") Long id);
}