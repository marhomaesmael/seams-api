package com.seams.backend.infrastructure.config;

import com.seams.backend.core.model.Role;
import com.seams.backend.core.model.User;
import com.seams.backend.core.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        if (userRepository.findByUsername("supervisor").isEmpty()) {
            User supervisor = User.builder()
                    .username("supervisor")
                    .password(passwordEncoder.encode("supervisor"))
                    .displayName("System Supervisor")
                    .role(Role.SUPERVISOR)
                    .mustChangePassword(false)
                    .build();
            userRepository.save(supervisor);
            System.out.println("Default supervisor user created: supervisor/supervisor");
        }

        if (userRepository.findByUsername("admin").isEmpty()) {
            User admin = User.builder()
                    .username("admin")
                    .password(passwordEncoder.encode("admin"))
                    .displayName("admin")
                    .role(Role.ADMIN)
                    .mustChangePassword(false)
                    .build();
            userRepository.save(admin);
            System.out.println("Default admin user created: admin/admin");
        }
    }
}
