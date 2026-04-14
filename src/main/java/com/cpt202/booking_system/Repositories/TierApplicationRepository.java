package com.cpt202.booking_system.Repositories;

import com.cpt202.booking_system.Models.TierApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface TierApplicationRepository extends JpaRepository<TierApplication, Long> {
    // 查找某个专家最新的一条申请
    Optional<TierApplication> findFirstBySpecialistEmailOrderByCreatedAtDesc(String email);
    
    // 查找所有待审核的申请
    List<TierApplication> findByStatus(String status);
}