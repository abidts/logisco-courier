package com.logisco.service;

import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Random;

@Service
public class OtpService {
    private static class OtpRecord {
        String code;
        LocalDateTime expiresAt;
        String fullName;
    }

    private final Map<String, OtpRecord> store = new ConcurrentHashMap<>();
    private final Random random = new Random();

    public void generateOtp(String phone, String fullName) {
        OtpRecord rec = new OtpRecord();
        rec.code = String.format("%06d", 100000 + random.nextInt(900000));
        rec.expiresAt = LocalDateTime.now().plusMinutes(5);
        rec.fullName = fullName;
        store.put(phone, rec);
        System.out.println("[OTP] Phone=" + phone + " Code=" + rec.code + " Expires=" + rec.expiresAt);
    }

    public boolean verifyOtp(String phone, String code) {
        OtpRecord rec = store.get(phone);
        if (rec == null) return false;
        if (LocalDateTime.now().isAfter(rec.expiresAt)) {
            store.remove(phone);
            return false;
        }
        boolean ok = rec.code.equals(code);
        if (ok) store.remove(phone);
        return ok;
    }

    public String getFullName(String phone) {
        OtpRecord rec = store.get(phone);
        return rec != null ? rec.fullName : null;
    }
}
