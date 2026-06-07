package com.jack.springaiopenrouter.bootstrap;

import com.jack.springaiopenrouter.entity.UserEntity;
import com.jack.springaiopenrouter.entity.UserRole;
import com.jack.springaiopenrouter.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final boolean seedEnabled;

    public DataSeeder(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            @Value("${app.seed.enabled:true}") boolean seedEnabled
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.seedEnabled = seedEnabled;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (!seedEnabled) {
            return;
        }

        seedUsers();
    }

    private void seedUsers() {
        if (userRepository.count() > 0) {
            return;
        }

        userRepository.saveAll(List.of(
                new UserEntity("jack@example.com", "Jack Son", passwordEncoder.encode("Password123"), UserRole.ADMIN),
                new UserEntity("demo@example.com", "Demo User", passwordEncoder.encode("Password123"), UserRole.USER)
        ));
    }
}
