
package com.ecommerce.backend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.ecommerce.backend.entity.Role;
import com.ecommerce.backend.entity.User;
import com.ecommerce.backend.repository.RoleRepository;
import com.ecommerce.backend.repository.UserRepository;

@Component
public class AdminUserInitializer implements ApplicationRunner {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${ADMIN_EMAIL:}")
    private String adminEmail;

    @Value("${ADMIN_FULL_NAME:}")
    private String adminFullName;

    @Value("${ADMIN_PASSWORD:}")
    private String adminPassword;

    public AdminUserInitializer(
            UserRepository userRepository,
            RoleRepository roleRepository,
            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        // No crear el administrador si no se configuraron las variables.
        if (adminEmail.isBlank()
                || adminFullName.isBlank()
                || adminPassword.isBlank()) {
            return;
        }

        // Si ya existe un administrador, no crear otro.
        if (userRepository.existsByRoles_Name("ADMIN")) {
            return;
        }

        if (userRepository.existsByEmailIgnoreCase(adminEmail)) {
            throw new IllegalStateException(
                "El correo configurado para el administrador ya existe, "
                + "pero no hay un administrador registrado. "
                + "Verifica los roles del usuario."
            );
        }

        if (adminPassword.length() < 12) {
            throw new IllegalStateException(
                "La contraseña inicial del administrador debe tener "
                + "al menos 12 caracteres."
            );
        }

        Role adminRole = roleRepository.findByName("ADMIN")
            .orElseThrow(() -> new IllegalStateException(
                "No existe el rol ADMIN en la base de datos."
            ));

        User admin = new User(
            adminFullName,
            adminEmail.trim(),
            passwordEncoder.encode(adminPassword)
        );

        admin.getRoles().add(adminRole);
        userRepository.save(admin);
    }
}
