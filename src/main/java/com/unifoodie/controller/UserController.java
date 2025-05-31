package com.unifoodie.controller;

import com.unifoodie.model.User;
import com.unifoodie.service.UserService;
import com.unifoodie.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
public class UserController {
    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private AuthenticationManager authenticationManager;

    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable String id) {
        return userService.getUserById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<User> createUser(@RequestBody User user) {
        return ResponseEntity.ok(userService.createUser(user));
    }

    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(@PathVariable String id, @RequestBody User user) {
        return ResponseEntity.ok(userService.updateUser(id, user));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable String id) {
        userService.deleteUser(id);
        return ResponseEntity.ok().build();
    }

    // Đăng ký
    @PostMapping("/register")
    public ResponseEntity<User> register(@RequestBody User user) {
        return ResponseEntity.ok(userService.register(user));
    }

    // Đăng nhập
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> loginRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.get("username"), loginRequest.get("password"))
            );
            // If authentication is successful, generate JWT and return user info
            User user = (User) authentication.getPrincipal();
            String token = jwtUtil.generateToken(user);
            return ResponseEntity.ok(java.util.Map.of(
                "token", token,
                "user", user
            ));
        } catch (AuthenticationException e) {
            // If authentication fails, return 401 Unauthorized
            return ResponseEntity.status(401).body(java.util.Map.of("error", "Invalid username or password"));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        // Với JWT, logout chỉ là xóa token phía client
        return ResponseEntity.ok("Logged out");
    }

    // Favorites endpoints
    @GetMapping("/{userId}/favourites")
    public ResponseEntity<?> getFavourites(@PathVariable String userId) {
        try {
            System.out.println("Fetching favorites for user: " + userId);
            var favorites = userService.getFavourites(userId);
            System.out.println("Found " + favorites.size() + " favorites");
            return ResponseEntity.ok(favorites);
        } catch (Exception e) {
            System.err.println("Error fetching favorites: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/{userId}/favourites")
    public ResponseEntity<?> addToFavourites(@PathVariable String userId, @RequestBody Map<String, String> request) {
        try {
            String foodId = request.get("foodId");
            System.out.println("Adding food " + foodId + " to favorites for user: " + userId);
            if (foodId == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Food ID is required"));
            }
            var result = userService.addToFavourites(userId, foodId);
            System.out.println("Successfully added to favorites");
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            System.err.println("Error adding to favorites: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/{userId}/favourites/{foodId}")
    public ResponseEntity<?> removeFromFavourites(@PathVariable String userId, @PathVariable String foodId) {
        try {
            System.out.println("Removing food " + foodId + " from favorites for user: " + userId);
            userService.removeFromFavourites(userId, foodId);
            System.out.println("Successfully removed from favorites");
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            System.err.println("Error removing from favorites: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
} 