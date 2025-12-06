package com.logisco.service;

import com.logisco.model.CourierPartner;
import com.logisco.model.Serviceability;
import com.logisco.repository.CourierPartnerRepository;
import com.logisco.repository.ServiceabilityRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ServiceabilityService {

    @Autowired
    private ServiceabilityRepository serviceabilityRepository;

    @Autowired
    private CourierPartnerRepository courierPartnerRepository;

    /**
     * Check if a pincode is serviceable by a courier partner
     */
    public Serviceability checkServiceability(Long courierPartnerId, String pincode) {
        return serviceabilityRepository.findByCourierPartnerIdAndPincode(courierPartnerId, pincode)
                .orElseGet(() -> {
                    // If not found in database, create a default serviceable entry
                    // In production, this would call the courier partner's API
                    Serviceability serviceability = new Serviceability();
                    CourierPartner partner = courierPartnerRepository.findById(courierPartnerId)
                            .orElseThrow(() -> new RuntimeException("Courier partner not found"));
                    serviceability.setCourierPartner(partner);
                    serviceability.setPincode(pincode);
                    serviceability.setStatus(Serviceability.ServiceStatus.SERVICEABLE);
                    serviceability.setCodAvailable(true);
                    serviceability.setEstimatedDays(3);
                    return serviceabilityRepository.save(serviceability);
                });
    }

    /**
     * Check serviceability for all active courier partners
     */
    public List<Map<String, Object>> checkServiceabilityForAllPartners(String pincode) {
        List<Map<String, Object>> results = new ArrayList<>();
        
        List<CourierPartner> partners = courierPartnerRepository.findAll();
        
        for (CourierPartner partner : partners) {
            if (partner.getActive()) {
                Serviceability serviceability = checkServiceability(partner.getId(), pincode);
                
                Map<String, Object> result = new HashMap<>();
                result.put("courierPartnerId", partner.getId());
                result.put("courierPartnerName", partner.getName());
                result.put("courierPartnerCode", partner.getCode());
                result.put("serviceable", serviceability.getStatus() == Serviceability.ServiceStatus.SERVICEABLE);
                result.put("codAvailable", serviceability.getCodAvailable());
                result.put("estimatedDays", serviceability.getEstimatedDays());
                
                results.add(result);
            }
        }
        
        return results;
    }

    /**
     * Validate pickup and delivery pincodes are serviceable
     */
    public Map<String, Object> validateServiceability(String pickupPincode, String deliveryPincode, Long courierPartnerId) {
        Map<String, Object> result = new HashMap<>();
        
        Serviceability pickupServiceability = checkServiceability(courierPartnerId, pickupPincode);
        Serviceability deliveryServiceability = checkServiceability(courierPartnerId, deliveryPincode);
        
        boolean pickupServiceable = pickupServiceability.getStatus() == Serviceability.ServiceStatus.SERVICEABLE;
        boolean deliveryServiceable = deliveryServiceability.getStatus() == Serviceability.ServiceStatus.SERVICEABLE;
        
        result.put("pickupServiceable", pickupServiceable);
        result.put("deliveryServiceable", deliveryServiceable);
        result.put("serviceable", pickupServiceable && deliveryServiceable);
        result.put("pickupCodAvailable", pickupServiceability.getCodAvailable());
        result.put("deliveryCodAvailable", deliveryServiceability.getCodAvailable());
        result.put("estimatedDays", Math.max(
            pickupServiceability.getEstimatedDays() != null ? pickupServiceability.getEstimatedDays() : 0,
            deliveryServiceability.getEstimatedDays() != null ? deliveryServiceability.getEstimatedDays() : 0
        ));
        
        return result;
    }
}

