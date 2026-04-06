package com.govshield.service;

import com.govshield.dto.LoginRequest;
import com.govshield.dto.LoginResponse;
import com.govshield.exception.CustomException;
import com.govshield.model.Citizen;
import com.govshield.model.GovEmployee;
import com.govshield.repository.CitizenRepository;
import com.govshield.repository.GovEmployeeRepository;
import com.govshield.util.UgidGenerator;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import java.util.Date;
import javax.crypto.SecretKey;

@Service
public class AuthService {

    private final PasswordEncoder passwordEncoder;
    private final GovEmployeeRepository govEmployeeRepository;
    private final CitizenRepository citizenRepository;

    @Value("${jwt.secret:my-secret-key-for-govshield-authentication-system}")
    private String jwtSecret;

    @Value("${jwt.expiration:86400000}") // 24 hours default
    private long jwtExpiration;

    @Autowired
    public AuthService(PasswordEncoder passwordEncoder,
                       GovEmployeeRepository govEmployeeRepository,
                       CitizenRepository citizenRepository) {
        this.passwordEncoder = passwordEncoder;
        this.govEmployeeRepository = govEmployeeRepository;
        this.citizenRepository = citizenRepository;
    }

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    /**
     * Authenticate user and generate JWT token
     */
    public LoginResponse authenticate(LoginRequest request) {
        GovEmployee employee = govEmployeeRepository.findByEmail(request.getEmail())
            .orElseThrow(() -> new CustomException("Invalid email or password", "INVALID_CREDENTIALS", 401));

        logger.debug("Authenticating user: {}", employee.getEmail());
        if (!employee.getIsActive()) {
            throw new CustomException("User account is inactive", "ACCOUNT_INACTIVE", 403);
        }

        String pwd = request.getPassword() == null ? null : request.getPassword();
        logger.debug("Received password length: {}", pwd == null ? 0 : pwd.length());

        boolean matches = passwordEncoder.matches(pwd, employee.getPasswordHash());
        logger.debug("Password matches: {} for user {}", matches, employee.getEmail());
    
        if (!matches) {
            throw new CustomException("Invalid email or password", "INVALID_CREDENTIALS", 401);
        }

        String token = generateToken(employee);

        return new LoginResponse(token, employee.getEmail(), employee.getRole(), jwtExpiration, employee.getId(), null);
    }

    public LoginResponse authenticateCitizen(String aadhaar, String ugid) {
        String normalizedAadhaar = normalizeAadhaar(aadhaar);
        String normalizedUgid = normalizeUgid(ugid);

        Citizen citizen = citizenRepository.findByAadhaar(normalizedAadhaar)
            .orElseThrow(() -> new CustomException("Citizen not found for Aadhaar", "CITIZEN_NOT_FOUND", 404));

        if (citizen.getUgid() == null || citizen.getUgid().isBlank()) {
            citizen.setUgid(UgidGenerator.generateFromAadhaar(normalizedAadhaar));
            citizen = citizenRepository.save(citizen);
        }

        if (!citizen.getUgid().trim().equalsIgnoreCase(normalizedUgid)) {
            throw new CustomException("Invalid Aadhaar/UGID combination", "INVALID_CITIZEN_CREDENTIALS", 401);
        }
        if (!citizen.getIsActive()) {
            throw new CustomException("Citizen account is inactive", "ACCOUNT_INACTIVE", 403);
        }

        String token = generateCitizenToken(citizen);
        return new LoginResponse(token, citizen.getEmail(), "CITIZEN", jwtExpiration, citizen.getId(), citizen.getUgid());
    }

    /**
     * Generate JWT token for authenticated user
     */
    private String generateToken(GovEmployee employee) {
        SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes());

        return Jwts.builder()
            .setSubject(employee.getEmail())
            .claim("role", employee.getRole())
            .claim("employeeId", employee.getId())
            .setIssuedAt(new Date())
            .setExpiration(new Date(System.currentTimeMillis() + jwtExpiration))
            .signWith(key, SignatureAlgorithm.HS256)
            .compact();
    }

    private String generateCitizenToken(Citizen citizen) {
        SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes());

        return Jwts.builder()
            .setSubject(citizen.getAadhaar())
            .claim("role", "CITIZEN")
            .claim("citizenId", citizen.getId())
            .claim("ugid", citizen.getUgid())
            .setIssuedAt(new Date())
            .setExpiration(new Date(System.currentTimeMillis() + jwtExpiration))
            .signWith(key, SignatureAlgorithm.HS256)
            .compact();
    }

    /**
     * Validate JWT token
     */
    public String validateToken(String token) {
        try {
            SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes());
            return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
        } catch (Exception e) {
            throw new CustomException("Invalid or expired token", "INVALID_TOKEN", 401);
        }
    }

    private String normalizeAadhaar(String aadhaar) {
        if (aadhaar == null) {
            return "";
        }
        return aadhaar.replaceAll("\\s+", "").trim();
    }

    private String normalizeUgid(String ugid) {
        if (ugid == null) {
            return "";
        }
        return ugid.trim();
    }
}
