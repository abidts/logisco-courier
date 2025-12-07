package com.logisco.controller;

import com.logisco.model.User;
import com.logisco.service.UserService;
import com.logisco.util.JwtUtil;
import com.logisco.service.OtpService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private OtpService otpService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody User user) {
        try {
            if (userService.existsByUsername(user.getUsername())) {
                return ResponseEntity.badRequest()
                        .body(Map.of("message", "Username already exists"));
            }

            if (user.getRole() == null) {
                user.setRole(User.Role.USER);
            }

            User createdUser = userService.createUser(user);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "User registered successfully");
            response.put("userId", createdUser.getId());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Error: " + e.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> credentials) {
        try {
            String username = credentials.get("username");
            String password = credentials.get("password");

            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, password)
            );

            User user = userService.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            String token = jwtUtil.generateToken(username, user.getRole().name());

            Map<String, Object> response = new HashMap<>();
            response.put("token", token);
            response.put("username", user.getUsername());
            response.put("role", user.getRole());
            response.put("userId", user.getId());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Invalid credentials"));
        }
    }

    @PostMapping("/request-otp")
    public ResponseEntity<?> requestOtp(@RequestBody Map<String, String> payload) {
        try {
            String phone = payload.get("phone");
            String fullName = payload.getOrDefault("fullName", "");
            if (phone == null || !phone.matches("^[0-9]{10}$")) {
                return ResponseEntity.badRequest().body(Map.of("message", "Invalid phone number"));
            }
            otpService.generateOtp(phone, fullName);
            return ResponseEntity.ok(Map.of("message", "OTP sent"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Error: " + e.getMessage()));
        }
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyOtp(@RequestBody Map<String, String> payload) {
        try {
            String phone = payload.get("phone");
            String otp = payload.get("otp");
            String fullName = payload.getOrDefault("fullName", otpService.getFullName(phone));
            if (phone == null || otp == null) {
                return ResponseEntity.badRequest().body(Map.of("message", "Phone and OTP required"));
            }
            boolean ok = otpService.verifyOtp(phone, otp);
            if (!ok) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("message", "Invalid or expired OTP"));
            }

            // Create or fetch user using phone as username
            User user = userService.findByUsername(phone).orElse(null);
            if (user == null) {
                user = new User();
                user.setUsername(phone);
                user.setPassword(phone + "!otp");
                user.setPhoneNumber(phone);
                user.setFullName(fullName);
                user.setEmail(phone + "@logisco.local");
                user.setRole(User.Role.USER);
                user = userService.createUser(user);
            }

            String token = jwtUtil.generateToken(user.getUsername(), user.getRole().name());
            Map<String, Object> response = new HashMap<>();
            response.put("token", token);
            response.put("userId", user.getId());
            response.put("username", user.getUsername());
            response.put("role", user.getRole());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Error: " + e.getMessage()));
        }
    }
}
