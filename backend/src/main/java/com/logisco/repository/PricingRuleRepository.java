package com.logisco.repository;

import com.logisco.model.PricingRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PricingRuleRepository extends JpaRepository<PricingRule, Long> {
    List<PricingRule> findByCourierPartnerIdAndActiveTrue(Long courierPartnerId);
    
    @Query("SELECT pr FROM PricingRule pr WHERE pr.courierPartner.id = :partnerId " +
           "AND pr.deliveryType = :deliveryType AND pr.packageType = :packageType " +
           "AND pr.active = true AND :weight BETWEEN pr.minWeight AND pr.maxWeight")
    List<PricingRule> findApplicableRules(
        @Param("partnerId") Long partnerId,
        @Param("deliveryType") PricingRule.DeliveryType deliveryType,
        @Param("packageType") PricingRule.PackageType packageType,
        @Param("weight") Double weight
    );
}

