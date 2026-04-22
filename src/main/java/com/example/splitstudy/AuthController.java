package com.example.splitstudy;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private FirestoreService firestoreService;

    @PostMapping("/register")
    public Map<String, Object> register(@RequestBody User user) {
        Map<String, Object> response = new HashMap<>();
        try {
            boolean success = firestoreService.registerUser(user.getUsername(), user.getPassword());
            response.put("success", success);
            response.put("message", success ? "User registered successfully" : "User already exists");
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error: " + e.getMessage());
        }
        return response;
    }

    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody User user) {
        Map<String, Object> response = new HashMap<>();
        try {
            boolean success = firestoreService.loginUser(user.getUsername(), user.getPassword());
            response.put("success", success);
            response.put("message", success ? "Login successful" : "Invalid credentials");
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error: " + e.getMessage());
        }
        return response;
    }
}
