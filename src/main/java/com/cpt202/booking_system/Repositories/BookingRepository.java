package com.cpt202.booking_system.Repositories;

import com.cpt202.booking_system.Models.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    
    // 找出某个客户的所有订单
    List<Booking> findByCustomerEmail(String email);
    
    // 冲突校验：检查这个客户在这个时间段，是不是已经有不是"Cancelled"的订单了
    boolean existsByCustomerEmailAndSlotTimeAndStatusNot(String email, String slotTime, String status);

    // 🌟 新增：查找所有状态为某一种特定状态的订单 (用于计算 Dashboard 的已完成订单)
    List<Booking> findByStatus(String status);
}