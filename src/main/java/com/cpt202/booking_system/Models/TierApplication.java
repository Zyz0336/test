package com.cpt202.booking_system.Models;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "tier_applications") // 强制小写
public class TierApplication {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "specialist_email", nullable = false)
    private String specialistEmail;

    @Column(name = "target_level", nullable = false)
    private String targetLevel; // Tier A, B, C

    @Column(name = "reason", length = 500)
    private String reason;

    @Column(name = "status")
    private String status = "Pending"; // Pending, Approved, Rejected

    @Column(name = "admin_comment")
    private String adminComment; // 管理员填写的拒绝理由

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    // Getter & Setter
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getSpecialistEmail() { return specialistEmail; }
    public void setSpecialistEmail(String specialistEmail) { this.specialistEmail = specialistEmail; }
    public String getTargetLevel() { return targetLevel; }
    public void setTargetLevel(String targetLevel) { this.targetLevel = targetLevel; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getAdminComment() { return adminComment; }
    public void setAdminComment(String adminComment) { this.adminComment = adminComment; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}