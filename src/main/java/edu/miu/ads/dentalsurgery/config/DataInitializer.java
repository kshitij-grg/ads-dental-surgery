package edu.miu.ads.dentalsurgery.config;

import edu.miu.ads.dentalsurgery.domain.AppUser;
import edu.miu.ads.dentalsurgery.domain.Role;
import edu.miu.ads.dentalsurgery.repository.AppUserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner seedUsers(AppUserRepository repository, PasswordEncoder passwordEncoder) {
        return args -> {
            createUserIfMissing(repository, passwordEncoder, "admin", "admin123", Role.ADMIN);
            createUserIfMissing(repository, passwordEncoder, "dentist", "dentist123", Role.DENTIST);
            createUserIfMissing(repository, passwordEncoder, "reception", "reception123", Role.RECEPTIONIST);
        };
    }

    private void createUserIfMissing(
            AppUserRepository repository,
            PasswordEncoder passwordEncoder,
            String username,
            String rawPassword,
            Role role) {
        if (repository.findByUsername(username).isEmpty()) {
            AppUser user = new AppUser(username, passwordEncoder.encode(rawPassword), role, true);
            repository.save(user);
        }
    }
}
