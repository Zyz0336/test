package com.cpt202.booking_system.Repositories;
import com.cpt202.booking_system.Models.Specialist;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface SpecialistRepository extends JpaRepository<Specialist, Long> {
    Optional<Specialist> findByEmail(String email);
    List<Specialist> findByStatus(String status);
}