package com.cpt202.booking_system.Models;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "bookings")
public class Booking {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String orderNumber; 
    private String customerEmail; 
    private String specialistEmail; 
    private String specialistName; 
    private String slotTime; 
    
    // 💰 核心：记录该笔订单产生的总费用
    private Double fee; 
    
    // 🌟 新增：记录对应的时间片 ID
    private Long slotId; 

    @Column(nullable = false, length = 50)
    private String topic; 
    
    @Column(length = 100)
    private String notes; 
    
    private String status; // Pending, Confirmed, Cancelled, Completed

    @Column(length = 255)
    private String cancelReason; 

    private String cancelSource; 

    private LocalDateTime createdAt; 

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.orderNumber = "ORD-" + System.currentTimeMillis();
    }

    // ==========================================
    // Getter & Setter
    // ==========================================
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getOrderNumber() { return orderNumber; }
    public void setOrderNumber(String orderNumber) { this.orderNumber = orderNumber; }
    public String getCustomerEmail() { return customerEmail; }
    public void setCustomerEmail(String customerEmail) { this.customerEmail = customerEmail; }
    public String getSpecialistEmail() { return specialistEmail; }
    public void setSpecialistEmail(String specialistEmail) { this.specialistEmail = specialistEmail; }
    public String getSpecialistName() { return specialistName; }
    public void setSpecialistName(String specialistName) { this.specialistName = specialistName; }
    public String getSlotTime() { return slotTime; }
    public void setSlotTime(String slotTime) { this.slotTime = slotTime; }
    public Double getFee() { return fee; }
    public void setFee(Double fee) { this.fee = fee; }
    
    // 🌟 新增的两个方法
    public Long getSlotId() { return slotId; }
    public void setSlotId(Long slotId) { this.slotId = slotId; }

    public String getTopic() { return topic; }
    public void setTopic(String topic) { this.topic = topic; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public String getCancelReason() { return cancelReason; }
    public void setCancelReason(String cancelReason) { this.cancelReason = cancelReason; }
    public String getCancelSource() { return cancelSource; }
    public void setCancelSource(String cancelSource) { this.cancelSource = cancelSource; }
}