package com.logisco.repository;

import com.logisco.model.CourierPartner;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CourierPartnerRepository extends JpaRepository<CourierPartner, Long> {
    Optional<CourierPartner> findByCode(String code);
    Optional<CourierPartner> findByName(String name);
}

