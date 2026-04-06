package com.govshield.controller;

import com.govshield.model.Scheme;
import com.govshield.service.SchemeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/schemes")
@CrossOrigin(origins = "*", maxAge = 3600)
public class SchemeController {

    @Autowired
    private SchemeService schemeService;

    /**
     * Create a new scheme
     */
    @PostMapping
    public ResponseEntity<Scheme> createScheme(@RequestBody Scheme scheme) {
        Scheme createdScheme = schemeService.createScheme(scheme);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdScheme);
    }

    /**
     * Get scheme by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<Scheme> getSchemeById(@PathVariable Long id) {
        Scheme scheme = schemeService.getSchemeById(id);
        return ResponseEntity.ok(scheme);
    }

    /**
     * Get scheme by code
     */
    @GetMapping("/code/{schemeCode}")
    public ResponseEntity<Scheme> getSchemeByCode(@PathVariable String schemeCode) {
        Scheme scheme = schemeService.getSchemeByCode(schemeCode);
        return ResponseEntity.ok(scheme);
    }

    /**
     * Get all active schemes
     */
    @GetMapping("/active/all")
    public ResponseEntity<List<Scheme>> getAllActiveSchemes() {
        List<Scheme> schemes = schemeService.getAllActiveSchemes();
        return ResponseEntity.ok(schemes);
    }

    /**
     * Get schemes by sector
     */
    @GetMapping("/sector/{sector}")
    public ResponseEntity<List<Scheme>> getSchemesBySector(@PathVariable String sector) {
        List<Scheme> schemes = schemeService.getSchemesBySector(sector);
        return ResponseEntity.ok(schemes);
    }

    /**
     * Update scheme
     */
    @PutMapping("/{id}")
    public ResponseEntity<Scheme> updateScheme(@PathVariable Long id, @RequestBody Scheme scheme) {
        Scheme updatedScheme = schemeService.updateScheme(id, scheme);
        return ResponseEntity.ok(updatedScheme);
    }

    /**
     * Deactivate scheme
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deactivateScheme(@PathVariable Long id) {
        schemeService.deactivateScheme(id);
        return ResponseEntity.ok("Scheme deactivated successfully");
    }
}
