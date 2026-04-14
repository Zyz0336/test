package com.cpt202.booking_system.Controllers;

import com.cpt202.booking_system.Models.Booking;
import com.cpt202.booking_system.Models.Specialist;
import com.cpt202.booking_system.Models.ScheduleSlot;
import com.cpt202.booking_system.Repositories.BookingRepository;
import com.cpt202.booking_system.Repositories.SpecialistRepository;
import com.cpt202.booking_system.Repositories.ScheduleSlotRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/customer")
public class CustomerController {

    @Autowired private SpecialistRepository specialistRepository;
    @Autowired private BookingRepository bookingRepository;
    @Autowired private ScheduleSlotRepository scheduleSlotRepository;

    @GetMapping("/specialists")
    public ResponseEntity<?> getActiveSpecialists() {
        List<Specialist> specialists = specialistRepository.findByStatus("Active");
        return ResponseEntity.ok(Map.of("code", 200, "data", specialists));
    }

    @PostMapping("/bookings/create")
    @Transactional 
    public ResponseEntity<?> createBooking(@RequestBody Map<String, Object> payload) {
        try {
            String customerEmail = (String) payload.get("customerEmail");
            String specialistEmail = (String) payload.get("specialistEmail");
            String specialistName = (String) payload.get("specialistName");
            String slotTime = (String) payload.get("slotTime");
            String topic = (String) payload.get("topic");
            String notes = (String) payload.get("notes");
            Double fee = Double.valueOf(payload.get("fee").toString());
            Long slotId = Long.valueOf(payload.get("slotId").toString());

            Optional<ScheduleSlot> slotOpt = scheduleSlotRepository.findById(slotId);
            if (slotOpt.isEmpty() || !"Available".equals(slotOpt.get().getStatus())) {
                return ResponseEntity.badRequest().body(Map.of("message", "Slot no longer available."));
            }

            ScheduleSlot slot = slotOpt.get();
            slot.setStatus("Occupied"); 
            scheduleSlotRepository.save(slot);

            Booking booking = new Booking();
            booking.setCustomerEmail(customerEmail);
            booking.setSpecialistEmail(specialistEmail);
            booking.setSpecialistName(specialistName);
            booking.setSlotTime(slotTime);
            booking.setTopic(topic);
            booking.setNotes(notes);
            booking.setFee(fee); 
            booking.setSlotId(slotId); 
            booking.setStatus("Pending");
            
            bookingRepository.save(booking);
            return ResponseEntity.ok(Map.of("code", 200, "message", "Booking successful!"));
            
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("message", "Error: " + e.getMessage()));
        }
    }

    @GetMapping("/bookings")
    public ResponseEntity<?> getMyBookings(@RequestParam String email) {
        List<Booking> myBookings = bookingRepository.findByCustomerEmail(email);
        return ResponseEntity.ok(Map.of("code", 200, "data", myBookings));
    }

    @PutMapping("/bookings/{id}/cancel")
    @Transactional 
    public ResponseEntity<?> cancelBooking(@PathVariable Long id) {
        Optional<Booking> bookingOpt = bookingRepository.findById(id);
        if (bookingOpt.isPresent()) {
            Booking booking = bookingOpt.get();
            
            if (booking.getSlotId() != null) {
                scheduleSlotRepository.findById(booking.getSlotId()).ifPresent(slot -> {
                    slot.setStatus("Available");
                    scheduleSlotRepository.save(slot);
                });
            }

            booking.setStatus("Cancelled");
            booking.setCancelSource("Customer"); 
            booking.setCancelReason("Customer cancelled and requested refund.");
            bookingRepository.save(booking);

            return ResponseEntity.ok(Map.of(
                "code", 200, 
                "message", "Cancelled. A refund of ¥" + booking.getFee() + " has been processed."
            ));
        }
        return ResponseEntity.status(404).body(Map.of("message", "Booking not found."));
    }

    @GetMapping("/specialists/slots")
    public ResponseEntity<?> getSpecialistAvailableSlots(@RequestParam String email) {
        List<ScheduleSlot> allSlots = scheduleSlotRepository.findBySpecialistEmail(email);
        List<ScheduleSlot> availableSlots = allSlots.stream()
                .filter(slot -> "Available".equals(slot.getStatus()))
                .toList();
        return ResponseEntity.ok(Map.of("code", 200, "data", availableSlots));
    }
}