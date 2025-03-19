package com.unifoodie.unifoodie.service;

import com.unifoodie.unifoodie.dto.LoginRequest;
import com.unifoodie.unifoodie.dto.LoginResponse;
import com.unifoodie.unifoodie.dto.RegisterRequest;
import com.unifoodie.unifoodie.dto.RegisterResponse;
import com.unifoodie.unifoodie.model.User;
import com.unifoodie.unifoodie.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class AuthService {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public RegisterResponse register(RegisterRequest registerRequest) {
        // Check if username or email already exists
        if (userService.existsByUsername(registerRequest.getUsername())) {
            return new RegisterResponse("Username is already taken!", false);
        }

        if (userService.existsByEmail(registerRequest.getEmail())) {
            return new RegisterResponse("Email is already in use!", false);
        }

        // Create new user
        User user = new User();
        user.setName(registerRequest.getName());
        user.setUsername(registerRequest.getUsername());
        user.setEmail(registerRequest.getEmail());
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        user.setRoles(Collections.singletonList("USER"));

        userService.save(user);

        return new RegisterResponse("User registered successfully!", true);
    }

    public LoginResponse login(LoginRequest loginRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getUsername(),
                            loginRequest.getPassword()));

            User user = (User) authentication.getPrincipal();
            String jwt = jwtUtil.generateToken(user);

            return new LoginResponse(jwt, user.getUsername(), user.getName());
        } catch (AuthenticationException e) {
            throw new RuntimeException("Invalid username/password supplied", e);
        }
    }
}
