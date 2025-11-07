package com.bankingapp.Service;

import com.bankingapp.Dto.UserRegistrationDto;
import com.google.common.cache.Cache;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.text.DecimalFormat;

@Service
@RequiredArgsConstructor // This injects all final fields
@Slf4j
public class OtpServiceImpl implements OtpService {

    private final EmailService emailService;

    // Injects the 'otpCache' bean from your CacheConfig.java
    @Qualifier("otpCache")
    private final Cache<String, String> otpCache;

    // Injects the 'registrationCache' bean from your CacheConfig.java
    @Qualifier("registrationCache")
    private final Cache<String, UserRegistrationDto> registrationCache;

    @Override
    public void generateAndSendLoginOtp(String email) {
        String otp = generateOtp();
        String cleanEmail = email.trim();
        otpCache.put(cleanEmail, otp);
        log.info("Generated LOGIN OTP {} for email {}", otp, cleanEmail);

        // --- UPDATED CALL ---
        // We pass 'null' for the name because we don't know it during login
        emailService.sendOtpEmail(cleanEmail, null, "Your Login OTP", otp);
    }

    @Override
    public void generateAndSendRegistrationOtp(UserRegistrationDto registrationDto) {
        String otp = generateOtp();
        String cleanEmail = registrationDto.getEmail().trim();
        otpCache.put(cleanEmail, otp);
        registrationCache.put(cleanEmail, registrationDto);
        log.info("Generated REGISTRATION OTP {} for email {}", otp, cleanEmail);

        // --- UPDATED CALL ---
        // We pass the name from the registration DTO
        emailService.sendOtpEmail(cleanEmail, registrationDto.getName(), "Your Registration OTP", otp);
    }

    @Override
    public boolean validateOtp(String email, String otp) {
        if (email == null || otp == null) {
            log.warn("Validation failed: Email or OTP is null.");
            return false;
        }
        String cleanEmail = email.trim();
        String cleanOtp = otp.trim();

        String cachedOtp = otpCache.getIfPresent(cleanEmail);

        if (cachedOtp == null) {
            log.error("Validation failed for {}: No OTP found in cache. It may have expired.", cleanEmail);
            return false;
        }

        log.info("Validating OTP for {}: Submitted [{}], Expected [{}]", cleanEmail, cleanOtp, cachedOtp);

        if (cachedOtp.equals(cleanOtp)) {
            otpCache.invalidate(cleanEmail); // OTP is used, invalidate it
            log.info("Validation SUCCESS for {}", cleanEmail);
            return true;
        } else {
            log.warn("Validation FAILED for {}: Submitted OTP does not match cached OTP.", cleanEmail);
            return false;
        }
    }

    @Override
    public UserRegistrationDto getPendingRegistration(String email) {
        if (email == null) {
            return null;
        }
        String cleanEmail = email.trim();
        UserRegistrationDto dto = registrationCache.getIfPresent(cleanEmail);
        if (dto != null) {
            registrationCache.invalidate(cleanEmail); // Registration data is used, invalidate it
            return dto;
        }
        return null;
    }

    private String generateOtp() {
        SecureRandom random = new SecureRandom();
        int otp = 100000 + random.nextInt(900000);
        return new DecimalFormat("000000").format(otp);
    }
}