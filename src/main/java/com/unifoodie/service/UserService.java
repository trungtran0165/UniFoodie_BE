package com.unifoodie.service;

import com.unifoodie.model.User;
import com.unifoodie.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class UserService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String identifier) throws UsernameNotFoundException {
        Optional<User> userOpt = userRepository.findByUsername(identifier);
        if (userOpt.isEmpty()) {
            userOpt = userRepository.findByEmail(identifier);
        }
        User user = userOpt.orElseThrow(() -> new UsernameNotFoundException("User not found with username or email: " + identifier));
        return user;
    }

    public User save(User user) {
        return userRepository.save(user);
    }

    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    public long countUsers() {
        return userRepository.count();
    }

    public List<User> getAllUsers() { return userRepository.findAll(); }
    public Optional<User> getUserById(String id) { return userRepository.findById(id); }
    public User createUser(User user) { return userRepository.save(user); }
    public User updateUser(String id, User userDetails) {
        User user = userRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found with id: " + id));
        user.setFullName(userDetails.getFullName());
        user.setEmail(userDetails.getEmail());
        user.setPhoneNumber(userDetails.getPhoneNumber());
        user.setRole(userDetails.getRole());
        return userRepository.save(user);
    }
    public void deleteUser(String id) { userRepository.deleteById(id); }

    // Đăng ký
    public User register(User user) {
        // Set default role if not provided
        if (user.getRole() == null || user.getRole().trim().isEmpty()) {
            user.setRole("USER");
        }
        // Ensure role is uppercase
        user.setRole(user.getRole().toUpperCase());
        return userRepository.save(user);
    }

    // Đăng nhập
    public Optional<User> login(String username, String password) {
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isPresent() && userOpt.get().getPassword().equals(password)) {
            return userOpt;
        }
        return Optional.empty();
    }

    // Favorites methods
    public List<String> getFavourites(String userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        return user.getFavourites();
    }

    public User addToFavourites(String userId, String foodId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        List<String> favourites = user.getFavourites();
        if (!favourites.contains(foodId)) {
            favourites.add(foodId);
            user.setFavourites(favourites);
            return userRepository.save(user);
        }
        return user;
    }

    public void removeFromFavourites(String userId, String foodId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        List<String> favourites = user.getFavourites();
        favourites.remove(foodId);
        user.setFavourites(favourites);
        userRepository.save(user);
    }
}