package com.logisco.repository;

import com.logisco.model.Serviceability;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ServiceabilityRepository extends JpaRepository<Serviceability, Long> {
    Optional<Serviceability> findByCourierPartnerIdAndPincode(Long courierPartnerId, String pincode);
    List<Serviceability> findByPincode(String pincode);
    List<Serviceability> findByCourierPartnerIdAndStatus(Long courierPartnerId, Serviceability.ServiceStatus status);
}

