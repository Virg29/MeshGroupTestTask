package com.example.demo.repository;

import com.example.demo.entity.EmailData;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmailDataRepository extends JpaRepository<EmailData, Long> {

    boolean existsByEmail(String email);
}
