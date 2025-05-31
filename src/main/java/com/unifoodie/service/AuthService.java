package com.unifoodie.service;

import com.unifoodie.dto.LoginRequest;
import com.unifoodie.dto.LoginResponse;
import com.unifoodie.dto.RegisterRequest;
import com.unifoodie.dto.RegisterResponse;
import com.unifoodie.model.User;
import com.unifoodie.repository.UserRepository;
import com.unifoodie.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthService {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public RegisterResponse register(RegisterRequest registerRequest) {
        if (userService.existsByUsername(registerRequest.getUsername())) {
            return new RegisterResponse("Username is already taken!", false);
        }

        if (userService.existsByEmail(registerRequest.getEmail())) {
             return new RegisterResponse("Email is already in use! Bạn có muốn đăng nhập không?", false);
        }

        User user = new User();
        user.setUsername(registerRequest.getUsername());
        user.setEmail(registerRequest.getEmail());
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        user.setRole("khách hàng");

        userRepository.save(user);

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

             LoginResponse loginResponse = new LoginResponse();
             loginResponse.setToken(jwt);
             loginResponse.setUsername(user.getUsername());

             return loginResponse;

        } catch (AuthenticationException e) {
            throw new RuntimeException("Invalid username/password supplied", e);
        } catch (RuntimeException e) {
             throw e;
        }
    }
}