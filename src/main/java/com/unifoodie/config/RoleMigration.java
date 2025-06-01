package com.unifoodie.config;

import com.unifoodie.model.User;
import com.unifoodie.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class RoleMigration implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(RoleMigration.class);

    @Autowired
    private UserRepository userRepository;

    @Override
    public void run(String... args) throws Exception {
        logger.info("Starting multiple role migrations...");

        // Migration: "quản lý cửa hàng" -> "ADMIN"
        List<User> adminUsersToMigrate = userRepository.findAll().stream()
                .filter(user -> "quản lý cửa hàng".equals(user.getRole()))
                .toList(); // Use toList() for Java 16+ or collect(Collectors.toList()) for older

        if (!adminUsersToMigrate.isEmpty()) {
            logger.info("Found {} users with role 'quản lý cửa hàng'. Migrating to 'ADMIN'", adminUsersToMigrate.size());
            adminUsersToMigrate.forEach(user -> {
                user.setRole("ADMIN");
                userRepository.save(user);
                logger.debug("Updated role for user: {} to ADMIN", user.getUsername());
            });
            logger.info("Migration for 'quản lý cửa hàng' finished. Updated {} users.", adminUsersToMigrate.size());
        } else {
             logger.info("No users found with role 'quản lý cửa hàng' to migrate.");
        }


        // Migration: "khách hàng" -> "USER"
        List<User> regularUsersToMigrate = userRepository.findAll().stream()
                .filter(user -> "khách hàng".equals(user.getRole()))
                .toList();

        if (!regularUsersToMigrate.isEmpty()) {
            logger.info("Found {} users with role 'khách hàng'. Migrating to 'USER'", regularUsersToMigrate.size());
            regularUsersToMigrate.forEach(user -> {
                user.setRole("USER");
                userRepository.save(user);
                logger.debug("Updated role for user: {} to USER", user.getUsername());
            });
             logger.info("Migration for 'khách hàng' finished. Updated {} users.", regularUsersToMigrate.size());
        } else {
            logger.info("No users found with role 'khách hàng' to migrate.");
        }

        logger.info("All role migrations finished.");
    }
} 