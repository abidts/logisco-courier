package com.logisco.config;

import com.logisco.model.User;
import com.logisco.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class AdminInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${admin.default.username:admin}")
    private String defaultAdminUsername;

    @Value("${admin.default.password:admin123}")
    private String defaultAdminPassword;

    @Value("${admin.default.email:admin@logisco.local}")
    private String defaultAdminEmail;

    public AdminInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        try {
            boolean exists = userRepository.existsByUsername(defaultAdminUsername);
            if (!exists) {
                User admin = new User();
                admin.setUsername(defaultAdminUsername);
                admin.setPassword(passwordEncoder.encode(defaultAdminPassword));
                admin.setEmail(defaultAdminEmail);
                admin.setFullName("System Administrator");
                admin.setRole(User.Role.ADMIN);
                admin.setActive(true);
                userRepository.save(admin);
            }
        } catch (Exception ignored) {}
    }
}
