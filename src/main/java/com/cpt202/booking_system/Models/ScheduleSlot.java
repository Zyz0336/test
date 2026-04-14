package com.cpt202.booking_system.Models;

import jakarta.persistence.*;

@Entity
@Table(name = "schedule_slots")
public class ScheduleSlot {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String specialistEmail; // 这个时间段属于哪个专家
    
    private String date; // 日期 (YYYY-MM-DD)
    private String startTime; // 开始时间 (HH:mm)
    private String endTime; // 结束时间 (HH:mm)
    
    private String status; // 状态: "Available" (可预约), "Booked" (已被预约)

    // --- Getter and Setter ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getSpecialistEmail() { return specialistEmail; }
    public void setSpecialistEmail(String specialistEmail) { this.specialistEmail = specialistEmail; }
    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }
    public String getStartTime() { return startTime; }
    public void setStartTime(String startTime) { this.startTime = startTime; }
    public String getEndTime() { return endTime; }
    public void setEndTime(String endTime) { this.endTime = endTime; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}