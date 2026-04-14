package com.cpt202.booking_system.Repositories;

import com.cpt202.booking_system.Models.ScheduleSlot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ScheduleSlotRepository extends JpaRepository<ScheduleSlot, Long> {
    
    // 找出某个专家的所有排班
    List<ScheduleSlot> findBySpecialistEmail(String email);
    
    // 冲突检测：检查该专家在这个日期和开始时间是否已经排过班了
    boolean existsBySpecialistEmailAndDateAndStartTime(String email, String date, String startTime);
}