package com.cpt202.booking_system.Controllers;

import com.cpt202.booking_system.Models.Booking;
import com.cpt202.booking_system.Models.ScheduleSlot;
import com.cpt202.booking_system.Repositories.BookingRepository;
import com.cpt202.booking_system.Repositories.ScheduleSlotRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/specialist")

public class SpecialistController {

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private ScheduleSlotRepository scheduleSlotRepository;

    // ==========================================
    // PBI 10: 专家预约管理接口 (Booking Management)
    // ==========================================

    // 1. 获取分配给该专家的所有订单
    @GetMapping("/bookings")
    public ResponseEntity<?> getSpecialistBookings(@RequestParam String email) {
        // 由于我们在 Booking 表里没有存 specialistEmail，我们暂时用 specialistName 模糊查询
        // (在实际项目中，Booking 应该保存专家的 email，这里为了快速联调我们直接拉取所有订单并在前端过滤)
        List<Booking> allBookings = bookingRepository.findAll();
        
        // 过滤出属于当前专家的订单
        List<Booking> myBookings = allBookings.stream()
                .filter(b -> b.getSpecialistEmail() != null && b.getSpecialistEmail().equals(email))
                .toList();

        return ResponseEntity.ok(Map.of("code", 200, "data", myBookings));
    }

    // 2. 专家确认订单
    @PutMapping("/bookings/{id}/confirm")
    public ResponseEntity<?> confirmBooking(@PathVariable Long id) {
        return changeBookingStatus(id, "Confirmed");
    }

    // 3. 专家拒绝订单
    @PutMapping("/bookings/{id}/reject")
    public ResponseEntity<?> rejectBooking(@PathVariable Long id) {
        return changeBookingStatus(id, "Cancelled");
    }

    // 4. 专家标记订单已完成
    @PutMapping("/bookings/{id}/complete")
    public ResponseEntity<?> completeBooking(@PathVariable Long id) {
        return changeBookingStatus(id, "Completed");
    }

    // 内部通用方法：修改订单状态
    private ResponseEntity<?> changeBookingStatus(Long id, String newStatus) {
        Optional<Booking> bookingOpt = bookingRepository.findById(id);
        if (bookingOpt.isPresent()) {
            Booking booking = bookingOpt.get();
            booking.setStatus(newStatus);
            bookingRepository.save(booking);
            return ResponseEntity.ok(Map.of("code", 200, "message", "Status updated to " + newStatus));
        }
        return ResponseEntity.status(404).body(Map.of("message", "Booking not found"));
    }

    // ==========================================
    // PBI 11: 专家排班时间表接口 (Slot Management)
    // ==========================================

    // 1. 获取专家的所有排班时间段
    @GetMapping("/slots")
    public ResponseEntity<?> getMySlots(@RequestParam String email) {
        List<ScheduleSlot> mySlots = scheduleSlotRepository.findBySpecialistEmail(email);
        return ResponseEntity.ok(Map.of("code", 200, "data", mySlots));
    }

    // 2. 发布新的空闲时间段
    @PostMapping("/slots/create")
    public ResponseEntity<?> createSlot(@RequestBody ScheduleSlot slot) {
        // 校验：是否在同一时间重复排班了？
        boolean isOverlap = scheduleSlotRepository.existsBySpecialistEmailAndDateAndStartTime(
                slot.getSpecialistEmail(), slot.getDate(), slot.getStartTime());
        
        if (isOverlap) {
            return ResponseEntity.badRequest().body(Map.of("message", "Time conflict! You already have a slot arranged during this period."));
        }

        slot.setStatus("Available"); // 新发布的默认可预约
        ScheduleSlot savedSlot = scheduleSlotRepository.save(slot);
        return ResponseEntity.ok(Map.of("code", 200, "message", "Published successfully!", "data", savedSlot));
    }

    // 3. 删除未被预约的空闲时间段
    @DeleteMapping("/slots/{id}")
    public ResponseEntity<?> deleteSlot(@PathVariable Long id) {
        Optional<ScheduleSlot> slotOpt = scheduleSlotRepository.findById(id);
        if (slotOpt.isPresent()) {
            ScheduleSlot slot = slotOpt.get();
            if ("Booked".equals(slot.getStatus())) {
                return ResponseEntity.badRequest().body(Map.of("message", "Cannot delete a booked slot."));
            }
            scheduleSlotRepository.deleteById(id);
            return ResponseEntity.ok(Map.of("code", 200, "message", "Slot deleted."));
        }
        return ResponseEntity.status(404).body(Map.of("message", "Slot not found"));
    }
    // 5. 专家带原因取消订单 (PBI 12 专属核心接口)
    @PutMapping("/bookings/{id}/cancel-with-reason")
    public ResponseEntity<?> cancelBookingWithReason(@PathVariable Long id, @RequestBody Map<String, String> payload) {
        String reason = payload.get("cancelReason");
        
        if (reason == null || reason.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "取消原因不能为空，请填写"));
        }

        Optional<Booking> bookingOpt = bookingRepository.findById(id);
        if (bookingOpt.isPresent()) {
            Booking booking = bookingOpt.get();
            booking.setStatus("Cancelled");
            booking.setCancelReason(reason); // 存入取消原因
            booking.setCancelSource("Specialist"); // 标记为专家取消 [cite: 5]
            
            bookingRepository.save(booking);
            return ResponseEntity.ok(Map.of("code", 200, "message", "取消成功，取消原因已发送给客户"));
        }
        return ResponseEntity.status(404).body(Map.of("message", "Booking not found"));
    }
}