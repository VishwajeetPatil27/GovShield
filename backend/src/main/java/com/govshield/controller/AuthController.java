package com.govshield.controller;

import com.govshield.dto.CitizenLoginRequest;
import com.govshield.dto.LoginRequest;
import com.govshield.dto.LoginResponse;
import com.govshield.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*", maxAge = 3600)
public class AuthController {

    @Autowired
    private AuthService authService;

    /**
     * Login endpoint
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        LoginResponse response = authService.authenticate(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Citizen login using Aadhaar + generated UGID
     */
    @PostMapping("/citizen-login")
    public ResponseEntity<LoginResponse> citizenLogin(@RequestBody CitizenLoginRequest request) {
        LoginResponse response = authService.authenticateCitizen(request.getAadhaar(), request.getUgid());
        return ResponseEntity.ok(response);
    }

    /**
     * Validate token endpoint
     */
    @PostMapping("/validate")
    public ResponseEntity<String> validateToken(@RequestHeader("Authorization") String token) {
        String email = authService.validateToken(token.replace("Bearer ", ""));
        return ResponseEntity.ok("Token valid for: " + email);
    }
}
