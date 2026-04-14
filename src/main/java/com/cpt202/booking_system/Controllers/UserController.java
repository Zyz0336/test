package com.cpt202.booking_system.Controllers;

import com.cpt202.booking_system.Models.*;
import com.cpt202.booking_system.Repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired private CustomerRepository customerRepository;
    @Autowired private SpecialistRepository specialistRepository;
    @Autowired private AdminRepository adminRepository;
    @Autowired private TierApplicationRepository tierApplicationRepository;

    @GetMapping("/profile")
    public ResponseEntity<?> getProfile(@RequestParam String email, @RequestParam String role) {
        if ("Specialist".equals(role)) {
            return specialistRepository.findByEmail(email)
                .map(s -> ResponseEntity.ok(Map.of("code", 200, "data", s)))
                .orElse(ResponseEntity.status(404).body(Map.of("message", "User not found")));
        } else if ("Customer".equals(role)) {
            return customerRepository.findByEmail(email)
                .map(c -> ResponseEntity.ok(Map.of("code", 200, "data", c)))
                .orElse(ResponseEntity.status(404).body(Map.of("message", "User not found")));
        } else if ("Admin".equals(role)) {
            return adminRepository.findByEmail(email)
                .map(a -> ResponseEntity.ok(Map.of("code", 200, "data", a)))
                .orElse(ResponseEntity.status(404).body(Map.of("message", "User not found")));
        }
        return ResponseEntity.badRequest().body(Map.of("message", "Invalid role"));
    }

    @PutMapping("/profile")
    public ResponseEntity<?> updateProfile(@RequestBody Map<String, String> payload) {
        String email = payload.get("email");
        String role = payload.get("role");
        String nickname = payload.get("nickname");
        String bio = payload.get("bio");

        if ("Specialist".equals(role)) {
            specialistRepository.findByEmail(email).ifPresent(s -> {
                s.setNickname(nickname); s.setBio(bio); specialistRepository.save(s);
            });
        } else if ("Customer".equals(role)) {
            customerRepository.findByEmail(email).ifPresent(c -> {
                c.setNickname(nickname); c.setBio(bio); customerRepository.save(c);
            });
        } else if ("Admin".equals(role)) {
            adminRepository.findByEmail(email).ifPresent(a -> {
                a.setNickname(nickname); adminRepository.save(a);
            });
        }
        return ResponseEntity.ok(Map.of("code", 200, "message", "Profile updated successfully"));
    }

    @PostMapping("/apply-tier")
    public ResponseEntity<?> applyForTier(@RequestBody Map<String, String> payload) {
        String email = payload.get("email");
        String targetLevel = payload.get("targetLevel");
        String reason = payload.get("reason");

        Optional<TierApplication> lastApp = tierApplicationRepository.findFirstBySpecialistEmailOrderByCreatedAtDesc(email);
        if (lastApp.isPresent() && "Pending".equals(lastApp.get().getStatus())) {
            return ResponseEntity.badRequest().body(Map.of("message", "You already have a pending application."));
        }

        TierApplication newApp = new TierApplication();
        newApp.setSpecialistEmail(email);
        newApp.setTargetLevel(targetLevel);
        newApp.setReason(reason);
        newApp.setStatus("Pending");
        
        tierApplicationRepository.save(newApp);
        return ResponseEntity.ok(Map.of("code", 200, "message", "Application submitted successfully"));
    }

    // ==========================================
    // 🌟 核心修复点：避开 Map.of() 对 null 的限制
    // ==========================================
    @GetMapping("/promotion-status")
    public ResponseEntity<?> getPromotionStatus(@RequestParam String email) {
        Optional<TierApplication> lastApp = tierApplicationRepository.findFirstBySpecialistEmailOrderByCreatedAtDesc(email);
        
        // 使用传统的 HashMap，它允许 value 为 null
        Map<String, Object> response = new HashMap<>();
        response.put("code", 200);
        response.put("data", lastApp.orElse(null));
        
        return ResponseEntity.ok(response);
    }
}