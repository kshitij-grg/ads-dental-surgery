package edu.miu.ads.dentalsurgery.repository;

import java.util.Optional;

import edu.miu.ads.dentalsurgery.domain.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AppUserRepository extends JpaRepository<AppUser, Long> {
    Optional<AppUser> findByUsername(String username);
}
