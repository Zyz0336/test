package com.cpt202.booking_system.Controllers;

import com.cpt202.booking_system.Models.*;
import com.cpt202.booking_system.Repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired private CustomerRepository customerRepository;
    @Autowired private SpecialistRepository specialistRepository;
    @Autowired private AdminRepository adminRepository;

    private final String MASTER_ADMIN_CODE = "ADMIN-2026";

    // 检查邮箱是否被任何角色占用
    private boolean emailExists(String email) {
        return customerRepository.findByEmail(email).isPresent() ||
               specialistRepository.findByEmail(email).isPresent() ||
               adminRepository.findByEmail(email).isPresent();
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, String> payload) {
        String email = payload.get("email");
        String password = payload.get("password");
        String role = payload.get("role");

        if (customerRepository.findByEmail(email).isPresent() || 
            specialistRepository.findByEmail(email).isPresent() || 
            adminRepository.findByEmail(email).isPresent()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Email already exists"));
        }

        if ("Admin".equals(role)) {
            if (!MASTER_ADMIN_CODE.equals(payload.get("inviteCode"))) {
                return ResponseEntity.badRequest().body(Map.of("message", "Invalid Invitation Code"));
            }
            Admin admin = new Admin();
            admin.setEmail(email); admin.setPassword(password); admin.setRole(role); admin.setStatus("Active");
            adminRepository.save(admin);
        } else if ("Specialist".equals(role)) {
            Specialist spec = new Specialist();
            spec.setEmail(email); spec.setPassword(password); spec.setRole(role);
            spec.setStatus("Pending Approval");
            // 🌟 接收前端传来的专属字段
            spec.setExpertise(payload.get("expertise"));
            spec.setCertLink(payload.get("certLink"));
            specialistRepository.save(spec);
        } else {
            Customer cust = new Customer();
            cust.setEmail(email); cust.setPassword(password); cust.setRole(role); cust.setStatus("Active");
            customerRepository.save(cust);
        }
        return ResponseEntity.ok(Map.of("code", 200, "message", "Success"));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> payload) {
        String email = payload.get("email");
        String password = payload.get("password");
        String role = payload.get("role");

        Map<String, Object> data = new HashMap<>();

        if ("Admin".equals(role)) {
            return adminRepository.findByEmail(email)
                .filter(a -> a.getPassword().equals(password))
                .map(a -> {
                    data.put("token", "admin-tk-" + a.getId());
                    data.put("role", "Admin");
                    data.put("nickname", a.getNickname() != null ? a.getNickname() : "");
                    return ResponseEntity.ok(Map.of("code", 200, "data", data));
                }).orElse(ResponseEntity.status(401).body(Map.of("message", "Invalid Admin credentials")));
        } 
        else if ("Specialist".equals(role)) {
            return specialistRepository.findByEmail(email)
                .filter(s -> s.getPassword().equals(password))
                .map(s -> {
                    if ("Pending Approval".equals(s.getStatus())) {
                        return ResponseEntity.status(403).body(Map.of("message", "Specialist review pending."));
                    }
                    data.put("token", "spec-tk-" + s.getId());
                    data.put("role", "Specialist");
                    data.put("nickname", s.getNickname() != null ? s.getNickname() : "");
                    return ResponseEntity.ok(Map.of("code", 200, "data", data));
                }).orElse(ResponseEntity.status(401).body(Map.of("message", "Invalid Specialist credentials")));
        } 
        else {
            return customerRepository.findByEmail(email)
                .filter(c -> c.getPassword().equals(password))
                .map(c -> {
                    data.put("token", "cust-tk-" + c.getId());
                    data.put("role", "Customer");
                    data.put("nickname", c.getNickname() != null ? c.getNickname() : "");
                    return ResponseEntity.ok(Map.of("code", 200, "data", data));
                }).orElse(ResponseEntity.status(401).body(Map.of("message", "Invalid Customer credentials")));
        }
    }
}