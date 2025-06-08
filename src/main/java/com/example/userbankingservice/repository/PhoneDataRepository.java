package com.example.userbankingservice.repository;

import com.example.userbankingservice.entity.PhoneData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PhoneDataRepository extends JpaRepository<PhoneData, Long> {
    boolean existsByPhone(String phone);
    Optional<PhoneData> findByPhone(String phone);
    long countByUserId(Long userId);
}
