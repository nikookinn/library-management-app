package com.nikookinn.librarymanagement.config;

import com.nikookinn.librarymanagement.entity.Role;
import com.nikookinn.librarymanagement.entity.User;
import com.nikookinn.librarymanagement.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DefaultAdminInitializer {

    @Bean
    public CommandLineRunner createDefaultAdmin(UserRepository userRepository,
                                                PasswordEncoder passwordEncoder,
                                                @Value("${app.admin.email}") String adminEmail,
                                                @Value("${app.admin.password}") String adminPassword) {
        return args -> {
            if (userRepository.existsByRole(Role.ADMIN)) {
                return;
            }

            if (userRepository.existsByEmail(adminEmail)) {
                throw new IllegalStateException("The initial admin email is already used by a non-admin user");
            }

            User admin = new User();
            admin.setEmail(adminEmail);
            admin.setPasswordHash(passwordEncoder.encode(adminPassword));
            admin.setRole(Role.ADMIN);
            userRepository.save(admin);
        };
    }
}
