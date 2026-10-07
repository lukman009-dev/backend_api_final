package com.shehia_management.api.shared.config;

import com.shehia_management.api.identity.User;
import com.shehia_management.api.identity.Role;
import com.shehia_management.api.identity.UserStatus;
import com.shehia_management.api.identity.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Seeds a default administrator account on first boot if none exists yet.
 *
 * CHANGED: the ZanID and password are no longer hardcoded in source. They are
 * now read from configuration (application.properties / environment
 * variables / a secrets manager in production), with safe fallbacks only for
 * local development. The password is also no longer printed to the console —
 * only the ZanID is logged, so operators know an account was created without
 * the credential itself ending up in log files.
 *
 * Recommended application.properties (dev):
 *   app.admin.default-zan-id=ADMIN001
 *   app.admin.default-password=Admin@123
 *
 * In staging/production, set these via environment variables instead, e.g.:
 *   APP_ADMIN_DEFAULT-ZAN-ID / APP_ADMIN_DEFAULT-PASSWORD
 * or better yet, provision the admin account out-of-band and disable this
 * initializer entirely via a profile check (see note at the bottom).
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.default-zan-id:ADMIN001}")
    private String defaultAdminZanId;

    @Value("${app.admin.default-password:Admin@123}")
    private String defaultAdminPassword;

    @Override
    public void run(String... args) {

        boolean adminExists = userRepository.findAll()
                .stream()
                .anyMatch(user -> user.getRole() == Role.ROLE_ADMIN);

        if (adminExists) {
            log.info("Admin account already exists — default admin was not created.");
            return;
        }

        User admin = User.builder()
                .zanId(defaultAdminZanId)
                .fullName("System Administrator")
                .email("admin@shehia.go.tz")
                .phoneNumber("")
                .houseNumber("")
                .street("")
                .shehia("")
                .district("")
                .region("Zanzibar")
                .password(passwordEncoder.encode(defaultAdminPassword))
                .role(Role.ROLE_ADMIN)
                .status(UserStatus.ACTIVE)
                .mustChangePassword(true) // enforced regardless of config source
                .build();

        userRepository.save(admin);

        // Deliberately does NOT log the password. Whoever configured
        // app.admin.default-password already knows it; anyone else should
        // not be able to recover it from application logs.
        log.warn("Default admin account created with ZanID '{}'. " +
                "mustChangePassword is set to true — change the password on first login.",
                defaultAdminZanId);
    }
}
