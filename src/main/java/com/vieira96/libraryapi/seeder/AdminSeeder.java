package com.vieira96.libraryapi.seeder;

import com.vieira96.libraryapi.model.role.RoleModel;
import com.vieira96.libraryapi.model.role.RoleName;
import com.vieira96.libraryapi.model.user.UserModel;
import com.vieira96.libraryapi.repository.role.RoleRepository;
import com.vieira96.libraryapi.repository.user.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
@Slf4j
public class AdminSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${admin.name:}")
    private String adminName;

    @Value("${admin.email:}")
    private String adminEmail;

    @Value("${admin.password:}")
    private String adminPassword;

    public AdminSeeder(
            UserRepository userRepository,
            RoleRepository roleRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String @NonNull ... args) {
        if (adminEmail == null || adminEmail.isBlank()) {
            return;
        }

        String normalizedEmail = adminEmail.trim().toLowerCase(Locale.ROOT);

        if (userRepository.existsByEmailIgnoreCase(normalizedEmail)) {
            log.info("Admin já existe, ignorando seed.");
            return;
        }

        RoleModel adminRole = roleRepository.findByName(RoleName.ADMIN)
                .orElseThrow(() -> new IllegalStateException("Role ADMIN não encontrada."));

        UserModel admin = new UserModel();
        admin.setName(adminName != null ? adminName.trim() : "Admin");
        admin.setEmail(normalizedEmail);
        admin.setPassword(passwordEncoder.encode(adminPassword));
        admin.getRoles().add(adminRole);

        userRepository.save(admin);
        log.info("Admin criado: {}", normalizedEmail);
    }
}
