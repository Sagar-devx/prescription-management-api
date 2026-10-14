package com.medical.prescription.config;

import com.medical.prescription.entity.AppUser;
import com.medical.prescription.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataInitializer {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    @Bean
    public CommandLineRunner initDefaultUsers(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            // Seed or refresh Admin user with exact runtime PasswordEncoder
            userRepository.findByUsername("admin").ifPresentOrElse(
                    admin -> {
                        admin.setPasswordHash(passwordEncoder.encode("Admin@123"));
                        admin.setRole(AppUser.UserRole.ROLE_ADMIN);
                        userRepository.save(admin);
                        log.info("Default admin user password refreshed with runtime BCrypt.");
                    },
                    () -> {
                        AppUser admin = AppUser.builder()
                                .username("admin")
                                .passwordHash(passwordEncoder.encode("Admin@123"))
                                .role(AppUser.UserRole.ROLE_ADMIN)
                                .build();
                        userRepository.save(admin);
                        log.info("Default admin user created successfully.");
                    }
            );

            // Seed or refresh regular User
            userRepository.findByUsername("user").ifPresentOrElse(
                    user -> {
                        user.setPasswordHash(passwordEncoder.encode("User@123"));
                        user.setRole(AppUser.UserRole.ROLE_USER);
                        userRepository.save(user);
                        log.info("Default regular user password refreshed with runtime BCrypt.");
                    },
                    () -> {
                        AppUser user = AppUser.builder()
                                .username("user")
                                .passwordHash(passwordEncoder.encode("User@123"))
                                .role(AppUser.UserRole.ROLE_USER)
                                .build();
                        userRepository.save(user);
                        log.info("Default regular user created successfully.");
                    }
            );
        };
    }
}
