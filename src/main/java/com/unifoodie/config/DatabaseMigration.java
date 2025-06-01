package com.unifoodie.config;

import com.unifoodie.model.User;
import com.unifoodie.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DatabaseMigration implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Override
    public void run(String... args) throws Exception {
        // Add profilePicture field to all existing users
        userRepository.findAll().forEach(user -> {
            if (user.getProfilePicture() == null) {
                user.setProfilePicture("https://cdn-icons-png.flaticon.com/512/706/706830.png");
                userRepository.save(user);
            }
        });
    }
} 