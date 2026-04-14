package com.cpt202.booking_system.Controllers;

import com.cpt202.booking_system.Models.*;
import com.cpt202.booking_system.Repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired private CustomerRepository customerRepository;
    @Autowired private SpecialistRepository specialistRepository;
    @Autowired private AdminRepository adminRepository;
    @Autowired private BookingRepository bookingRepository;
    @Autowired private TierApplicationRepository tierApplicationRepository;
    
    // 🌟 新增：注入时间片仓库，用于强制取消订单时释放专家时间
    @Autowired private ScheduleSlotRepository scheduleSlotRepository; 

    @GetMapping("/dashboard-stats")
    public ResponseEntity<?> getDashboardStats() {
        List<Booking> completed = bookingRepository.findByStatus("Completed");
        List<Specialist> specialists = specialistRepository.findByStatus("Active");
        List<Map<String, Object>> specData = new ArrayList<>();
        for (Specialist s : specialists) {
            double income = completed.stream().filter(b -> s.getEmail().equals(b.getSpecialistEmail())).mapToDouble(Booking::getFee).sum();
            specData.add(Map.of("name", s.getNickname() != null ? s.getNickname() : s.getEmail(), "totalIncome", income, "completedOrders", completed.stream().filter(b -> s.getEmail().equals(b.getSpecialistEmail())).count()));
        }
        return ResponseEntity.ok(Map.of("code", 200, "data", Map.of("totalBookings", completed.size(), "totalIncome", completed.stream().mapToDouble(Booking::getFee).sum(), "specialistData", specData)));
    }

    @GetMapping("/users")
    public ResponseEntity<?> getUsersByRole(@RequestParam String role) {
        if ("Customer".equals(role)) return ResponseEntity.ok(Map.of("code", 200, "data", customerRepository.findAll()));
        if ("Specialist".equals(role)) return ResponseEntity.ok(Map.of("code", 200, "data", specialistRepository.findAll()));
        return ResponseEntity.ok(Map.of("code", 200, "data", adminRepository.findAll()));
    }

    @PutMapping("/users/{id}/status")
    public ResponseEntity<?> updateStatus(@PathVariable Long id, @RequestParam String role, @RequestBody Map<String, String> payload) {
        String status = payload.get("status");
        if ("Customer".equals(role)) customerRepository.findById(id).ifPresent(u -> { u.setStatus(status); customerRepository.save(u); });
        else if ("Specialist".equals(role)) specialistRepository.findById(id).ifPresent(u -> { u.setStatus(status); specialistRepository.save(u); });
        else if ("Admin".equals(role)) adminRepository.findById(id).ifPresent(u -> { u.setStatus(status); adminRepository.save(u); });
        return ResponseEntity.ok(Map.of("message", "Status updated"));
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id, @RequestParam String role) {
        if ("Customer".equals(role)) customerRepository.deleteById(id);
        else if ("Specialist".equals(role)) specialistRepository.deleteById(id);
        else if ("Admin".equals(role)) adminRepository.deleteById(id);
        return ResponseEntity.ok(Map.of("message", "Deleted"));
    }

    @GetMapping("/bookings")
    public ResponseEntity<?> getAllBookings() {
        return ResponseEntity.ok(Map.of("code", 200, "data", bookingRepository.findAll()));
    }

    @GetMapping("/specialists/pending")
    public ResponseEntity<?> getPendingSpecialists() {
        return ResponseEntity.ok(Map.of("code", 200, "data", specialistRepository.findByStatus("Pending Approval")));
    }

    @PutMapping("/specialists/{id}/audit")
    public ResponseEntity<?> auditSpecialist(@PathVariable Long id, @RequestBody Map<String, String> payload) {
        String action = payload.get("action");
        Optional<Specialist> specOpt = specialistRepository.findById(id);
        if (specOpt.isPresent()) {
            Specialist spec = specOpt.get();
            spec.setStatus("Approve".equals(action) ? "Active" : "Rejected");
            specialistRepository.save(spec);
            return ResponseEntity.ok(Map.of("code", 200, "message", "Processed"));
        }
        return ResponseEntity.status(404).body(Map.of("message", "Not found"));
    }

    @GetMapping("/tier-applications/pending")
    public ResponseEntity<?> getPendingTierApps() {
        return ResponseEntity.ok(Map.of("code", 200, "data", tierApplicationRepository.findByStatus("Pending")));
    }

    @PutMapping("/tier-applications/{id}/review")
    @Transactional
    public ResponseEntity<?> reviewTierApp(@PathVariable Long id, @RequestBody Map<String, String> payload) {
        String status = payload.get("status");
        String comment = payload.get("comment");
        Optional<TierApplication> appOpt = tierApplicationRepository.findById(id);
        if (appOpt.isEmpty()) return ResponseEntity.status(404).body(Map.of("message", "Not found"));

        TierApplication app = appOpt.get();
        app.setStatus(status);
        app.setAdminComment(comment);
        tierApplicationRepository.save(app);

        if ("Approved".equals(status)) {
            specialistRepository.findByEmail(app.getSpecialistEmail()).ifPresent(spec -> {
                spec.setLevel(app.getTargetLevel());
                if ("Tier A".equals(spec.getLevel())) spec.setFee(1000.0);
                else if ("Tier B".equals(spec.getLevel())) spec.setFee(500.0);
                specialistRepository.save(spec);
            });
        }
        return ResponseEntity.ok(Map.of("code", 200, "message", "Review updated"));
    }

    @GetMapping("/income-summary")
    public ResponseEntity<?> getIncomeSummary() {
        List<Specialist> specialists = specialistRepository.findByStatus("Active");
        List<Map<String, Object>> result = new ArrayList<>();
        List<Booking> completedBookings = bookingRepository.findByStatus("Completed");
        for (Specialist spec : specialists) {
            double income = completedBookings.stream()
                .filter(b -> spec.getEmail().equals(b.getSpecialistEmail()))
                .mapToDouble(Booking::getFee).sum();
            result.add(Map.of("name", spec.getNickname() != null ? spec.getNickname() : spec.getEmail(), "level", spec.getLevel() != null ? spec.getLevel() : "Tier C", "totalIncome", income));
        }
        return ResponseEntity.ok(Map.of("code", 200, "data", result));
    }

    // =========================================================================
    // 🌟 以下是补充的 3 个高级接口，匹配你前端的豪华 UI 逻辑
    // =========================================================================

    // 1. 修改专家信息 (Tier, Fee, Expertise) -> 解决 AdminAccounts.vue 里的 Edit 报错
    @PutMapping("/specialists/{id}/info")
    public ResponseEntity<?> updateSpecialistInfo(@PathVariable Long id, @RequestBody Map<String, Object> payload) {
        Optional<Specialist> specOpt = specialistRepository.findById(id);
        if (specOpt.isPresent()) {
            Specialist spec = specOpt.get();
            if (payload.containsKey("level")) spec.setLevel(payload.get("level").toString());
            if (payload.containsKey("fee")) spec.setFee(Double.valueOf(payload.get("fee").toString()));
            if (payload.containsKey("expertise")) spec.setExpertise(payload.get("expertise").toString());
            specialistRepository.save(spec);
            return ResponseEntity.ok(Map.of("code", 200, "message", "Specialist info updated"));
        }
        return ResponseEntity.status(404).body(Map.of("message", "Specialist not found"));
    }

    // 2. 管理员强制取消订单 -> 解决 AdminBookings.vue 里的 Force Cancel 报错
    @PutMapping("/bookings/{id}/force-cancel")
    @Transactional
    public ResponseEntity<?> forceCancelBooking(@PathVariable Long id, @RequestBody Map<String, String> payload) {
        Optional<Booking> bookingOpt = bookingRepository.findById(id);
        if (bookingOpt.isPresent()) {
            Booking booking = bookingOpt.get();
            booking.setStatus("Cancelled");
            booking.setCancelReason("Admin Intervention: " + payload.get("cancelReason"));
            booking.setCancelSource("Admin");
            bookingRepository.save(booking);

            // 🌟 最关键：强行取消后，必须把专家这天的时间重新变为 "Available"
            if (booking.getSlotId() != null) {
                scheduleSlotRepository.findById(booking.getSlotId()).ifPresent(slot -> {
                    slot.setStatus("Available");
                    scheduleSlotRepository.save(slot);
                });
            }
            return ResponseEntity.ok(Map.of("code", 200, "message", "Force cancelled successfully"));
        }
        return ResponseEntity.status(404).body(Map.of("message", "Booking not found"));
    }

    // 3. 生成安全邀请码 -> 解决 AdminSpecialists.vue 里的 Generate Invite Code 报错
    @GetMapping("/generate-invite")
    public ResponseEntity<?> generateInviteCode() {
        // 生成一个带有 ADMIN- 前缀的 8 位随机大写邀请码
        String code = "ADMIN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return ResponseEntity.ok(Map.of("code", 200, "data", code));
    }
}