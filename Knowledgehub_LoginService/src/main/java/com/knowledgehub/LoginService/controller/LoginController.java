package com.knowledgehub.LoginService.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.knowledgehub.LoginService.DTO.User;
import com.knowledgehub.LoginService.Data.AuthResponse;
import com.knowledgehub.LoginService.Data.LoginRequest;
import com.knowledgehub.LoginService.jwt.JwtUtil;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.http.HttpStatus;

@RestController
public class LoginController {

    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private JwtUtil jwtUtil;

    @Value("${user.service.url}")
    private String userServiceUrl;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        // User service se user data fetch karna
        ResponseEntity<User> response = restTemplate.getForEntity(userServiceUrl + "/users/" + loginRequest.getUsername(), User.class);

        if (response.getStatusCode() == HttpStatus.OK) {
            User user = response.getBody();
            // Password match karna
            if (user != null && passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
                // Generate token and return response
                String token = jwtUtil.generateToken(user.getUsername());
                return ResponseEntity.ok(new AuthResponse(token));
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials");
            }
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not found");
        }
    }
}
