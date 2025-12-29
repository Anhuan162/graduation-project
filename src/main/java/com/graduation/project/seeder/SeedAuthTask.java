package com.graduation.project.seeder;

import com.graduation.project.auth.constant.PredefinedRole;
import com.graduation.project.common.constant.Provider;
import com.graduation.project.common.entity.Role;
import com.graduation.project.common.entity.User;
import com.graduation.project.auth.repository.RoleRepository;
import com.graduation.project.auth.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.datafaker.Faker;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
public class SeedAuthTask {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.seed.admin-password:Admin@123}")
    private String adminPassword;

    private final Faker faker = new Faker();

    @Transactional(rollbackFor = Exception.class)
    public void run(SeedProperties props) {
        seedRoles();
        seedAdmin();
        seedUsers(props.getUsers());
    }

    private void seedRoles() {

        upsertRole(PredefinedRole.ADMIN_ROLE, "Administrator");
        upsertRole(PredefinedRole.USER_ROLE, "Standard user");
        upsertRole(PredefinedRole.MANAGER_CLUB_ROLE, "Club manager");
        log.info(" - Seeded roles");
    }

    private void upsertRole(String name, String description) {
        if (roleRepository.existsById(name))
            return;

        try {
            Role role = new Role();
            role.setName(name);
            role.setDescription(description);
            role.setPermissions(new HashSet<>());
            roleRepository.save(role);
        } catch (DataIntegrityViolationException e) {
            log.debug("Role {} already exists", name);
        }
    }

    @Transactional
    private void seedAdmin() {
        String adminEmail = "admin@grad.local";
        if (userRepository.findByEmail(adminEmail).isPresent())
            return;

        Role adminRole = roleRepository.findById(PredefinedRole.ADMIN_ROLE).orElseThrow();

        User admin = new User();
        admin.setEmail(adminEmail);
        admin.setFullName("Super Admin");
        admin.setEnabled(true);
        admin.setAvatarUrl("https://i.pravatar.cc/300?img=1");
        admin.setPhone("0900000000");
        admin.setProvider(Provider.LOCAL);
        admin.setStudentCode("ADMIN0001");
        admin.setClassCode("ADMIN-CLASS");
        admin.setRegistrationDate(LocalDateTime.now());
        admin.setPassword(passwordEncoder.encode(adminPassword));
        admin.setRoles(Set.of(adminRole));

        userRepository.save(admin);
        log.info(" - Seeded admin: {}", adminEmail);
    }

    private void seedUsers(int count) {
        long current = userRepository.count();
        if (current >= count) {
            log.info(" - Users already seeded: {}", current);
            return;
        }

        Role userRole = roleRepository.findById(PredefinedRole.USER_ROLE).orElseThrow();
        String hashed = passwordEncoder.encode("123456");

        int toCreate = (int) Math.max(0, count - current);
        int maxRetries = 3;
        for (int i = 0; i < toCreate; i++) {
            int retryCount = 0;
            try {
                User u = new User();
                // Ensure unique email by appending timestamp and counter
                u.setEmail("user." + UUID.randomUUID() + "@" + faker.internet().domainName());
                u.setFullName(faker.name().fullName());
                u.setEnabled(true);
                u.setAvatarUrl("https://i.pravatar.cc/300?u=" + UUID.randomUUID());
                u.setPhone(faker.phoneNumber().cellPhone());
                u.setProvider(Provider.LOCAL);
                u.setStudentCode("S" + faker.number().digits(7));
                u.setClassCode("K" + faker.number().numberBetween(60, 68) + "-" + faker.number().digits(2));
                u.setRegistrationDate(LocalDateTime.now().minusDays(faker.number().numberBetween(1, 120)));
                u.setPassword(hashed);
                u.setRoles(Set.of(userRole));

                userRepository.save(u);
            } catch (DataIntegrityViolationException e) {
                if (retryCount++ < maxRetries) {
                    log.warn("Failed to create user due to duplicate data, retrying... (attempt {}/{})", retryCount,
                            maxRetries);
                    i--;
                } else {
                    log.error("Failed to create user after {} retries, skipping", maxRetries);
                }
            }
        }

        log.info(" - Seeded {} users", toCreate);
    }
}
