package com.bankingapp.Service;

import com.bankingapp.Dto.UserRegistrationDto;

public interface OtpService {

    /**
     * Generates a 6-digit OTP, stores it in the cache, and sends it via email.
     * This is used for LOGINS.
     *
     * @param email The user's email address.
     */
    void generateAndSendLoginOtp(String email);

    /**
     * Generates a 6-digit OTP, stores it, and also caches the user's registration data.
     * This is used for REGISTRATION.
     *
     * @param registrationDto The user's pending registration data.
     */
    void generateAndSendRegistrationOtp(UserRegistrationDto registrationDto);

    /**
     * Validates the provided OTP against the one stored in the cache.
     *
     * @param email The user's email.
     * @param otp The OTP provided by the user.
     * @return true if valid, false otherwise.
     */
    boolean validateOtp(String email, String otp);

    /**
     * Retrieves and removes a pending registration from the cache after successful OTP validation.
     *
     * @param email The user's email.
     * @return The cached UserRegistrationDto, or null if not found.
     */
    UserRegistrationDto getPendingRegistration(String email);
}